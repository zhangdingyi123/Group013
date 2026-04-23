package com.bupt.ta.web;

import com.bupt.ta.model.Applicant;
import com.bupt.ta.service.ApplicantService;
import com.bupt.ta.util.I18n;
import com.bupt.ta.util.PasswordUtil;
import com.bupt.ta.util.SessionLogoutUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;

@WebServlet("/ta/auth")
public class TAAuthServlet extends HttpServlet {
    private final ApplicantService applicantService = new ApplicantService();

    /**
     * 登录成功后的安全回跳路径（站内相对路径，禁止 ..）。
     */
    static boolean isSafeTaLoginReturnUrl(String returnUrl) {
        if (returnUrl == null || returnUrl.isEmpty() || returnUrl.contains("..")) {
            return false;
        }
        return returnUrl.startsWith("/ta/") || "/assistant".equals(returnUrl);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("1".equals(req.getParameter("logout"))) {
            SessionLogoutUtil.invalidateSessionAndClearCookie(req, resp);
            resp.sendRedirect(req.getContextPath() + "/ta/auth");
            return;
        }
        req.getRequestDispatcher("/ta/auth.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");
        if ("login".equals(action)) {
            String email = req.getParameter("email");
            String password = req.getParameter("password");
            Optional<Applicant> opt = applicantService.findByEmail(email);
            if (opt.isPresent() && PasswordUtil.check(password, opt.get().getPasswordHash())) {
                HttpSession session = req.getSession(true);
                session.setAttribute("taUser", opt.get());
                String returnUrl = req.getParameter("returnUrl");
                if (isSafeTaLoginReturnUrl(returnUrl)) {
                    resp.sendRedirect(req.getContextPath() + returnUrl);
                } else {
                    resp.sendRedirect(req.getContextPath() + "/ta/dashboard");
                }
                return;
            }
            req.setAttribute("error", I18n.msg(req, "err.login.bad"));
            req.getRequestDispatcher("/ta/auth.jsp").forward(req, resp);
            return;
        }
        if ("confirm".equals(action)) {
            // 个人信息核准：校验并跳转确认页，将信息暂存 session
            String returnUrlParam = req.getParameter("returnUrl");
            String safeRegReturn = null;
            if (returnUrlParam != null && !returnUrlParam.isEmpty()
                    && returnUrlParam.startsWith("/ta/") && !returnUrlParam.contains("..")) {
                safeRegReturn = returnUrlParam;
            }
            String name = req.getParameter("name");
            String email = req.getParameter("email");
            String password = req.getParameter("password");
            String studentId = req.getParameter("studentId");
            String phone = req.getParameter("phone");
            if (name == null || name.trim().isEmpty() || email == null || email.trim().isEmpty()
                    || password == null || password.trim().isEmpty() || studentId == null || studentId.trim().isEmpty()
                    || phone == null || phone.trim().isEmpty()) {
                req.setAttribute("error", I18n.msg(req, "err.ta.fields"));
                if (safeRegReturn != null) {
                    req.setAttribute("regReturnUrl", safeRegReturn);
                }
                req.getRequestDispatcher("/ta/register.jsp").forward(req, resp);
                return;
            }
            String sid = studentId.trim();
            if (applicantService.findByStudentId(sid).isPresent()) {
                req.setAttribute("error", I18n.msg(req, "err.ta.sid.taken"));
                req.setAttribute("regName", name.trim());
                req.setAttribute("regEmail", email.trim());
                req.setAttribute("regStudentId", sid);
                req.setAttribute("regPhone", phone.trim());
                if (safeRegReturn != null) {
                    req.setAttribute("regReturnUrl", safeRegReturn);
                }
                req.getRequestDispatcher("/ta/register.jsp").forward(req, resp);
                return;
            }
            HttpSession session = req.getSession(true);
            String phoneTrim = phone.trim();
            session.setAttribute("taRegName", name.trim());
            session.setAttribute("taRegEmail", email.trim());
            session.setAttribute("taRegStudentId", sid);
            session.setAttribute("taRegPhone", phoneTrim);
            session.setAttribute("taRegPassword", password);
            req.setAttribute("regName", name.trim());
            req.setAttribute("regEmail", email.trim());
            req.setAttribute("regStudentId", sid);
            req.setAttribute("regPhone", phoneTrim);
            if (safeRegReturn != null) {
                req.setAttribute("regReturnUrl", safeRegReturn);
            }
            req.getRequestDispatcher("/ta/register_confirm.jsp").forward(req, resp);
            return;
        }
        if ("register".equals(action)) {
            // 从 session 读取核准后的信息完成注册
            HttpSession session = req.getSession(false);
            String name = session != null ? (String) session.getAttribute("taRegName") : null;
            String email = session != null ? (String) session.getAttribute("taRegEmail") : null;
            String password = session != null ? (String) session.getAttribute("taRegPassword") : null;
            String studentId = session != null ? (String) session.getAttribute("taRegStudentId") : null;
            String phone = session != null ? (String) session.getAttribute("taRegPhone") : null;
            if (session != null) {
                session.removeAttribute("taRegName");
                session.removeAttribute("taRegEmail");
                session.removeAttribute("taRegStudentId");
                session.removeAttribute("taRegPhone");
                session.removeAttribute("taRegPassword");
            }
            if (name == null || name.isEmpty() || email == null || email.isEmpty()
                    || password == null || password.isEmpty() || studentId == null || studentId.isEmpty()
                    || phone == null || phone.trim().isEmpty()) {
                req.setAttribute("error", I18n.msg(req, "err.ta.confirm.expired"));
                req.getRequestDispatcher("/ta/register.jsp").forward(req, resp);
                return;
            }
            if (applicantService.findByStudentId(studentId).isPresent()) {
                req.setAttribute("error", I18n.msg(req, "err.ta.sid.exists"));
                req.getRequestDispatcher("/ta/register.jsp").forward(req, resp);
                return;
            }
            Applicant a = applicantService.create(name, email, PasswordUtil.hash(password), studentId, phone.trim());
            if (a == null) {
                req.setAttribute("error", I18n.msg(req, "err.ta.sid.exists"));
                req.getRequestDispatcher("/ta/register.jsp").forward(req, resp);
                return;
            }
            HttpSession newSession = req.getSession(true);
            newSession.setAttribute("taUser", a);
            resp.sendRedirect(req.getContextPath() + "/ta/dashboard");
            return;
        }
        doGet(req, resp);
    }
}
