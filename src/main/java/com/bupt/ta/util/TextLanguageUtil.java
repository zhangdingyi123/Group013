package com.bupt.ta.util;

/**
 * Script checks for job postings that must be written in English.
 */
public final class TextLanguageUtil {

    private TextLanguageUtil() {}

    /**
     * True if the string contains at least one Han script character (common Chinese/Japanese Kanji range).
     */
    public static boolean containsCjkScript(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        return s.codePoints().anyMatch(TextLanguageUtil::isCjkUnifiedOrExtension);
    }

    private static boolean isCjkUnifiedOrExtension(int cp) {
        return (cp >= 0x4E00 && cp <= 0x9FFF)
                || (cp >= 0x3400 && cp <= 0x4DBF)
                || (cp >= 0x20000 && cp <= 0x2A6DF);
    }

    /**
     * True if the combined posting text is not acceptable as English-only (CJK, Hangul, kana, Cyrillic, Arabic, etc.).
     */
    public static boolean looksNonEnglishJobText(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        return s.codePoints().anyMatch(TextLanguageUtil::isNonEnglishScriptCodePoint);
    }

    private static boolean isNonEnglishScriptCodePoint(int cp) {
        if (isCjkUnifiedOrExtension(cp)) {
            return true;
        }
        // Hangul
        if (cp >= 0xAC00 && cp <= 0xD7AF) {
            return true;
        }
        // Hiragana + Katakana
        if (cp >= 0x3040 && cp <= 0x30FF) {
            return true;
        }
        // Cyrillic
        if (cp >= 0x0400 && cp <= 0x052F) {
            return true;
        }
        // Arabic
        if (cp >= 0x0600 && cp <= 0x06FF) {
            return true;
        }
        return false;
    }
}
