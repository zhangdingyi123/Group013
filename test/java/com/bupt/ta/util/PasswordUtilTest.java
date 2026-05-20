package com.bupt.ta.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link PasswordUtil} 单元测试：加盐哈希、校验与旧版无盐兼容。
 */
class PasswordUtilTest {

    @Test
    void hashWithSaltAndCheck_roundTrip() {
        String plain = "demo123";
        String stored = PasswordUtil.hashWithSalt(plain);
        assertTrue(stored.contains(":"));
        assertTrue(PasswordUtil.checkWithSalt(plain, stored));
        assertFalse(PasswordUtil.checkWithSalt("wrong", stored));
    }

    @Test
    void checkWithSalt_rejectsNullStored() {
        assertFalse(PasswordUtil.checkWithSalt("x", null));
    }

    @Test
    void legacyHash_stillVerifiable() {
        String plain = "legacyPass";
        String hashed = PasswordUtil.hashLegacy(plain);
        assertFalse(hashed.contains(":"));
        assertTrue(PasswordUtil.checkWithSalt(plain, hashed));
    }

    @Test
    void differentSalts_produceDifferentHashes() {
        String a = PasswordUtil.hashWithSalt("same");
        String b = PasswordUtil.hashWithSalt("same");
        assertNotEquals(a, b);
        assertTrue(PasswordUtil.checkWithSalt("same", a));
        assertTrue(PasswordUtil.checkWithSalt("same", b));
    }
}
