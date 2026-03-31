package com.bupt.ta.web;

import com.bupt.ta.model.Applicant;
import com.bupt.ta.model.Application;
import com.bupt.ta.model.DirectMessage;
import com.bupt.ta.model.Job;
import com.bupt.ta.service.ApplicantService;
import com.bupt.ta.service.ApplicationService;
import com.bupt.ta.service.JobService;
import com.bupt.ta.service.MessageService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 应聘者个人中心（独立于工作台子导航，右上角入口）。
 */
@WebServlet("/ta/profile")
public class TAProfileServlet extends HttpServlet {
    private final ApplicantService applicantService = new ApplicantService();
    private final JobService jobService = new JobService();
    private final ApplicationService applicationService = new ApplicationService();
    private final MessageService messageService = new MessageService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Applicant user = (Applicant) req.getSession().getAttribute("taUser");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/ta/auth");
            return;
        }
        req.setAttribute("applicant", user);
        try {
            List<Application> myApplications = applicationService.findByApplicantId(user.getId());
            List<TADashboardServlet.ApplicationWithJob> withJobs = new ArrayList<>();
            for (Application app : myApplications) {
                Optional<Job> j = jobService.findById(app.getJobId());
                withJobs.add(new TADashboardServlet.ApplicationWithJob(app, j.orElse(null)));
            }
            req.setAttribute("myApplications", withJobs);
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
            try {
                TADashboardServlet.loadApplicantMessagesTab(req, user);
            } catch (Exception ex) {
                req.setAttribute("error", ex.getMessage());
            }
        } catch (Exception e) {
            req.setAttribute("error", e.getMessage());
        }
        req.setAttribute("taDmProfileMode", Boolean.TRUE);
        req.setAttribute("applicationsPostUrl", req.getContextPath() + "/ta/profile");
        req.setAttribute("taDmPostAction", req.getContextPath() + "/ta/profile");
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
        if ("cancelApplication".equals(action)) {
            String applicationId = req.getParameter("applicationId");
            if (applicationId != null && !applicationId.isEmpty()) {
                try {
                    Optional<Application> appOpt = applicationService.findById(applicationId);
                    if (appOpt.isPresent() && user.getId().equals(appOpt.get().getApplicantId())
                            && Application.STATUS_PENDING.equals(appOpt.get().getStatus())) {
                        applicationService.updateStatus(applicationId, Application.STATUS_CANCELLED);
                    }
                } catch (Exception ignored) {}
            }
            resp.sendRedirect(req.getContextPath() + "/ta/profile");
            return;
        }
        if ("sendDm".equals(action)) {
            String moId = req.getParameter("moId");
            String body = req.getParameter("body");
            String jobId = req.getParameter("jobId");
            if (moId != null && !moId.trim().isEmpty()) {
                try {
                    DirectMessage sent = messageService.sendFromApplicant(user.getId(), moId.trim(), body,
                            jobId != null ? jobId.trim() : null);
                    if (sent == null) {
                        req.getSession().setAttribute("dmNotice", "发送失败：无权向对方发私信或内容为空。");
                    } else {
                        req.getSession().setAttribute("dmNotice", "已发送。");
                    }
                } catch (Exception ignored) {
                    req.getSession().setAttribute("dmNotice", "发送失败，请稍后重试。");
                }
            }
            StringBuilder to = new StringBuilder(req.getContextPath() + "/ta/profile");
            if (moId != null && !moId.trim().isEmpty()) {
                to.append("?withMo=").append(URLEncoder.encode(moId.trim(), StandardCharsets.UTF_8));
            }
            resp.sendRedirect(to.toString());
            return;
        }
        if ("updateProfile".equals(action)) {
            String name = req.getParameter("name");
            String studentId = req.getParameter("studentId");
            String skillsStr = req.getParameter("skills");
            if (name != null) user.setName(name.trim());
            if (studentId != null) user.setStudentId(studentId.trim());
            if (skillsStr != null) {
                List<String> skills = new ArrayList<>();
                for (String s : skillsStr.split("[,，\\s]+")) {
                    if (!s.trim().isEmpty()) skills.add(s.trim());
                }
                user.setSkills(skills);
            }
            try {
                applicantService.update(user);
                req.getSession().setAttribute("profileNotice", "资料已保存");
            } catch (Exception ignored) {}
        }
        resp.sendRedirect(req.getContextPath() + "/ta/profile");
    }
}
