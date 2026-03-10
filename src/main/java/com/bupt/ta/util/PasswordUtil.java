package com.bupt.ta.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public final class PasswordUtil {
    private static final String ALG = "SHA-256";

    public static String hash(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALG);
            byte[] bytes = md.digest(plain.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean check(String plain, String hashed) {
        return hash(plain).equals(hashed);
    }
}
