package com.bupt.ta.web.filter;

import com.bupt.ta.util.I18n;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Sets UI language from {@code ?lang=en|zh} (then redirects to strip the param) or {@code ui_lang} cookie; stores {@code uiLang} on session and request.
 */
public class LocaleFilter implements Filter {

    private static final String ATTR = "uiLang";
    private static final String COOKIE = "ui_lang";

    @Override
    public void init(FilterConfig filterConfig) { }

    @Override
    public void destroy() { }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String langParam = req.getParameter("lang");
        if ("en".equals(langParam) || "zh".equals(langParam)) {
            persistLang(req, resp, langParam);
            String redir = buildUrlWithoutLangParam(req);
            resp.sendRedirect(redir);
            return;
        }

        String lang = I18n.cookieLang(req);
        HttpSession session = req.getSession(true);
        Object sessLang = session.getAttribute(ATTR);
        if (lang == null && (sessLang instanceof String) && ("en".equals(sessLang) || "zh".equals(sessLang))) {
            lang = (String) sessLang;
        }
        if (lang == null) {
            lang = "zh";
        }
        session.setAttribute(ATTR, lang);
        req.setAttribute(ATTR, lang);
        chain.doFilter(request, response);
    }

    private static void persistLang(HttpServletRequest req, HttpServletResponse resp, String lang) {
        HttpSession session = req.getSession(true);
        session.setAttribute(ATTR, lang);
        Cookie c = new Cookie(COOKIE, lang);
        c.setHttpOnly(false);
        c.setPath(contextPathCookiePath(req));
        c.setMaxAge(60 * 60 * 24 * 400);
        resp.addCookie(c);
    }

    private static String contextPathCookiePath(HttpServletRequest req) {
        String ctx = req.getContextPath();
        return (ctx == null || ctx.isEmpty()) ? "/" : ctx + "/";
    }

    static String buildUrlWithoutLangParam(HttpServletRequest req) {
        String uri = req.getRequestURI();
        String qs = req.getQueryString();
        if (qs == null || qs.isEmpty()) {
            return uri;
        }
        StringBuilder sb = new StringBuilder();
        for (String part : qs.split("&")) {
            if (part.isEmpty() || part.startsWith("lang=")) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append('&');
            }
            sb.append(part);
        }
        return sb.length() == 0 ? uri : uri + "?" + sb;
    }
}
