package com.bupt.ta.web;

import com.bupt.ta.model.Applicant;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class TAAuthServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("1".equals(req.getParameter("logout"))) {
            HttpSession s = req.getSession(false);
            if (s != null) s.invalidate();
        }
        String uri = req.getRequestURI();
        if (uri != null && uri.endsWith("/register")) {
            req.getRequestDispatcher("/ta/register.jsp").forward(req, resp);
        } else {
            req.getRequestDispatcher("/ta/auth.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if ("login".equals(action)) {
            String email = req.getParameter("email");
            Applicant a = WebApp.getApplicantService().findByEmail(email != null ? email.trim() : "").orElse(null);
            if (a == null) {
                req.setAttribute("error", "该邮箱未注册过。");
                req.getRequestDispatcher("/ta/auth.jsp").forward(req, resp);
                return;
            }
            HttpSession session = req.getSession(true);
            session.setAttribute("ta", a);
            resp.sendRedirect(req.getContextPath() + "/ta/dashboard");
            return;
        }
        if ("register".equals(action) || req.getRequestURI() != null && req.getRequestURI().endsWith("/register")) {
            String name = req.getParameter("name");
            String email = req.getParameter("email");
            if (name == null || name.trim().isEmpty() || email == null || email.trim().isEmpty()) {
                req.setAttribute("error", "请填写用户名和邮箱。");
                req.getRequestDispatcher("/ta/register.jsp").forward(req, resp);
                return;
            }
            if (WebApp.getApplicantService().findByEmail(email.trim()).isPresent()) {
                req.setAttribute("error", "该邮箱已注册，请直接登录。");
                req.getRequestDispatcher("/ta/register.jsp").forward(req, resp);
                return;
            }
            Applicant a = WebApp.getApplicantService().create(name.trim(), email.trim());
            HttpSession session = req.getSession(true);
            session.setAttribute("ta", a);
            resp.sendRedirect(req.getContextPath() + "/ta/dashboard");
            return;
        }
        doGet(req, resp);
    }
}
