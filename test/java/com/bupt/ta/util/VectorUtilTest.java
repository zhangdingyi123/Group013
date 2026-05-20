package com.bupt.ta.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link VectorUtil} 单元测试：余弦相似度与 0–100 分映射。
 */
class VectorUtilTest {

    @Test
    void cosineSimilarity_identicalVectors_isOne() {
        float[] v = {1f, 0f, 0f};
        assertEquals(1.0, VectorUtil.cosineSimilarity(v, v), 1e-6);
    }

    @Test
    void cosineSimilarity_orthogonal_isZero() {
        float[] a = {1f, 0f};
        float[] b = {0f, 1f};
        assertEquals(0.0, VectorUtil.cosineSimilarity(a, b), 1e-6);
    }

    @Test
    void cosineSimilarity_invalid_returnsNaN() {
        assertTrue(Double.isNaN(VectorUtil.cosineSimilarity(null, new float[]{1f})));
        assertTrue(Double.isNaN(VectorUtil.cosineSimilarity(new float[]{1f}, new float[]{1f, 2f})));
    }

    @Test
    void cosineToScore0to100_mapsEndpoints() {
        assertEquals(0, VectorUtil.cosineToScore0to100(-1.0));
        assertEquals(100, VectorUtil.cosineToScore0to100(1.0));
        assertEquals(50, VectorUtil.cosineToScore0to100(0.0));
    }
}
