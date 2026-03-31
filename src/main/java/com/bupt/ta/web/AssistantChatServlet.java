package com.bupt.ta.web;

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
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JSON API：小助手对话（Kimi K2.5 / 通义千问），OpenAI 兼容接口。
 */
@WebServlet("/api/assistant/chat")
public class AssistantChatServlet extends HttpServlet {

    private static final int MAX_INPUT_CHARS = 12000;
    private static final double TEMPERATURE = 0.6;

    private static final String SYSTEM_PROMPT = "你是北京邮电大学国际学院「助教招聘系统」的站内智能小助手。"
            + "请用简洁、清晰的中文回答；若问题与系统无关，也可友好作答。"
            + "说明：本系统数据存于 JSON 文件，无数据库；涉及账号与隐私时请提醒用户通过官方渠道核实。";

    private final OpenAiCompatibleChatClient client = new OpenAiCompatibleChatClient();

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
            if (AssistantConfig.PROVIDER_KIMI.equals(p) || AssistantConfig.PROVIDER_QWEN.equals(p)) {
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

        List<OpenAiCompatibleChatClient.ChatMessage> withSystem =
                OpenAiCompatibleChatClient.withSystemPrompt(list, SYSTEM_PROMPT);

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
