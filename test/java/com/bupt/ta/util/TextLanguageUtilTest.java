package com.bupt.ta.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link TextLanguageUtil} 单元测试：岗位文案语言检测。
 */
class TextLanguageUtilTest {

    @Test
    void containsCjkScript_detectsChinese() {
        assertTrue(TextLanguageUtil.containsCjkScript("软件工程助教"));
        assertFalse(TextLanguageUtil.containsCjkScript("Software Engineering TA"));
        assertFalse(TextLanguageUtil.containsCjkScript(null));
        assertFalse(TextLanguageUtil.containsCjkScript(""));
    }

    @Test
    void looksNonEnglishJobText_flagsCjkAndAcceptsEnglish() {
        assertTrue(TextLanguageUtil.looksNonEnglishJobText("招聘 Java 助教"));
        assertFalse(TextLanguageUtil.looksNonEnglishJobText("Hiring Java TA for lab sessions."));
        assertFalse(TextLanguageUtil.looksNonEnglishJobText(null));
    }

    @Test
    void looksNonEnglishJobText_flagsHangul() {
        assertTrue(TextLanguageUtil.looksNonEnglishJobText("조교 모집"));
    }
}
