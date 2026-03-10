package com.bupt.ta.web;

import com.bupt.ta.model.Applicant;
import com.bupt.ta.model.Application;
import com.bupt.ta.model.Job;
import com.bupt.ta.service.ApplicantService;
import com.bupt.ta.service.ApplicationService;
import com.bupt.ta.service.JobService;
import com.bupt.ta.service.MatchHelper;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@MultipartConfig(maxFileSize = 5 * 1024 * 1024, maxRequestSize = 5 * 1024 * 1024)
@WebServlet("/ta/dashboard")
public class TADashboardServlet extends HttpServlet {
    private final ApplicantService applicantService = new ApplicantService();
    private final JobService jobService = new JobService();
    private final ApplicationService applicationService = new ApplicationService();
    private final MatchHelper matchHelper = new MatchHelper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Applicant user = (Applicant) req.getSession().getAttribute("taUser");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/ta/auth");
            return;
        }
        req.setAttribute("applicant", user);
        if (user.getResumePath() != null && !user.getResumePath().isEmpty()) {
            try {
                boolean isText = applicantService.isResumeText(user.getResumePath());
                req.setAttribute("resumeIsText", isText);
                req.setAttribute("resumeFilename", user.getResumePath());
                if (isText) {
                    String content = applicantService.getResumeContent(user.getResumePath());
                    req.setAttribute("resumeContent", content != null ? content : "");
                }
            } catch (Exception ignored) {}
        }
        try {
            List<Job> openJobs = jobService.findOpen();
            req.setAttribute("openJobs", openJobs);
            List<Application> myApplications = applicationService.findByApplicantId(user.getId());
            List<ApplicationWithJob> withJobs = new ArrayList<>();
            for (Application app : myApplications) {
                Optional<Job> j = jobService.findById(app.getJobId());
                withJobs.add(new ApplicationWithJob(app, j.orElse(null)));
            }
            req.setAttribute("myApplications", withJobs);
            // 根据简历与开放岗位识别技能短板并给出提示
            String resumeText = null;
            if (Boolean.TRUE.equals(req.getAttribute("resumeIsText")) && user.getResumePath() != null) {
                try {
                    resumeText = applicantService.getResumeContent(user.getResumePath());
                } catch (Exception ignored) {}
            }
            List<String> resumeSkillGaps = matchHelper.getResumeBasedSkillGaps(user, resumeText, openJobs);
            req.setAttribute("resumeSkillGaps", resumeSkillGaps);
        } catch (Exception e) {
            req.setAttribute("error", e.getMessage());
        }
        req.getRequestDispatcher("/ta/dashboard.jsp").forward(req, resp);
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
            } catch (Exception ignored) {}
            resp.sendRedirect(req.getContextPath() + "/ta/dashboard");
            return;
        }
        if ("resume".equals(action)) {
            try {
                Part filePart = req.getPart("resumeFile");
                if (filePart != null && filePart.getSize() > 0) {
                    String fn = filePart.getSubmittedFileName();
                    if (fn != null && !fn.trim().isEmpty()) {
                        String ext = fn.contains(".") ? fn.substring(fn.lastIndexOf('.')).toLowerCase() : "";
                        if (ext.equals(".txt") || ext.equals(".pdf") || ext.equals(".doc") || ext.equals(".docx")) {
                            String saved = applicantService.saveResumeFile(user.getId(), filePart.getInputStream(), fn);
                            if (saved != null) {
                                user.setResumePath(saved);
                                applicantService.update(user);
                            }
                        }
                    }
                } else {
                    req.setCharacterEncoding("UTF-8");
                    String content = req.getParameter("resumeContent");
                    if (content != null) {
                        String path = applicantService.saveResume(user.getId(), content);
                        user.setResumePath(path);
                        applicantService.update(user);
                    }
                }
            } catch (Exception ignored) {}
            resp.sendRedirect(req.getContextPath() + "/ta/dashboard");
            return;
        }
        if ("apply".equals(action)) {
            String jobId = req.getParameter("jobId");
            String note = req.getParameter("note");
            if (jobId != null && !jobId.isEmpty()) {
                try {
                    applicationService.apply(user.getId(), jobId, note);
                } catch (Exception ignored) {}
            }
            resp.sendRedirect(req.getContextPath() + "/ta/dashboard");
            return;
        }
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
            resp.sendRedirect(req.getContextPath() + "/ta/dashboard");
            return;
        }
        doGet(req, resp);
    }

    public static class ApplicationWithJob {
        public final Application application;
        public final Job job;
        public ApplicationWithJob(Application application, Job job) {
            this.application = application;
            this.job = job;
        }
    }
}
