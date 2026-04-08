package com.bupt.ta.web;

import com.bupt.ta.model.Admin;
import com.bupt.ta.service.AdminService;
import com.bupt.ta.util.I18n;
import com.bupt.ta.util.PasswordUtil;
import com.bupt.ta.util.SessionLogoutUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;

/** 管理员登录与注册，URL 映射见 web.xml */
public class AdminAuthServlet extends HttpServlet {
    private final AdminService adminService = new AdminService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("1".equals(req.getParameter("logout"))) {
            SessionLogoutUtil.invalidateSessionAndClearCookie(req, resp);
            resp.sendRedirect(req.getContextPath() + "/admin/auth");
            return;
        }
        req.getRequestDispatcher("/admin/auth.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");
        if ("login".equals(action)) {
            String email = req.getParameter("email");
            String password = req.getParameter("password");
            String emailTrim = email != null ? email.trim() : "";
            if (emailTrim.isEmpty() || password == null || password.isEmpty()) {
                req.setAttribute("error", I18n.msg(req, "err.login.fields"));
                req.getRequestDispatcher("/admin/auth.jsp").forward(req, resp);
                return;
            }
            try {
                Optional<Admin> opt = adminService.findByEmail(emailTrim);
                if (opt.isPresent() && PasswordUtil.check(password, opt.get().getPasswordHash())) {
                    HttpSession session = req.getSession(true);
                    session.setAttribute("adminUser", opt.get());
                    resp.sendRedirect(req.getContextPath() + "/admin/workload");
                    return;
                }
            } catch (IOException e) {
                req.setAttribute("error", I18n.msg(req, "err.login.fail"));
                req.getRequestDispatcher("/admin/auth.jsp").forward(req, resp);
                return;
            }
            req.setAttribute("error", I18n.msg(req, "err.login.bad"));
            req.getRequestDispatcher("/admin/auth.jsp").forward(req, resp);
            return;
        }
        if ("register".equals(action)) {
            String name = req.getParameter("name");
            String email = req.getParameter("email");
            String password = req.getParameter("password");
            if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
                req.setAttribute("error", I18n.msg(req, "err.login.fields"));
                req.getRequestDispatcher("/admin/register.jsp").forward(req, resp);
                return;
            }
            try {
                Admin admin = adminService.create(
                        name != null ? name.trim() : "",
                        email.trim(),
                        PasswordUtil.hash(password));
                if (admin == null) {
                    req.setAttribute("error", I18n.msg(req, "err.admin.registered"));
                    req.getRequestDispatcher("/admin/register.jsp").forward(req, resp);
                    return;
                }
                HttpSession session = req.getSession(true);
                session.setAttribute("adminUser", admin);
                resp.sendRedirect(req.getContextPath() + "/admin/workload");
                return;
            } catch (Exception e) {
                req.setAttribute("error", e.getMessage() != null ? e.getMessage() : I18n.msg(req, "err.admin.register.fail"));
                req.getRequestDispatcher("/admin/register.jsp").forward(req, resp);
                return;
            }
        }
        doGet(req, resp);
    }
}
