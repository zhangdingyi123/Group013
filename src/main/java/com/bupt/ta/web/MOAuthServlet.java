package com.bupt.ta.web;

import com.bupt.ta.model.ModuleOrganiser;
import com.bupt.ta.service.ModuleOrganiserService;
import com.bupt.ta.util.PasswordUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;

@WebServlet("/mo/auth")
public class MOAuthServlet extends HttpServlet {
    private final ModuleOrganiserService moService = new ModuleOrganiserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("1".equals(req.getParameter("logout"))) {
            req.getSession().invalidate();
            resp.sendRedirect(req.getContextPath() + "/mo/auth");
            return;
        }
        req.getRequestDispatcher("/mo/auth.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");
        if ("login".equals(action)) {
            String email = req.getParameter("email");
            String password = req.getParameter("password");
            Optional<ModuleOrganiser> opt = moService.findByEmail(email);
            if (opt.isPresent() && PasswordUtil.check(password, opt.get().getPasswordHash())) {
                HttpSession session = req.getSession(true);
                session.setAttribute("moUser", opt.get());
                String returnUrl = req.getParameter("returnUrl");
                if (returnUrl != null && !returnUrl.isEmpty() && returnUrl.startsWith("/mo/") && !returnUrl.contains("..")) {
                    resp.sendRedirect(req.getContextPath() + returnUrl);
                } else {
                    resp.sendRedirect(req.getContextPath() + "/mo/dashboard");
                }
                return;
            }
            req.setAttribute("error", "邮箱或密码错误");
            req.getRequestDispatcher("/mo/auth.jsp").forward(req, resp);
            return;
        }
        if ("register".equals(action)) {
            String name = req.getParameter("name");
            String email = req.getParameter("email");
            String password = req.getParameter("password");
            String department = req.getParameter("department");
            if (name == null || name.trim().isEmpty() || email == null || email.trim().isEmpty()
                    || password == null || password.trim().isEmpty()) {
                req.setAttribute("error", "请填写必填项");
                req.getRequestDispatcher("/mo/register.jsp").forward(req, resp);
                return;
            }
            ModuleOrganiser mo = moService.create(name.trim(), email.trim(), PasswordUtil.hash(password),
                    department != null ? department.trim() : "");
            if (mo == null) {
                req.setAttribute("error", "该邮箱已注册");
                req.getRequestDispatcher("/mo/register.jsp").forward(req, resp);
                return;
            }
            HttpSession session = req.getSession(true);
            session.setAttribute("moUser", mo);
            resp.sendRedirect(req.getContextPath() + "/mo/dashboard");
            return;
        }
        doGet(req, resp);
    }
}
