package com.bupt.ta.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 密码哈希与校验工具。
 * <p>新注册用户采用 {@linkplain #hashWithSalt(String) 随机盐 + SHA-256}，存储格式为
 * {@code Base64(盐):Base64(哈希)}；仍兼容早期无盐的 {@linkplain #hashLegacy(String) 旧格式}。</p>
 */
public final class PasswordUtil {
    private static final String ALG = "SHA-256";
    private static final int SALT_LEN = 16;               // 16 字节随机盐
    private static final String DELIMITER = ":";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 对明文密码加盐哈希。
     *
     * @param plain 明文密码
     * @return {@code 盐:哈希} 的 Base64 编码串
     */
    public static String hashWithSalt(String plain) {
        byte[] salt = new byte[SALT_LEN];
        RANDOM.nextBytes(salt);
        byte[] hash = sha256(concat(salt, plain.getBytes(StandardCharsets.UTF_8)));
        return Base64.getEncoder().encodeToString(salt) + DELIMITER +
                Base64.getEncoder().encodeToString(hash);
    }

    /**
     * 校验明文是否与存储值匹配（自动识别加盐或无盐旧格式）。
     *
     * @param plain  用户输入的明文
     * @param stored 数据库/JSON 中的哈希字段
     * @return 匹配为 {@code true}
     */
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

    /**
     * 旧版无盐 SHA-256 哈希，仅用于兼容历史数据。
     *
     * @param plain 明文密码
     * @return Base64 编码的摘要
     */
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
