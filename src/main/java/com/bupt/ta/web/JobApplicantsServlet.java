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
import java.io.IOException;
import java.util.List;
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
            resp.sendRedirect(req.getContextPath() + "/mo/dashboard");
            return;
        }
        try {
            Optional<Job> jobOpt = jobService.findById(jobId.trim());
            if (jobOpt.isEmpty() || !user.getId().equals(jobOpt.get().getModuleOrganiserId())) {
                req.setAttribute("error", "岗位不存在或您无权查看该岗位的应聘者。");
                req.setAttribute("job", null);
            } else {
                Job job = jobOpt.get();
                req.setAttribute("job", job);
                List<ApplicantMatch> recommended = matchHelper.recommendApplicantsForJobBalanced(jobId.trim());
                List<Application> applicationsForJob = applicationService.findByJobId(jobId.trim());
                req.setAttribute("applicantsForJob", recommended);
                req.setAttribute("applicationsForJob", applicationsForJob);
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
        if ("applicationStatus".equals(action)) {
            String appId = req.getParameter("applicationId");
            String status = req.getParameter("status");
            if (appId != null && (Application.STATUS_ACCEPTED.equals(status) || Application.STATUS_REJECTED.equals(status))) {
                try {
                    Optional<Application> appOpt = applicationService.findById(appId);
                    if (appOpt.isPresent()) {
                        String jobId = appOpt.get().getJobId();
                        Optional<Job> jobOpt = jobService.findById(jobId);
                        if (jobOpt.isPresent() && user.getId().equals(jobOpt.get().getModuleOrganiserId())) {
                            applicationService.updateStatus(appId, status);
                        }
                    }
                } catch (Exception ignored) {}
            }
            String jobId = req.getParameter("jobId");
            if (jobId != null && !jobId.trim().isEmpty()) {
                resp.sendRedirect(req.getContextPath() + "/mo/job-applicants?jobId=" + java.net.URLEncoder.encode(jobId.trim(), "UTF-8"));
                return;
            }
        }
        resp.sendRedirect(req.getContextPath() + "/mo/dashboard");
    }
}
