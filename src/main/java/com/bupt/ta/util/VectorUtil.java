package com.bupt.ta.util;

public final class VectorUtil {
    private VectorUtil() {}

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

    /** 将 cosine[-1,1] 映射为整数分数[0,100] */
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

