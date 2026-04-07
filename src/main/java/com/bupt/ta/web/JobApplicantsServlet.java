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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
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
        HttpSession session = req.getSession();
        ModuleOrganiser user = (ModuleOrganiser) session.getAttribute("moUser");
        if (user == null) {
            String returnUrl = "/mo/job-applicants?jobId=" + java.net.URLEncoder.encode(req.getParameter("jobId") != null ? req.getParameter("jobId") : "", "UTF-8");
            resp.sendRedirect(req.getContextPath() + "/mo/auth?returnUrl=" + java.net.URLEncoder.encode(returnUrl, "UTF-8"));
            return;
        }
        String moNotice = (String) session.getAttribute("moNotice");
        if (moNotice != null) {
            session.removeAttribute("moNotice");
            req.setAttribute("moNotice", moNotice);
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
                        && !Application.STATUS_INTERVIEW.equals(filter)
                        && !Application.STATUS_ACCEPTED.equals(filter) && !Application.STATUS_REJECTED.equals(filter)
                        && !Application.STATUS_CANCELLED.equals(filter)) {
                    filter = "all";
                }
                String q = req.getParameter("q");
                String qTrim = q != null ? q.trim() : "";
                String qLower = qTrim.toLowerCase();
                String sort = req.getParameter("sort");
                if (sort == null || sort.isEmpty()) {
                    sort = "match_desc";
                } else if (!"match_desc".equals(sort) && !"match_asc".equals(sort)
                        && !"time_desc".equals(sort) && !"time_asc".equals(sort)
                        && !"name_asc".equals(sort)) {
                    sort = "match_desc";
                }
                int minScore = 0;
                try {
                    String ms = req.getParameter("minScore");
                    if (ms != null && !ms.trim().isEmpty()) {
                        minScore = Math.max(0, Math.min(100, Integer.parseInt(ms.trim())));
                    }
                } catch (NumberFormatException ignored) {
                    minScore = 0;
                }
                Map<String, Long> appliedAtByApplicant = new HashMap<>();
                for (Application app : applicationsForJob) {
                    appliedAtByApplicant.put(app.getApplicantId(), app.getAppliedAt());
                }
                List<ApplicantMatch> shown = new ArrayList<>();
                for (ApplicantMatch m : recommended) {
                    Application app = appByApplicantId.get(m.applicant.getId());
                    String rowStatus = app != null && app.getStatus() != null ? app.getStatus() : Application.STATUS_PENDING;
                    if (!"all".equals(filter) && !filter.equals(rowStatus)) {
                        continue;
                    }
                    if (m.score < minScore) {
                        continue;
                    }
                    if (!qLower.isEmpty()) {
                        String name = m.applicant.getName() != null ? m.applicant.getName().toLowerCase() : "";
                        String email = m.applicant.getEmail() != null ? m.applicant.getEmail().toLowerCase() : "";
                        String sid = m.applicant.getStudentId() != null ? m.applicant.getStudentId().toLowerCase() : "";
                        if (!name.contains(qLower) && !email.contains(qLower) && !sid.contains(qLower)) {
                            continue;
                        }
                    }
                    shown.add(m);
                }
                sortApplicants(shown, sort, appliedAtByApplicant);
                req.setAttribute("applicantsForJob", shown);
                req.setAttribute("totalApplicantsForJob", recommended.size());
                req.setAttribute("applicationsForJob", applicationsForJob);
                req.setAttribute("filter", filter);
                req.setAttribute("q", qTrim);
                req.setAttribute("sort", sort);
                req.setAttribute("minScore", minScore);
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
        String jobIdParam = req.getParameter("jobId");
        HttpSession session = req.getSession();
        if ("scheduleInterview".equals(action)) {
            String appId = req.getParameter("applicationId");
            String interviewAtRaw = req.getParameter("interviewAt");
            String interviewDetail = req.getParameter("interviewDetail");
            if (appId == null || appId.trim().isEmpty()) {
                redirectAfterPost(req, resp, jobIdParam);
                return;
            }
            long atMs = parseInterviewAtMillis(interviewAtRaw);
            try {
                Optional<Application> appOpt = applicationService.findById(appId.trim());
                if (appOpt.isEmpty()) {
                    session.setAttribute("moNotice", "申请记录不存在。");
                    redirectAfterPost(req, resp, jobIdParam);
                    return;
                }
                String jobId = appOpt.get().getJobId();
                Optional<Job> jobOpt = jobService.findById(jobId);
                if (jobOpt.isEmpty() || !user.getId().equals(jobOpt.get().getModuleOrganiserId())) {
                    session.setAttribute("moNotice", "无权处理该申请。");
                    redirectAfterPost(req, resp, jobIdParam);
                    return;
                }
                Job job = jobOpt.get();
                if (!Job.STATUS_OPEN.equals(job.getStatus())) {
                    session.setAttribute("moNotice", "该岗位已关闭，无法安排面试。");
                    redirectAfterPost(req, resp, jobIdParam);
                    return;
                }
                if (atMs <= 0) {
                    session.setAttribute("moNotice", "请选择有效的面试/试讲时间。");
                } else if (!applicationService.scheduleInterview(appId.trim(), atMs, interviewDetail)) {
                    session.setAttribute("moNotice", "仅待审核的申请可设为待面试。");
                } else {
                    session.setAttribute("moNotice", "已标记为待面试，并通知应聘者查看时间与地点。");
                }
            } catch (Exception e) {
                session.setAttribute("moNotice", "操作失败，请重试。");
            }
            String backJobId = jobIdParam != null && !jobIdParam.trim().isEmpty() ? jobIdParam.trim() : null;
            if (backJobId == null || backJobId.isEmpty()) {
                try {
                    Optional<Application> ao = applicationService.findById(appId.trim());
                    if (ao.isPresent()) {
                        backJobId = ao.get().getJobId();
                    }
                } catch (IOException ignored) {}
            }
            if (backJobId != null && !backJobId.isEmpty()) {
                resp.sendRedirect(applicantsListUrl(req.getContextPath(), backJobId,
                        req.getParameter("filter"), req.getParameter("q"),
                        req.getParameter("sort"), req.getParameter("minScore")));
            } else {
                resp.sendRedirect(req.getContextPath() + "/mo/dashboard?tab=positions");
            }
            return;
        }
        if (!"applicationStatus".equals(action)) {
            resp.sendRedirect(req.getContextPath() + "/mo/dashboard?tab=positions");
            return;
        }
        String appId = req.getParameter("applicationId");
        String status = req.getParameter("status");
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
            Application currentApp = appOpt.get();
            String curSt = currentApp.getStatus();
            if (!Application.STATUS_PENDING.equals(curSt) && !Application.STATUS_INTERVIEW.equals(curSt)) {
                session.setAttribute("moNotice", "当前状态不可录用或拒绝。");
                String backJobId = (jobIdParam != null && !jobIdParam.trim().isEmpty()) ? jobIdParam.trim() : currentApp.getJobId();
                resp.sendRedirect(applicantsListUrl(req.getContextPath(), backJobId,
                        req.getParameter("filter"), req.getParameter("q"),
                        req.getParameter("sort"), req.getParameter("minScore")));
                return;
            }
            String jobId = currentApp.getJobId();
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
                    req.getParameter("filter"), req.getParameter("q"),
                    req.getParameter("sort"), req.getParameter("minScore")));
        } catch (Exception e) {
            session.setAttribute("moNotice", "操作失败，请重试。");
            resp.sendRedirect(req.getContextPath() + "/mo/dashboard?tab=positions");
        }
    }

    private static void redirectAfterPost(HttpServletRequest req, HttpServletResponse resp, String jobIdParam) throws IOException {
        String ctx = req.getContextPath();
        if (jobIdParam != null && !jobIdParam.trim().isEmpty()) {
            resp.sendRedirect(applicantsListUrl(ctx, jobIdParam.trim(), req.getParameter("filter"), req.getParameter("q"),
                    req.getParameter("sort"), req.getParameter("minScore")));
        } else {
            resp.sendRedirect(ctx + "/mo/dashboard?tab=positions");
        }
    }

    private static void sortApplicants(List<ApplicantMatch> list, String sort, Map<String, Long> appliedAtByApplicant) {
        Comparator<ApplicantMatch> primary;
        switch (sort) {
            case "match_asc":
                primary = Comparator.comparingInt(m -> m.score);
                break;
            case "time_desc":
                primary = Comparator.comparingLong((ApplicantMatch m) ->
                        appliedAtByApplicant.getOrDefault(m.applicant.getId(), 0L)).reversed();
                break;
            case "time_asc":
                primary = Comparator.comparingLong(m ->
                        appliedAtByApplicant.getOrDefault(m.applicant.getId(), 0L));
                break;
            case "name_asc":
                primary = Comparator.comparing(m -> m.applicant.getName() != null ? m.applicant.getName() : "",
                        String.CASE_INSENSITIVE_ORDER);
                break;
            default:
                primary = Comparator.comparingInt((ApplicantMatch m) -> m.score).reversed();
                break;
        }
        Comparator<ApplicantMatch> tie = Comparator.comparing(m -> m.applicant.getName() != null ? m.applicant.getName() : "",
                String.CASE_INSENSITIVE_ORDER);
        list.sort(primary.thenComparing(tie));
    }

    /** 返回筛选页 URL，保留筛选条件（与列表 GET 一致）。 */
    private static String applicantsListUrl(String ctx, String jobId, String filter, String q, String sort, String minScore) {
        StringBuilder sb = new StringBuilder(ctx).append("/mo/job-applicants?jobId=")
                .append(URLEncoder.encode(jobId, StandardCharsets.UTF_8));
        if (filter != null && !filter.isEmpty() && !"all".equals(filter)) {
            sb.append("&filter=").append(URLEncoder.encode(filter, StandardCharsets.UTF_8));
        }
        if (q != null && !q.trim().isEmpty()) {
            sb.append("&q=").append(URLEncoder.encode(q.trim(), StandardCharsets.UTF_8));
        }
        if (sort != null && !sort.isEmpty() && !"match_desc".equals(sort)) {
            sb.append("&sort=").append(URLEncoder.encode(sort, StandardCharsets.UTF_8));
        }
        if (minScore != null && !minScore.trim().isEmpty()) {
            try {
                int ms = Integer.parseInt(minScore.trim());
                if (ms > 0) {
                    sb.append("&minScore=").append(ms);
                }
            } catch (NumberFormatException ignored) { /* skip */ }
        }
        return sb.toString();
    }

    /** 解析 {@code datetime-local} 提交值（本地时区） */
    private static long parseInterviewAtMillis(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return -1;
        }
        try {
            LocalDateTime ldt = LocalDateTime.parse(raw.trim(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (DateTimeParseException e) {
            return -1;
        }
    }
}
