package com.bupt.ta.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public final class HashUtil {
    private static final String ALG = "SHA-256";

    private HashUtil() {}

    public static String sha256Base64(String text) {
        if (text == null) {
            text = "";
        }
        try {
            MessageDigest md = MessageDigest.getInstance(ALG);
            byte[] bytes = md.digest(text.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}

