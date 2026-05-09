package com.bupt.ta.util;

import javax.servlet.SessionCookieConfig;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/** 退出登录时销毁服务端会话并令浏览器删除会话 Cookie。 */
public final class SessionLogoutUtil {

    private SessionLogoutUtil() {}

    public static void invalidateSessionAndClearCookie(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SessionCookieConfig cfg = req.getServletContext().getSessionCookieConfig();
        String name = cfg.getName();
        if (name == null || name.isEmpty()) {
            name = "JSESSIONID";
        }
        String path = cfg.getPath();
        if (path == null || path.isEmpty()) {
            path = req.getContextPath();
            if (path == null || path.isEmpty()) {
                path = "/";
            }
        }
        Cookie kill = new Cookie(name, "");
        kill.setPath(path);
        kill.setMaxAge(0);
        kill.setHttpOnly(true);
        if (req.isSecure()) {
            kill.setSecure(true);
        }
        String domain = cfg.getDomain();
        if (domain != null && !domain.isEmpty()) {
            kill.setDomain(domain);
        }
        resp.addCookie(kill);
    }
}
