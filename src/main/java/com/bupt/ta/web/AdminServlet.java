package com.bupt.ta.web;

import com.bupt.ta.model.Admin;
import com.bupt.ta.model.Applicant;
import com.bupt.ta.model.Application;
import com.bupt.ta.model.Job;
import com.bupt.ta.service.ApplicantService;
import com.bupt.ta.service.ApplicationService;
import com.bupt.ta.service.JobService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

/**
 * 管理员：查看助教整体工作负荷（每人被录用的岗位数）。需登录后访问。
 */
@WebServlet("/admin/workload")
public class AdminServlet extends HttpServlet {
    private final ApplicantService applicantService = new ApplicantService();
    private final ApplicationService applicationService = new ApplicationService();
    private final JobService jobService = new JobService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Admin admin = (Admin) req.getSession().getAttribute("adminUser");
        if (admin == null) {
            resp.sendRedirect(req.getContextPath() + "/admin/auth");
            return;
        }
        req.setAttribute("admin", admin);
        HttpSession session = req.getSession(false);
        if (session != null) {
            Object notice = session.getAttribute("adminNotice");
            if (notice != null) {
                req.setAttribute("notice", notice);
                session.removeAttribute("adminNotice");
            }
        }
        try {
            List<Application> all = applicationService.findAll();
            Map<String, Integer> workload = new HashMap<>();
            for (Application a : all) {
                if (Application.STATUS_ACCEPTED.equals(a.getStatus())) {
                    workload.merge(a.getApplicantId(), 1, Integer::sum);
                }
            }
            List<WorkloadEntry> entries = new ArrayList<>();
            for (Map.Entry<String, Integer> e : workload.entrySet()) {
                Applicant app = applicantService.findById(e.getKey()).orElse(null);
                entries.add(new WorkloadEntry(app != null ? app.getName() : e.getKey(), app != null ? app.getEmail() : "", e.getValue()));
            }
            entries.sort((a, b) -> Integer.compare(b.count, a.count));
            req.setAttribute("workload", entries);
            int total = entries.stream().mapToInt(e -> e.count).sum();
            req.setAttribute("totalAssignments", total);
            req.setAttribute("taCount", entries.size());

            List<AcceptedAssignment> details = new ArrayList<>();
            for (Application a : all) {
                if (!Application.STATUS_ACCEPTED.equals(a.getStatus())) {
                    continue;
                }
                Applicant ap = applicantService.findById(a.getApplicantId()).orElse(null);
                Job job = jobService.findById(a.getJobId()).orElse(null);
                details.add(new AcceptedAssignment(
                        a.getId(),
                        ap != null ? ap.getName() : a.getApplicantId(),
                        ap != null ? ap.getEmail() : "",
                        job != null ? job.getTitle() : a.getJobId()));
            }
            details.sort(Comparator.comparing((AcceptedAssignment x) -> x.applicantName).thenComparing(x -> x.jobTitle));
            req.setAttribute("acceptedDetails", details);
        } catch (Exception e) {
            req.setAttribute("error", e.getMessage());
        }
        req.getRequestDispatcher("/admin/workload.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        Admin admin = (Admin) req.getSession().getAttribute("adminUser");
        if (admin == null) {
            resp.sendRedirect(req.getContextPath() + "/admin/auth");
            return;
        }
        if (!"cancelApplication".equals(req.getParameter("action"))) {
            resp.sendRedirect(req.getContextPath() + "/admin/workload");
            return;
        }
        String applicationId = req.getParameter("applicationId");
        HttpSession session = req.getSession();
        if (applicationId == null || applicationId.trim().isEmpty()) {
            session.setAttribute("adminNotice", "缺少申请记录。");
            resp.sendRedirect(req.getContextPath() + "/admin/workload");
            return;
        }
        try {
            Optional<Application> appOpt = applicationService.findById(applicationId.trim());
            if (appOpt.isEmpty()) {
                session.setAttribute("adminNotice", "申请记录不存在。");
                resp.sendRedirect(req.getContextPath() + "/admin/workload");
                return;
            }
            Application app = appOpt.get();
            if (!Application.STATUS_ACCEPTED.equals(app.getStatus())) {
                session.setAttribute("adminNotice", "仅可对已录用状态的申请执行取消。");
                resp.sendRedirect(req.getContextPath() + "/admin/workload");
                return;
            }
            String jobId = app.getJobId();
            applicationService.updateStatus(applicationId.trim(), Application.STATUS_CANCELLED);
            reopenJobIfNoAcceptedRemain(jobId);
            session.setAttribute("adminNotice", "已强行取消该录用关系；若该岗位无其他已录用者，岗位已重新开放。");
        } catch (Exception e) {
            session.setAttribute("adminNotice", "操作失败，请重试。");
        }
        resp.sendRedirect(req.getContextPath() + "/admin/workload");
    }

    /** 当该岗位不再有任何已录用申请时，将已关闭的岗位重新开放。 */
    private void reopenJobIfNoAcceptedRemain(String jobId) throws IOException {
        List<Application> forJob = applicationService.findByJobId(jobId);
        boolean anyAccepted = forJob.stream()
                .anyMatch(a -> Application.STATUS_ACCEPTED.equals(a.getStatus()));
        if (!anyAccepted) {
            Optional<Job> j = jobService.findById(jobId);
            if (j.isPresent() && Job.STATUS_CLOSED.equals(j.get().getStatus())) {
                Job job = j.get();
                job.setStatus(Job.STATUS_OPEN);
                jobService.update(job);
            }
        }
    }

    public static class WorkloadEntry {
        public final String name;
        public final String email;
        public final int count;
        public WorkloadEntry(String name, String email, int count) {
            this.name = name;
            this.email = email;
            this.count = count;
        }
    }

    /** 一条已录用记录，供管理员强行取消。 */
    public static class AcceptedAssignment {
        public final String applicationId;
        public final String applicantName;
        public final String applicantEmail;
        public final String jobTitle;
        public AcceptedAssignment(String applicationId, String applicantName, String applicantEmail, String jobTitle) {
            this.applicationId = applicationId;
            this.applicantName = applicantName;
            this.applicantEmail = applicantEmail;
            this.jobTitle = jobTitle;
        }
    }
}
