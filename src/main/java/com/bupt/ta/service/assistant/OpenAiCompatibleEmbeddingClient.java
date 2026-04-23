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

/**
 * 调用 OpenAI 兼容的 Embeddings 接口（返回单条向量）。
 */
public class OpenAiCompatibleEmbeddingClient {

    private static final int CONNECT_TIMEOUT_SEC = 20;
    private static final int REQUEST_TIMEOUT_SEC = 60;
    private static final String USER_AGENT = "BUPT-TA-Recruitment/1.0";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(CONNECT_TIMEOUT_SEC))
            .build();

    public static final class EmbeddingResult {
        public final float[] embedding;
        public final String rawError;

        public EmbeddingResult(float[] embedding, String rawError) {
            this.embedding = embedding;
            this.rawError = rawError;
        }

        public boolean isOk() {
            return embedding != null && embedding.length > 0;
        }
    }

    public EmbeddingResult embed(String url, String apiKey, String model, String input)
            throws IOException, InterruptedException {
        JsonObject body = new JsonObject();
        body.addProperty("model", model);
        body.addProperty("input", input);

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
            return new EmbeddingResult(null, "HTTP " + response.statusCode() + ": " + truncate(respBody, 2000));
        }
        try {
            JsonObject root = new JsonParser().parse(respBody).getAsJsonObject();
            if (root.has("error")) {
                JsonElement err = root.get("error");
                return new EmbeddingResult(null, err.isJsonObject() ? err.getAsJsonObject().toString() : err.toString());
            }
            JsonArray data = root.getAsJsonArray("data");
            if (data == null || data.size() == 0) {
                return new EmbeddingResult(null, "empty data: " + truncate(respBody, 500));
            }
            JsonObject first = data.get(0).getAsJsonObject();
            JsonArray emb = first.getAsJsonArray("embedding");
            if (emb == null || emb.size() == 0) {
                return new EmbeddingResult(null, "empty embedding: " + truncate(respBody, 500));
            }
            float[] vec = new float[emb.size()];
            for (int i = 0; i < emb.size(); i++) {
                vec[i] = emb.get(i).getAsFloat();
            }
            return new EmbeddingResult(vec, null);
        } catch (Exception e) {
            return new EmbeddingResult(null, "parse error: " + e.getMessage() + " body=" + truncate(respBody, 800));
        }
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

