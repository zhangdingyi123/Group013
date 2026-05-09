package com.bupt.ta.service;

import com.bupt.ta.model.Applicant;
import com.bupt.ta.model.EmbeddingRecord;
import com.bupt.ta.model.Job;
import com.bupt.ta.service.assistant.AssistantConfig;
import com.bupt.ta.service.assistant.OpenAiCompatibleEmbeddingClient;
import com.bupt.ta.storage.Storage;
import com.bupt.ta.util.HashUtil;
import com.bupt.ta.util.VectorUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基于向量 embeddings 的语义匹配（可选开关，默认关闭）。
 * <p>注意：首次计算会调用外部 embeddings API；结果写入 data/embeddings.json 作为缓存。</p>
 */
public class SemanticMatchService {
    private static final int MAX_TEXT_CHARS = 8000;

    private static final Object CACHE_LOCK = new Object();
    private static volatile Map<String, EmbeddingRecord> cache; // key -> record

    private final OpenAiCompatibleEmbeddingClient client = new OpenAiCompatibleEmbeddingClient();

    /**
     * 返回 0-100 的匹配分；若未开启/未配置/失败，返回 null（由调用方回退到旧算法）。
     */
    public Integer semanticMatchScore(Applicant applicant, Job job, String resumePlainText) {
        if (!AssistantConfig.semanticMatchEnabled()) {
            return null;
        }
        String provider = AssistantConfig.semanticMatchProvider();
        String url = AssistantConfig.embeddingsUrl(provider);
        String apiKey = AssistantConfig.apiKeyForProvider(provider);
        String model = AssistantConfig.embeddingModel(provider);
        if (url == null || url.isEmpty() || apiKey == null || apiKey.isEmpty() || model == null || model.isEmpty()) {
            return null;
        }
        if (applicant == null || job == null || applicant.getId() == null || job.getId() == null) {
            return null;
        }

        String applicantText = buildApplicantText(applicant, resumePlainText);
        String jobText = buildJobText(job);
        if (applicantText.isEmpty() || jobText.isEmpty()) {
            return null;
        }

        float[] a = embeddingForText("applicant:" + applicant.getId(), model, applicantText, url, apiKey);
        if (a == null) return null;
        float[] b = embeddingForText("job:" + job.getId(), model, jobText, url, apiKey);
        if (b == null) return null;
        double cosine = VectorUtil.cosineSimilarity(a, b);
        return VectorUtil.cosineToScore0to100(cosine);
    }

    private static String buildApplicantText(Applicant a, String resumePlainText) {
        StringBuilder sb = new StringBuilder();
        if (a.getSkills() != null && !a.getSkills().isEmpty()) {
            sb.append("Skills: ");
            boolean first = true;
            for (String s : a.getSkills()) {
                if (s == null) continue;
                String t = s.trim();
                if (t.isEmpty()) continue;
                if (!first) sb.append(", ");
                sb.append(t);
                first = false;
            }
            sb.append("\n");
        }
        if (resumePlainText != null && !resumePlainText.trim().isEmpty()) {
            sb.append("Resume:\n").append(resumePlainText.trim());
        }
        return truncateAndNormalize(sb.toString());
    }

    private static String buildJobText(Job j) {
        StringBuilder sb = new StringBuilder();
        if (j.getTitle() != null && !j.getTitle().trim().isEmpty()) {
            sb.append("Title: ").append(j.getTitle().trim()).append("\n");
        }
        if (j.getDescription() != null && !j.getDescription().trim().isEmpty()) {
            sb.append("Description:\n").append(j.getDescription().trim()).append("\n");
        }
        if (j.getRequiredSkills() != null && !j.getRequiredSkills().isEmpty()) {
            sb.append("Required skills: ");
            boolean first = true;
            for (String s : j.getRequiredSkills()) {
                if (s == null) continue;
                String t = s.trim();
                if (t.isEmpty()) continue;
                if (!first) sb.append(", ");
                sb.append(t);
                first = false;
            }
            sb.append("\n");
        }
        if (j.getType() != null && !j.getType().trim().isEmpty()) {
            sb.append("Type: ").append(j.getType().trim()).append("\n");
        }
        return truncateAndNormalize(sb.toString());
    }

    private float[] embeddingForText(String key, String model, String text, String url, String apiKey) {
        String normalized = truncateAndNormalize(text);
        String sha = HashUtil.sha256Base64(normalized + "\nMODEL=" + model);

        EmbeddingRecord hit = getCacheRecord(key);
        if (hit != null && model.equals(hit.getModel()) && sha.equals(hit.getSha256Base64())
                && hit.getEmbedding() != null && hit.getEmbedding().length > 0) {
            return hit.getEmbedding();
        }

        OpenAiCompatibleEmbeddingClient.EmbeddingResult r;
        try {
            r = client.embed(url, apiKey, model, normalized);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (IOException e) {
            return null;
        }
        if (!r.isOk()) {
            return null;
        }
        EmbeddingRecord updated = new EmbeddingRecord(key, model, sha, r.embedding, System.currentTimeMillis());
        upsertCacheRecord(updated);
        return r.embedding;
    }

    private static String truncateAndNormalize(String s) {
        if (s == null) return "";
        String t = s.trim();
        if (t.isEmpty()) return "";
        if (t.length() > MAX_TEXT_CHARS) {
            t = t.substring(0, MAX_TEXT_CHARS);
        }
        // 轻量规整：减少无意义空白（避免同内容不同空白导致重复计算）
        return t.replaceAll("[ \\t\\x0B\\f\\r]+", " ").replaceAll("\\n{3,}", "\n\n").trim();
    }

    private static EmbeddingRecord getCacheRecord(String key) {
        ensureCacheLoaded();
        synchronized (CACHE_LOCK) {
            return cache.get(key);
        }
    }

    private static void upsertCacheRecord(EmbeddingRecord record) {
        ensureCacheLoaded();
        synchronized (CACHE_LOCK) {
            cache.put(record.getKey(), record);
            persistCacheUnsafe();
        }
    }

    private static void ensureCacheLoaded() {
        if (cache != null) {
            return;
        }
        synchronized (CACHE_LOCK) {
            if (cache != null) {
                return;
            }
            Map<String, EmbeddingRecord> m = new HashMap<>();
            try {
                List<EmbeddingRecord> list = Storage.loadEmbeddings();
                for (EmbeddingRecord r : list) {
                    if (r != null && r.getKey() != null && !r.getKey().trim().isEmpty()) {
                        m.put(r.getKey().trim(), r);
                    }
                }
            } catch (IOException ignored) {
            }
            cache = m;
        }
    }

    private static void persistCacheUnsafe() {
        List<EmbeddingRecord> list = new ArrayList<>(cache.values());
        try {
            Storage.saveEmbeddings(list);
        } catch (IOException ignored) {
        }
    }
}

