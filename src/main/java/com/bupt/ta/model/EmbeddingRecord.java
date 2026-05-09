package com.bupt.ta.model;

/**
 * 文本向量缓存条目（持久化到 data/embeddings.json）。
 */
public class EmbeddingRecord {
    private String key;
    private String model;
    private String sha256Base64;
    private float[] embedding;
    private long updatedAt;

    public EmbeddingRecord() {}

    public EmbeddingRecord(String key, String model, String sha256Base64, float[] embedding, long updatedAt) {
        this.key = key;
        this.model = model;
        this.sha256Base64 = sha256Base64;
        this.embedding = embedding;
        this.updatedAt = updatedAt;
    }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getSha256Base64() { return sha256Base64; }
    public void setSha256Base64(String sha256Base64) { this.sha256Base64 = sha256Base64; }

    public float[] getEmbedding() { return embedding; }
    public void setEmbedding(float[] embedding) { this.embedding = embedding; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}

