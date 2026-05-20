package com.bupt.ta.web;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link TAAuthServlet} 单元测试：登录回跳 URL 安全校验。
 */
class TAAuthServletTest {

    @Test
    void isSafeTaLoginReturnUrl_acceptsTaPathsAndAssistant() {
        assertTrue(TAAuthServlet.isSafeTaLoginReturnUrl("/ta/dashboard"));
        assertTrue(TAAuthServlet.isSafeTaLoginReturnUrl("/ta/dashboard?tab=jobs"));
        assertTrue(TAAuthServlet.isSafeTaLoginReturnUrl("/assistant"));
    }

    @Test
    void isSafeTaLoginReturnUrl_rejectsNullEmptyTraversalAndForeignPaths() {
        assertFalse(TAAuthServlet.isSafeTaLoginReturnUrl(null));
        assertFalse(TAAuthServlet.isSafeTaLoginReturnUrl(""));
        assertFalse(TAAuthServlet.isSafeTaLoginReturnUrl("/mo/dashboard"));
        assertFalse(TAAuthServlet.isSafeTaLoginReturnUrl("/admin/workload"));
        assertFalse(TAAuthServlet.isSafeTaLoginReturnUrl("/ta/../admin/workload"));
        assertFalse(TAAuthServlet.isSafeTaLoginReturnUrl("https://evil.example/"));
    }
}
