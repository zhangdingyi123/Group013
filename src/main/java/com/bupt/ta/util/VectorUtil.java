package com.bupt.ta.util;

/**
 * 向量运算工具，供可选的语义匹配（embeddings）使用。
 */
public final class VectorUtil {
    private VectorUtil() {}

    /**
     * 计算两向量的余弦相似度。
     *
     * @param a 向量 a
     * @param b 向量 b（长度须与 a 相同）
     * @return 相似度 ∈ [-1, 1]；参数无效时返回 {@link Double#NaN}
     */
    public static double cosineSimilarity(float[] a, float[] b) {
        if (a == null || b == null || a.length == 0 || b.length == 0 || a.length != b.length) {
            return Double.NaN;
        }
        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < a.length; i++) {
            double x = a[i];
            double y = b[i];
            dot += x * y;
            normA += x * x;
            normB += y * y;
        }
        if (normA <= 0.0 || normB <= 0.0) {
            return Double.NaN;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * 将余弦相似度 [-1, 1] 线性映射为 0–100 的整数匹配分。
     *
     * @param cosine 余弦值
     * @return 0–100；非法输入返回 0
     */
    public static int cosineToScore0to100(double cosine) {
        if (Double.isNaN(cosine) || Double.isInfinite(cosine)) {
            return 0;
        }
        if (cosine < -1.0) cosine = -1.0;
        if (cosine > 1.0) cosine = 1.0;
        double v = (cosine + 1.0) * 0.5; // 0..1
        int s = (int) Math.round(v * 100.0);
        if (s < 0) return 0;
        if (s > 100) return 100;
        return s;
    }
}

