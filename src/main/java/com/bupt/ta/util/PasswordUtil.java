package com.bupt.ta.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordUtil {
    private static final String ALG = "SHA-256";
    private static final int SALT_LEN = 16;               // 16 字节随机盐
    private static final String DELIMITER = ":";
    private static final SecureRandom RANDOM = new SecureRandom();

    // 新方法：生成随机盐，返回 "盐:哈希"
    public static String hashWithSalt(String plain) {
        byte[] salt = new byte[SALT_LEN];
        RANDOM.nextBytes(salt);
        byte[] hash = sha256(concat(salt, plain.getBytes(StandardCharsets.UTF_8)));
        return Base64.getEncoder().encodeToString(salt) + DELIMITER +
                Base64.getEncoder().encodeToString(hash);
    }

    // 新方法：验证，自动兼容旧数据（无盐）
    public static boolean checkWithSalt(String plain, String stored) {
        if (stored == null) return false;
        if (!stored.contains(DELIMITER)) {
            // 旧数据兼容：使用原有的无盐验证
            return legacyCheck(plain, stored);
        }
        String[] parts = stored.split(DELIMITER, 2);
        if (parts.length != 2) return false;
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] expectedHash = Base64.getDecoder().decode(parts[1]);
        byte[] actualHash = sha256(concat(salt, plain.getBytes(StandardCharsets.UTF_8)));
        return MessageDigest.isEqual(actualHash, expectedHash);
    }

    // 旧版无盐哈希（兼容）
    public static String hashLegacy(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALG);
            byte[] bytes = md.digest(plain.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean legacyCheck(String plain, String hashed) {
        return hashLegacy(plain).equals(hashed);
    }

    private static byte[] sha256(byte[] input) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALG);
            return md.digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}
