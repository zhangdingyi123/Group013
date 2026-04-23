package com.bupt.ta.web;

import com.bupt.ta.model.Applicant;
import com.bupt.ta.service.ApplicantService;
import com.bupt.ta.service.assistant.AssistantConfig;
import com.bupt.ta.service.assistant.AssistantQuotaService;
import com.bupt.ta.service.assistant.AssistantScopeGuard;
import com.bupt.ta.service.assistant.OpenAiCompatibleChatClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JSON API：小助手对话（Kimi / 通义千问 / OpenAI），OpenAI 兼容接口。
 */
@WebServlet("/api/assistant/chat")
public class AssistantChatServlet extends HttpServlet {

    private static final int MAX_INPUT_CHARS = 12000;
    private static final int MAX_RESUME_CHARS = 10000;
    /** 通义 / OpenAI 等使用的采样温度 */
    private static final double TEMPERATURE = 0.6;
    /** Kimi（如 kimi-k2.5）上游要求 temperature 固定为 1，否则返回 400 */
    private static final double TEMPERATURE_KIMI = 1.0;

    /** 与无关问题时的固定回复话术保持一致，便于模型严格执行 */
    private static final String OFF_TOPIC_REPLY = "抱歉，我只能协助助教招聘系统相关的问题，请换个与本站功能或应聘相关的问题。";

    private static final String SYSTEM_PROMPT = "你是北京邮电大学国际学院「助教招聘系统」的站内智能小助手。"
            + "【范围】仅回答与本系统或本助教招聘直接相关的“问题解决类”内容，例如：站内功能与流程（岗位浏览、申请、简历上传、个人中心、招聘方端、论坛与私信等）、常见报错/无法操作的排查建议、岗位职责与应聘材料说明、简历/自我介绍等与助教应聘直接相关的修改建议。"
            + "【拒绝】不回答与本系统及本招聘无关的闲聊、学科作业辅导、代写代码、其他产品/网站、政治敏感、违法、医疗诊断等内容。"
            + "若用户当前问题不在上述范围内，你须仅用下面这句话回复，不要添加任何其他解释或客套：「" + OFF_TOPIC_REPLY + "」"
            + "请用简洁、清晰的中文；说明：本系统数据存于 JSON 文件，无数据库；涉及账号与隐私时请提醒用户通过官方渠道核实。";

    private static final String RESUME_MODE_SUFFIX = "\n\n【简历辅助】用户提供了简历正文（见文末）。"
            + "仅围绕该简历的润色、结构、要点提炼，以及与本助教招聘相关的问题作答；"
            + "若用户转而闲聊或提出与简历/应聘无关的请求，请按系统主提示中的固定话术拒绝。"
            + "协助时润色表述、调整结构、提炼要点或指出可改进之处；不要编造经历或学历；信息不足时请直接说明。";

    private final OpenAiCompatibleChatClient client = new OpenAiCompatibleChatClient();
    private final ApplicantService applicantService = new ApplicantService();
    private final AssistantQuotaService quotaService = new AssistantQuotaService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        HttpSession authSession = req.getSession(false);
        Applicant authUser = authSession != null ? (Applicant) authSession.getAttribute("taUser") : null;
        if (authUser == null || authUser.getId() == null || authUser.getId().trim().isEmpty()) {
            writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "login required");
            return;
        }

        String body;
        try (java.io.BufferedReader reader = req.getReader()) {
            body = reader.lines().collect(Collectors.joining("\n"));
        }
        if (body == null || body.trim().isEmpty()) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "empty body");
            return;
        }

        JsonObject root;
        try {
            root = new JsonParser().parse(body).getAsJsonObject();
        } catch (Exception e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "invalid json");
            return;
        }

        String provider = AssistantConfig.defaultProvider();
        if (root.has("provider") && root.get("provider").isJsonPrimitive()) {
            String p = root.get("provider").getAsString().trim().toLowerCase();
            if (AssistantConfig.PROVIDER_KIMI.equals(p) || AssistantConfig.PROVIDER_QWEN.equals(p)
                    || AssistantConfig.PROVIDER_OPENAI.equals(p)) {
                provider = p;
            }
        }

        JsonArray messagesJson = root.getAsJsonArray("messages");
        if (messagesJson == null || messagesJson.size() == 0) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "messages required");
            return;
        }

        List<OpenAiCompatibleChatClient.ChatMessage> list = new ArrayList<>();
        int totalChars = 0;
        for (JsonElement el : messagesJson) {
            if (!el.isJsonObject()) {
                continue;
            }
            JsonObject o = el.getAsJsonObject();
            String role = o.has("role") ? o.get("role").getAsString() : "user";
            String content = o.has("content") ? o.get("content").getAsString() : "";
            if (!"user".equals(role) && !"assistant".equals(role)) {
                role = "user";
            }
            totalChars += content.length();
            if (totalChars > MAX_INPUT_CHARS) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "input too long");
                return;
            }
            list.add(new OpenAiCompatibleChatClient.ChatMessage(role, content));
        }
        if (list.isEmpty()) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "no valid messages");
            return;
        }

        String resumeBlock = null;
        if (root.has("resumeText") && root.get("resumeText").isJsonPrimitive()) {
            String rt = root.get("resumeText").getAsString();
            if (rt.length() > MAX_RESUME_CHARS) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "resume text too long");
                return;
            }
            String trimmed = rt.trim();
            if (!trimmed.isEmpty()) {
                resumeBlock = trimmed;
            }
        }
        boolean useSavedResume = root.has("useSavedResume") && root.get("useSavedResume").isJsonPrimitive()
                && root.get("useSavedResume").getAsBoolean();

        // 严格限制提问范围：明显无关则直接返回固定拒绝话术（避免上游模型“跑题”或被提示注入）。
        boolean resumeModeRequested = resumeBlock != null || useSavedResume;
        if (AssistantConfig.strictScopeEnabled()
                && !AssistantScopeGuard.isInScope(list, resumeModeRequested)) {
            JsonObject out = new JsonObject();
            out.addProperty("provider", provider);
            out.addProperty("model", "scope-guard");
            out.addProperty("reply", OFF_TOPIC_REPLY);
            writeJson(resp, out);
            return;
        }

        if (useSavedResume && resumeBlock == null) {
            String path = authUser.getResumePath();
            if (path == null || path.trim().isEmpty()) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "no saved resume");
                return;
            }
            try {
                String content = applicantService.extractResumePlainText(path);
                if (content == null) {
                    writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "saved resume format not supported");
                    return;
                }
                String trimmed = content.trim();
                if (trimmed.isEmpty()) {
                    writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "saved resume is empty or unreadable");
                    return;
                }
                resumeBlock = trimmed;
                if (resumeBlock.length() > MAX_RESUME_CHARS) {
                    resumeBlock = resumeBlock.substring(0, MAX_RESUME_CHARS);
                }
            } catch (IOException e) {
                writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "cannot read resume");
                return;
            } catch (Exception e) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "cannot extract resume text");
                return;
            }
        }

        String systemPrompt = SYSTEM_PROMPT;
        if (resumeBlock != null && !resumeBlock.isEmpty()) {
            systemPrompt = SYSTEM_PROMPT + RESUME_MODE_SUFFIX + "\n\n--- 简历正文 ---\n" + resumeBlock + "\n--- 结束 ---";
        }

        List<OpenAiCompatibleChatClient.ChatMessage> withSystem =
                OpenAiCompatibleChatClient.withSystemPrompt(list, systemPrompt);

        // 额度校验：按登录助教 applicantId 计费。
        String userKey = "applicant:" + authUser.getId().trim();
        AssistantQuotaService.ConsumeResult quota = quotaService.tryConsume(userKey);
        if (!quota.ok) {
            resp.setStatus(HttpServletResponse.SC_PAYMENT_REQUIRED); // 402
            JsonObject o = new JsonObject();
            o.addProperty("error", quota.message);
            o.addProperty("code", quota.code);
            if (quota.status != null) {
                o.addProperty("period", quota.status.period);
                o.addProperty("freeLimit", quota.status.freeLimit);
                o.addProperty("freeUsed", quota.status.freeUsed);
                o.addProperty("freeRemaining", quota.status.freeRemaining);
                o.addProperty("paidCredits", quota.status.paidCredits);
            }
            String hint = AssistantConfig.payHint();
            if (hint != null && !hint.trim().isEmpty()) {
                o.addProperty("payHint", hint.trim());
            }
            writeJson(resp, o);
            return;
        }

        String url;
        String apiKey;
        String model;
        if (AssistantConfig.PROVIDER_QWEN.equals(provider)) {
            apiKey = AssistantConfig.qwenApiKey();
            if (apiKey.isEmpty()) {
                writeError(resp, HttpServletResponse.SC_SERVICE_UNAVAILABLE, "QWEN_API_KEY not configured");
                return;
            }
            url = AssistantConfig.qwenBaseUrl();
            model = AssistantConfig.qwenModel();
        } else if (AssistantConfig.PROVIDER_OPENAI.equals(provider)) {
            apiKey = AssistantConfig.openaiApiKey();
            if (apiKey.isEmpty()) {
                writeError(resp, HttpServletResponse.SC_SERVICE_UNAVAILABLE, "OPENAI_API_KEY not configured");
                return;
            }
            url = AssistantConfig.openaiBaseUrl();
            model = AssistantConfig.openaiModel();
        } else {
            apiKey = AssistantConfig.kimiApiKey();
            if (apiKey.isEmpty()) {
                writeError(resp, HttpServletResponse.SC_SERVICE_UNAVAILABLE, "KIMI_API_KEY not configured");
                return;
            }
            url = AssistantConfig.kimiBaseUrl();
            model = AssistantConfig.kimiModel();
        }

        double temperature = AssistantConfig.PROVIDER_KIMI.equals(provider) ? TEMPERATURE_KIMI : TEMPERATURE;

        try {
            OpenAiCompatibleChatClient.ChatResult result = client.complete(url, apiKey, model, withSystem, temperature);
            JsonObject out = new JsonObject();
            out.addProperty("provider", provider);
            out.addProperty("model", model);
            if (result.isOk()) {
                out.addProperty("reply", result.content);
                if (quota.status != null) {
                    out.addProperty("period", quota.status.period);
                    out.addProperty("freeRemaining", quota.status.freeRemaining);
                    out.addProperty("paidCredits", quota.status.paidCredits);
                }
            } else {
                out.addProperty("error", result.rawError != null ? result.rawError : "unknown error");
                resp.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
                // 上游失败时退款，避免“扣了次数但没回答”
                try {
                    quotaService.refundOnce(userKey, quota.usedPaid);
                } catch (Exception ignored) {}
            }
            writeJson(resp, out);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            writeError(resp, HttpServletResponse.SC_GATEWAY_TIMEOUT, "interrupted");
            try {
                quotaService.refundOnce(userKey, quota.usedPaid);
            } catch (Exception ignored) {}
        } catch (Exception e) {
            writeError(resp, HttpServletResponse.SC_BAD_GATEWAY, e.getMessage() != null ? e.getMessage() : "upstream error");
            try {
                quotaService.refundOnce(userKey, quota.usedPaid);
            } catch (Exception ignored) {}
        }
    }

    private void writeJson(HttpServletResponse resp, JsonObject obj) throws IOException {
        PrintWriter w = resp.getWriter();
        w.write(obj.toString());
    }

    private void writeError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        JsonObject o = new JsonObject();
        o.addProperty("error", message);
        writeJson(resp, o);
    }
}
