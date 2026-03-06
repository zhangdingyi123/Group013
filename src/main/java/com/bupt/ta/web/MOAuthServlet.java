package com.bupt.ta.web;

import com.bupt.ta.model.ModuleOrganiser;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class MOAuthServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("1".equals(req.getParameter("logout"))) {
            HttpSession s = req.getSession(false);
            if (s != null) s.invalidate();
        }
        req.getRequestDispatcher("/mo/auth.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if ("login".equals(action)) {
            String email = req.getParameter("email");
            ModuleOrganiser mo = WebApp.getMoService().findAll().stream()
                    .filter(m -> email != null && email.trim().equalsIgnoreCase(m.getEmail()))
                    .findFirst().orElse(null);
            if (mo == null) {
                req.setAttribute("error", "该邮箱未注册过。");
                req.setAttribute("promptGoRegister", Boolean.TRUE);
                req.setAttribute("tab", "login");
                req.getRequestDispatcher("/mo/auth.jsp").forward(req, resp);
                return;
            }
            HttpSession session = req.getSession(true);
            session.setAttribute("mo", mo);
            resp.sendRedirect(req.getContextPath() + "/mo/dashboard");
            return;
        }
        if ("register".equals(action)) {
            String name = req.getParameter("name");
            String email = req.getParameter("email");
            if (name == null || name.trim().isEmpty() || email == null || email.trim().isEmpty()) {
                req.setAttribute("error", "请填写姓名和邮箱。");
                req.setAttribute("tab", "register");
                req.getRequestDispatcher("/mo/auth.jsp").forward(req, resp);
                return;
            }
            if (WebApp.getMoService().findAll().stream().anyMatch(m -> email.trim().equalsIgnoreCase(m.getEmail()))) {
                req.setAttribute("error", "该邮箱已注册，请直接登录。");
                req.setAttribute("promptGoLogin", Boolean.TRUE);
                req.setAttribute("tab", "register");
                req.getRequestDispatcher("/mo/auth.jsp").forward(req, resp);
                return;
            }
            ModuleOrganiser mo = WebApp.getMoService().create(name.trim(), email.trim());
            HttpSession session = req.getSession(true);
            session.setAttribute("mo", mo);
            resp.sendRedirect(req.getContextPath() + "/mo/dashboard");
            return;
        }
        doGet(req, resp);
    }
}
