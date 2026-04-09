package com.bupt.ta.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.MessageFormat;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * UTF-8 UI strings for zh / en; locale from session {@code uiLang} (set by {@link com.bupt.ta.web.filter.LocaleFilter}).
 */
public final class I18n {

    private static final Properties ZH = new Properties();
    private static final Properties EN = new Properties();

    static {
        try {
            loadProps(ZH, "/i18n/messages_zh_CN.properties");
            loadProps(EN, "/i18n/messages_en.properties");
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static void loadProps(Properties p, String classpath) throws IOException {
        try (InputStream in = I18n.class.getResourceAsStream(classpath)) {
            if (in == null) {
                throw new IOException("Missing classpath resource: " + classpath);
            }
            p.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
    }

    public static String lang(HttpServletRequest req) {
        if (req == null) {
            return "zh";
        }
        Object a = req.getAttribute("uiLang");
        if ("en".equals(a) || "zh".equals(a)) {
            return (String) a;
        }
        HttpSession s = req.getSession(false);
        if (s != null) {
            Object v = s.getAttribute("uiLang");
            if ("en".equals(v)) {
                return "en";
            }
        }
        return "zh";
    }

    public static boolean isEnglish(HttpServletRequest req) {
        return "en".equals(lang(req));
    }

    /**
     * Localized UI string; falls back to Chinese if English key missing, then to key name.
     */
    public static String msg(HttpServletRequest req, String key) {
        return msgLang(lang(req), key);
    }

    /** {@link MessageFormat} with numbered placeholders {@code {0}}, {@code {1}}, … */
    public static String msg(HttpServletRequest req, String key, Object... args) {
        String pattern = msg(req, key);
        if (args == null || args.length == 0) {
            return pattern;
        }
        return MessageFormat.format(pattern, args);
    }

    public static String msgLang(String lang, String key) {
        boolean en = "en".equals(lang);
        Properties primary = en ? EN : ZH;
        String v = primary.getProperty(key);
        if (v == null && en) {
            v = ZH.getProperty(key);
        }
        if (v == null && !en) {
            v = EN.getProperty(key);
        }
        return v != null ? v : key;
    }

    /**
     * Current URL with query string rebuilt to set {@code lang=} (for language switch links).
     */
    public static String switchLangUrl(HttpServletRequest req, String newLang) {
        if (!"en".equals(newLang) && !"zh".equals(newLang)) {
            newLang = "zh";
        }
        String uri = req.getRequestURI();
        StringBuilder q = new StringBuilder();
        String qs = req.getQueryString();
        if (qs != null && !qs.isEmpty()) {
            for (String part : qs.split("&")) {
                if (part.isEmpty() || part.startsWith("lang=")) {
                    continue;
                }
                if (q.length() > 0) {
                    q.append('&');
                }
                q.append(part);
            }
        }
        if (q.length() > 0) {
            q.append('&');
        }
        q.append("lang=").append(newLang);
        return uri + "?" + q;
    }

    public static String htmlLangAttr(HttpServletRequest req) {
        return "en".equals(lang(req)) ? "en" : "zh-CN";
    }

    /**
     * Read optional cookie without creating a session.
     */
    public static String cookieLang(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie c : cookies) {
            if ("ui_lang".equals(c.getName()) && ("en".equals(c.getValue()) || "zh".equals(c.getValue()))) {
                return c.getValue();
            }
        }
        return null;
    }
}
