package com.bupt.ta.web;

import com.bupt.ta.model.Applicant;
import com.bupt.ta.service.ApplicantService;
import com.bupt.ta.util.I18n;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 应聘者个人中心：仅维护账号资料，岗位/简历/私信在工作台处理。
 */
@WebServlet("/ta/profile")
public class TAProfileServlet extends HttpServlet {
    private final ApplicantService applicantService = new ApplicantService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Applicant user = (Applicant) req.getSession().getAttribute("taUser");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/ta/auth");
            return;
        }
        req.setAttribute("applicant", user);
        try {
            List<String> sk = user.getSkills();
            String skillsJoined = "";
            if (sk != null && !sk.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (String s : sk) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(s);
                }
                skillsJoined = sb.toString();
            }
            req.setAttribute("applicantSkillsJoined", skillsJoined);
        } catch (Exception e) {
            req.setAttribute("error", e.getMessage());
        }
        String notice = (String) req.getSession().getAttribute("profileNotice");
        if (notice != null) {
            req.getSession().removeAttribute("profileNotice");
            req.setAttribute("profileNotice", notice);
        }
        req.getRequestDispatcher("/ta/profile.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        Applicant user = (Applicant) req.getSession().getAttribute("taUser");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/ta/auth");
            return;
        }
        String action = req.getParameter("action");
        if ("updateProfile".equals(action)) {
            String name = req.getParameter("name");
            String studentId = req.getParameter("studentId");
            String phone = req.getParameter("phone");
            String skillsStr = req.getParameter("skills");
            if (name != null) user.setName(name.trim());
            if (studentId != null) user.setStudentId(studentId.trim());
            if (phone != null) user.setPhone(phone.trim());
            if (skillsStr != null) {
                List<String> skills = new ArrayList<>();
                for (String s : skillsStr.split("[,，\\s]+")) {
                    if (!s.trim().isEmpty()) skills.add(s.trim());
                }
                user.setSkills(skills);
            }
            try {
                applicantService.update(user);
                req.getSession().setAttribute("profileNotice", I18n.msg(req, "profile.saved.ta"));
            } catch (Exception ignored) {}
        }
        resp.sendRedirect(req.getContextPath() + "/ta/profile");
    }
}
