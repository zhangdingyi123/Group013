package com.bupt.ta.web;

import com.bupt.ta.model.Application;
import com.bupt.ta.model.Job;
import com.bupt.ta.model.ModuleOrganiser;
import com.bupt.ta.service.ApplicationService;
import com.bupt.ta.service.JobService;
import com.bupt.ta.service.MatchHelper;
import com.bupt.ta.service.MatchHelper.ApplicantMatch;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 筛选应聘者 / 系统推荐：单独页面，按岗位查看应聘者列表并录用/拒绝。
 */
@WebServlet("/mo/job-applicants")
public class JobApplicantsServlet extends HttpServlet {
    private final JobService jobService = new JobService();
    private final ApplicationService applicationService = new ApplicationService();
    private final MatchHelper matchHelper = new MatchHelper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ModuleOrganiser user = (ModuleOrganiser) req.getSession().getAttribute("moUser");
        if (user == null) {
            String returnUrl = "/mo/job-applicants?jobId=" + java.net.URLEncoder.encode(req.getParameter("jobId") != null ? req.getParameter("jobId") : "", "UTF-8");
            resp.sendRedirect(req.getContextPath() + "/mo/auth?returnUrl=" + java.net.URLEncoder.encode(returnUrl, "UTF-8"));
            return;
        }
        String jobId = req.getParameter("jobId");
        if (jobId == null || jobId.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/mo/dashboard?tab=positions");
            return;
        }
        try {
            Optional<Job> jobOpt = jobService.findById(jobId.trim());
            if (jobOpt.isEmpty() || !user.getId().equals(jobOpt.get().getModuleOrganiserId())) {
                req.setAttribute("error", "岗位不存在或您无权查看该岗位的应聘者。");
                req.setAttribute("job", null);
            } else {
                Job job = jobOpt.get();
                if (!Job.STATUS_OPEN.equals(job.getStatus())) {
                    HttpSession session = req.getSession();
                    session.setAttribute("moNotice", "该岗位已关闭，无法继续筛选应聘者。");
                    resp.sendRedirect(req.getContextPath() + "/mo/dashboard?tab=positions");
                    return;
                }
                req.setAttribute("job", job);
                List<ApplicantMatch> recommended = matchHelper.recommendApplicantsForJobBalanced(jobId.trim());
                List<Application> applicationsForJob = applicationService.findByJobId(jobId.trim());
                Map<String, Application> appByApplicantId = new HashMap<>();
                for (Application app : applicationsForJob) {
                    appByApplicantId.put(app.getApplicantId(), app);
                }
                String filter = req.getParameter("filter");
                if (filter == null || filter.isEmpty()) {
                    filter = "all";
                } else if (!"all".equals(filter) && !Application.STATUS_PENDING.equals(filter)
                        && !Application.STATUS_ACCEPTED.equals(filter) && !Application.STATUS_REJECTED.equals(filter)) {
                    filter = "all";
                }
                String q = req.getParameter("q");
                String qTrim = q != null ? q.trim() : "";
                String qLower = qTrim.toLowerCase();
                List<ApplicantMatch> shown = new ArrayList<>();
                for (ApplicantMatch m : recommended) {
                    Application app = appByApplicantId.get(m.applicant.getId());
                    String rowStatus = app != null && app.getStatus() != null ? app.getStatus() : Application.STATUS_PENDING;
                    if (!"all".equals(filter) && !filter.equals(rowStatus)) {
                        continue;
                    }
                    if (!qLower.isEmpty()) {
                        String name = m.applicant.getName() != null ? m.applicant.getName().toLowerCase() : "";
                        String email = m.applicant.getEmail() != null ? m.applicant.getEmail().toLowerCase() : "";
                        if (!name.contains(qLower) && !email.contains(qLower)) {
                            continue;
                        }
                    }
                    shown.add(m);
                }
                req.setAttribute("applicantsForJob", shown);
                req.setAttribute("totalApplicantsForJob", recommended.size());
                req.setAttribute("applicationsForJob", applicationsForJob);
                req.setAttribute("filter", filter);
                req.setAttribute("q", qTrim);
            }
        } catch (Exception e) {
            req.setAttribute("error", e.getMessage());
            req.setAttribute("job", null);
        }
        req.getRequestDispatcher("/mo/job_applicants.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        ModuleOrganiser user = (ModuleOrganiser) req.getSession().getAttribute("moUser");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/mo/auth");
            return;
        }
        String action = req.getParameter("action");
        if (!"applicationStatus".equals(action)) {
            resp.sendRedirect(req.getContextPath() + "/mo/dashboard?tab=positions");
            return;
        }
        String appId = req.getParameter("applicationId");
        String status = req.getParameter("status");
        String jobIdParam = req.getParameter("jobId");
        HttpSession session = req.getSession();
        if (appId == null || appId.trim().isEmpty()
                || (!Application.STATUS_ACCEPTED.equals(status) && !Application.STATUS_REJECTED.equals(status))) {
            redirectAfterPost(req, resp, jobIdParam);
            return;
        }
        try {
            Optional<Application> appOpt = applicationService.findById(appId.trim());
            if (appOpt.isEmpty()) {
                session.setAttribute("moNotice", "申请记录不存在。");
                resp.sendRedirect(req.getContextPath() + "/mo/dashboard?tab=positions");
                return;
            }
            String jobId = appOpt.get().getJobId();
            Optional<Job> jobOpt = jobService.findById(jobId);
            if (jobOpt.isEmpty() || !user.getId().equals(jobOpt.get().getModuleOrganiserId())) {
                session.setAttribute("moNotice", "无权处理该申请。");
                resp.sendRedirect(req.getContextPath() + "/mo/dashboard?tab=positions");
                return;
            }
            Job job = jobOpt.get();
            if (!Job.STATUS_OPEN.equals(job.getStatus())) {
                session.setAttribute("moNotice", "该岗位已关闭，无法变更申请状态。");
                resp.sendRedirect(req.getContextPath() + "/mo/dashboard?tab=positions");
                return;
            }
            applicationService.updateStatus(appId.trim(), status);
            if (Application.STATUS_ACCEPTED.equals(status)) {
                job.setStatus(Job.STATUS_CLOSED);
                jobService.update(job);
                session.setAttribute("moNotice", "已录用该应聘者，岗位已自动关闭。");
                resp.sendRedirect(req.getContextPath() + "/mo/dashboard?tab=positions");
                return;
            }
            String backJobId = (jobIdParam != null && !jobIdParam.trim().isEmpty()) ? jobIdParam.trim() : jobId;
            resp.sendRedirect(applicantsListUrl(req.getContextPath(), backJobId,
                    req.getParameter("filter"), req.getParameter("q")));
        } catch (Exception e) {
            session.setAttribute("moNotice", "操作失败，请重试。");
            resp.sendRedirect(req.getContextPath() + "/mo/dashboard?tab=positions");
        }
    }

    private static void redirectAfterPost(HttpServletRequest req, HttpServletResponse resp, String jobIdParam) throws IOException {
        String ctx = req.getContextPath();
        if (jobIdParam != null && !jobIdParam.trim().isEmpty()) {
            resp.sendRedirect(applicantsListUrl(ctx, jobIdParam.trim(), req.getParameter("filter"), req.getParameter("q")));
        } else {
            resp.sendRedirect(ctx + "/mo/dashboard?tab=positions");
        }
    }

    /** 返回筛选页 URL，保留状态与关键词（与列表 GET 一致）。 */
    private static String applicantsListUrl(String ctx, String jobId, String filter, String q) {
        StringBuilder sb = new StringBuilder(ctx).append("/mo/job-applicants?jobId=")
                .append(URLEncoder.encode(jobId, StandardCharsets.UTF_8));
        if (filter != null && !filter.isEmpty() && !"all".equals(filter)) {
            sb.append("&filter=").append(URLEncoder.encode(filter, StandardCharsets.UTF_8));
        }
        if (q != null && !q.trim().isEmpty()) {
            sb.append("&q=").append(URLEncoder.encode(q.trim(), StandardCharsets.UTF_8));
        }
        return sb.toString();
    }
}
