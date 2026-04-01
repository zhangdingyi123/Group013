package com.bupt.ta.web;

import com.bupt.ta.model.Applicant;
import com.bupt.ta.service.ApplicantService;
import com.bupt.ta.service.assistant.AssistantConfig;
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
import java.nio.charset.StandardCharsets;
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
    private static final double TEMPERATURE = 0.6;

    private static final String SYSTEM_PROMPT = "你是北京邮电大学国际学院「助教招聘系统」的站内智能小助手。"
            + "请用简洁、清晰的中文回答；若问题与系统无关，也可友好作答。"
            + "说明：本系统数据存于 JSON 文件，无数据库；涉及账号与隐私时请提醒用户通过官方渠道核实。";

    private static final String RESUME_MODE_SUFFIX = "\n\n【简历辅助】用户提供了简历正文（见文末）。请根据用户的问题协助润色表述、调整结构、提炼要点或指出可改进之处；"
            + "不要编造经历或学历；信息不足时请直接说明。";

    private final OpenAiCompatibleChatClient client = new OpenAiCompatibleChatClient();
    private final ApplicantService applicantService = new ApplicantService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

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
        if (useSavedResume && resumeBlock == null) {
            HttpSession session = req.getSession(false);
            Applicant user = session != null ? (Applicant) session.getAttribute("taUser") : null;
            if (user == null) {
                writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "login required for saved resume");
                return;
            }
            String path = user.getResumePath();
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

        try {
            OpenAiCompatibleChatClient.ChatResult result = client.complete(url, apiKey, model, withSystem, TEMPERATURE);
            JsonObject out = new JsonObject();
            out.addProperty("provider", provider);
            out.addProperty("model", model);
            if (result.isOk()) {
                out.addProperty("reply", result.content);
            } else {
                out.addProperty("error", result.rawError != null ? result.rawError : "unknown error");
                resp.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
            }
            writeJson(resp, out);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            writeError(resp, HttpServletResponse.SC_GATEWAY_TIMEOUT, "interrupted");
        } catch (Exception e) {
            writeError(resp, HttpServletResponse.SC_BAD_GATEWAY, e.getMessage() != null ? e.getMessage() : "upstream error");
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
