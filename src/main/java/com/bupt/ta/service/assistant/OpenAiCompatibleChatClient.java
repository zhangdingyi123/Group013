package com.bupt.ta.service.assistant;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 调用 OpenAI 兼容的 Chat Completions（Kimi / 通义千问）。
 */
public class OpenAiCompatibleChatClient {

    private static final int CONNECT_TIMEOUT_SEC = 20;
    private static final int REQUEST_TIMEOUT_SEC = 120;
    private static final String USER_AGENT = "BUPT-TA-Recruitment/1.0";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(CONNECT_TIMEOUT_SEC))
            .build();

    public static final class ChatMessage {
        public final String role;
        public final String content;

        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    public static final class ChatResult {
        public final String content;
        public final String rawError;

        public ChatResult(String content, String rawError) {
            this.content = content;
            this.rawError = rawError;
        }

        public boolean isOk() {
            return content != null && !content.isEmpty();
        }
    }

    public ChatResult complete(String url, String apiKey, String model, List<ChatMessage> messages, double temperature)
            throws IOException, InterruptedException {
        JsonObject body = new JsonObject();
        body.addProperty("model", model);
        body.addProperty("temperature", temperature);
        JsonArray arr = new JsonArray();
        for (ChatMessage m : messages) {
            JsonObject one = new JsonObject();
            one.addProperty("role", m.role);
            one.addProperty("content", m.content);
            arr.add(one);
        }
        body.add("messages", arr);

        String json = body.toString();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Authorization", "Bearer " + apiKey)
                .header("User-Agent", USER_AGENT)
                .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SEC))
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        String respBody = response.body();
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            return new ChatResult(null, "HTTP " + response.statusCode() + ": " + truncate(respBody, 2000));
        }
        try {
            JsonObject root = new JsonParser().parse(respBody).getAsJsonObject();
            if (root.has("error")) {
                JsonElement err = root.get("error");
                return new ChatResult(null, err.isJsonObject() ? err.getAsJsonObject().toString() : err.toString());
            }
            JsonArray choices = root.getAsJsonArray("choices");
            if (choices == null || choices.size() == 0) {
                return new ChatResult(null, "empty choices: " + truncate(respBody, 500));
            }
            JsonObject first = choices.get(0).getAsJsonObject();
            JsonObject msg = first.getAsJsonObject("message");
            String content = msg.has("content") ? msg.get("content").getAsString() : "";
            return new ChatResult(content, null);
        } catch (Exception e) {
            return new ChatResult(null, "parse error: " + e.getMessage() + " body=" + truncate(respBody, 800));
        }
    }

    public static List<ChatMessage> withSystemPrompt(List<ChatMessage> userMessages, String systemPrompt) {
        List<ChatMessage> out = new ArrayList<>(userMessages.size() + 1);
        out.add(new ChatMessage("system", systemPrompt));
        out.addAll(userMessages);
        return out;
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        if (s.length() <= max) {
            return s;
        }
        return s.substring(0, max) + "...";
    }
}
