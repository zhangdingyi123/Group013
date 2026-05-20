package com.bupt.ta.web;

import com.bupt.ta.model.Applicant;
import com.bupt.ta.model.Application;
import com.bupt.ta.model.DirectMessage;
import com.bupt.ta.model.Job;
import com.bupt.ta.model.ModuleOrganiser;
import com.bupt.ta.service.ApplicantService;
import com.bupt.ta.service.ApplicationService;
import com.bupt.ta.service.JobService;
import com.bupt.ta.service.FriendService;
import com.bupt.ta.service.MatchHelper;
import com.bupt.ta.service.MessageService;
import com.bupt.ta.service.ModuleOrganiserService;
import com.bupt.ta.util.I18n;
import com.bupt.ta.service.assistant.AssistantConfig;
import com.bupt.ta.model.FriendRequest;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.TreeSet;

@WebServlet("/ta/dashboard")
public class TADashboardServlet extends HttpServlet {
    private static final Set<String> TA_DASHBOARD_TABS = new HashSet<>(Arrays.asList(
            "resume", "jobs", "applications", "messages"));

    private final ApplicantService applicantService = new ApplicantService();
    private final JobService jobService = new JobService();
    private final ApplicationService applicationService = new ApplicationService();
    private final MatchHelper matchHelper = new MatchHelper();
    private final MessageService messageService = new MessageService();
    private final ModuleOrganiserService moduleOrganiserService = new ModuleOrganiserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Applicant user = (Applicant) req.getSession().getAttribute("taUser");
        if (user == null) {
            handleTaGuestDashboardGet(req, resp);
            return;
        }
        if (wantsLegacyProfileTab(req)) {
            resp.sendRedirect(req.getContextPath() + "/ta/profile");
            return;
        }
        String tab = readDashboardTab(req);
        req.setAttribute("applicant", user);
        try {
            req.setAttribute("taDmTotalUnread", messageService.totalUnreadForApplicant(user.getId()));
        } catch (IOException ignored) {}
        String resumePlainText = null;
        if (user.getResumePath() != null && !user.getResumePath().isEmpty()) {
            try {
                String rp = user.getResumePath();
                boolean isText = applicantService.isResumeText(rp);
                req.setAttribute("resumeIsText", isText);
                req.setAttribute("resumeFilename", rp);
                // 用于匹配/提示：尽量抽取简历纯文本（.txt / .pdf / .doc / .docx）
                try {
                    resumePlainText = applicantService.extractResumePlainText(rp);
                } catch (Exception ignored) {}
                // 用于展示/编辑：仅 .txt 读原文
                if (isText) {
                    String content = applicantService.getResumeContent(rp);
                    resumePlainText = content != null ? content : "";
                    req.setAttribute("resumeContent", resumePlainText);
                }
            } catch (Exception ignored) {}
        }
        try {
            List<Job> allOpenJobs = jobService.findOpen();
            req.setAttribute("openJobsTotal", allOpenJobs.size());
            Set<String> dmAllowedJobIds = new HashSet<>();
            for (Job j : allOpenJobs) {
                if (j != null && j.getModuleOrganiserId() != null
                        && messageService.canApplicantContactMo(user.getId(), j.getModuleOrganiserId(), j.getId())) {
                    dmAllowedJobIds.add(j.getId());
                }
            }
            req.setAttribute("dmAllowedJobIds", dmAllowedJobIds);
            List<Application> myApplications = applicationService.findByApplicantId(user.getId());
            List<TaApplicationEntry> withJobs = new ArrayList<>();
            for (Application app : myApplications) {
                Optional<Job> j = jobService.findById(app.getJobId());
                withJobs.add(new TaApplicationEntry(app, j.orElse(null)));
            }
            req.setAttribute("myApplications", withJobs);
            Set<String> appliedJobIds = new HashSet<>();
            for (TaApplicationEntry entry : withJobs) {
                if (entry.getApp() != null && entry.getApp().getJobId() != null) {
                    appliedJobIds.add(entry.getApp().getJobId());
                }
            }
            req.setAttribute("appliedJobIds", appliedJobIds);
            if ("jobs".equals(tab)) {
                req.setAttribute("openJobs", filterTaJobsForDisplay(req, allOpenJobs, appliedJobIds, user, resumePlainText));
            } else {
                req.setAttribute("openJobs", allOpenJobs);
            }
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
            // 根据简历与开放岗位识别技能短板并给出提示（与 jobs 列表匹配分共用已读简历正文）
            List<String> resumeSkillGaps = matchHelper.getResumeBasedSkillGaps(user, resumePlainText, allOpenJobs);
            req.setAttribute("resumeSkillGaps", resumeSkillGaps);
            List<String> resumeStrengths = matchHelper.getResumeBasedStrengths(user, resumePlainText, allOpenJobs);
            req.setAttribute("resumeStrengths", resumeStrengths);
        } catch (Exception e) {
            req.setAttribute("error", e.getMessage());
        }
        String applyMessage = (String) req.getSession().getAttribute("applyMessage");
        if (applyMessage != null) {
            req.getSession().removeAttribute("applyMessage");
            req.setAttribute("applyMessage", applyMessage);
        }
        if ("messages".equals(tab)) {
            try {
                loadApplicantMessagesTab(req, user);
            } catch (Exception e) {
                req.setAttribute("error", e.getMessage());
            }
        }
        if ("resume".equals(tab)) {
            req.setAttribute("assistantKimiConfigured", !AssistantConfig.kimiApiKey().isEmpty());
            req.setAttribute("assistantQwenConfigured", !AssistantConfig.qwenApiKey().isEmpty());
            req.setAttribute("assistantOpenaiConfigured", !AssistantConfig.openaiApiKey().isEmpty());
            req.setAttribute("assistantDefaultProvider", AssistantConfig.defaultProvider());
            String rp = user.getResumePath();
            boolean okResume = rp != null && !rp.trim().isEmpty()
                    && applicantService.canAssistantReadResume(rp);
            req.setAttribute("assistantSavedResumeTxt", okResume);
        }
        req.setAttribute("taDashboardTab", tab);
        req.getRequestDispatcher("/ta/dashboard.jsp").forward(req, resp);
    }

    /**
     * 未登录访客：仅可浏览「开放岗位」；其余分区需登录后使用。
     */
    private void handleTaGuestDashboardGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (wantsLegacyProfileTab(req)) {
            resp.sendRedirect(req.getContextPath() + "/ta/auth?returnUrl="
                    + URLEncoder.encode("/ta/profile", StandardCharsets.UTF_8));
            return;
        }
        String tab = readDashboardTab(req);
        if (!"jobs".equals(tab)) {
            resp.sendRedirect(req.getContextPath() + "/ta/dashboard?tab=jobs");
            return;
        }
        req.setAttribute("taGuestMode", Boolean.TRUE);
        Applicant guestProbe = new Applicant();
        try {
            List<Job> allOpenJobs = jobService.findOpen();
            req.setAttribute("openJobsTotal", allOpenJobs.size());
            req.setAttribute("dmAllowedJobIds", Collections.emptySet());
            req.setAttribute("appliedJobIds", Collections.<String>emptySet());
            req.setAttribute("myApplications", Collections.emptyList());
            List<Job> filtered = filterTaJobsForDisplay(req, allOpenJobs,
                    Collections.<String>emptySet(), guestProbe, null);
            req.setAttribute("openJobs", filtered);
        } catch (Exception e) {
            req.setAttribute("error", e.getMessage());
        }
        req.setAttribute("taDmTotalUnread", 0);
        req.setAttribute("taDashboardTab", "jobs");
        req.getRequestDispatcher("/ta/dashboard.jsp").forward(req, resp);
    }

    /**
     * 读取当前分区 tab。部分 Tomcat 版本在带 {@link MultipartConfig} 的 Servlet 上对 GET 请求
     * {@link HttpServletRequest#getParameter(String)} 不稳定，故增加对 {@link HttpServletRequest#getQueryString()} 的解析。
     */
    private static String readDashboardTab(HttpServletRequest req) {
        String tab = normalizeTabToken(req.getParameter("tab"));
        if (tab != null) {
            return tab;
        }
        String qs = req.getQueryString();
        if (qs != null && !qs.isEmpty()) {
            for (String part : qs.split("&")) {
                if (part.isEmpty()) {
                    continue;
                }
                int eq = part.indexOf('=');
                String key = decodeQueryToken(eq < 0 ? part : part.substring(0, eq));
                if (!"tab".equals(key)) {
                    continue;
                }
                String val = eq < 0 || eq >= part.length() - 1
                        ? ""
                        : decodeQueryToken(part.substring(eq + 1));
                tab = normalizeTabToken(val);
                if (tab != null) {
                    return tab;
                }
            }
        }
        return "resume";
    }

    /** 旧链接 ?tab=profile 重定向到独立个人中心页 */
    private static boolean wantsLegacyProfileTab(HttpServletRequest req) {
        String raw = readRawTabParam(req);
        return "profile".equals(raw);
    }

    private static String readRawTabParam(HttpServletRequest req) {
        String p = req.getParameter("tab");
        if (p != null && !p.trim().isEmpty()) {
            return p.trim();
        }
        String qs = req.getQueryString();
        if (qs != null && !qs.isEmpty()) {
            for (String part : qs.split("&")) {
                if (part.isEmpty()) {
                    continue;
                }
                int eq = part.indexOf('=');
                String key = decodeQueryToken(eq < 0 ? part : part.substring(0, eq));
                if (!"tab".equals(key)) {
                    continue;
                }
                String val = eq < 0 || eq >= part.length() - 1
                        ? ""
                        : decodeQueryToken(part.substring(eq + 1));
                if (val != null && !val.trim().isEmpty()) {
                    return val.trim();
                }
            }
        }
        return null;
    }

    private static String decodeQueryToken(String raw) {
        if (raw == null || raw.isEmpty()) {
            return "";
        }
        try {
            return URLDecoder.decode(raw, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            return raw;
        }
    }

    private static String normalizeTabToken(String raw) {
        if (raw == null) {
            return null;
        }
        String t = raw.trim();
        if (t.isEmpty() || !TA_DASHBOARD_TABS.contains(t)) {
            return null;
        }
        return t;
    }

    private static String dashboardUrl(HttpServletRequest req, String tab) {
        return req.getContextPath() + "/ta/dashboard?tab=" + tab;
    }

    /** 从开放岗位聚合所需技能标签，供下拉与联想列表使用（去重、不区分大小写排序）。 */
    private static List<String> collectOpenJobSkillKeywords(List<Job> jobs) {
        Set<String> seen = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Job j : jobs) {
            if (j == null || !Job.STATUS_OPEN.equals(j.getStatus()) || j.getRequiredSkills() == null) {
                continue;
            }
            for (String s : j.getRequiredSkills()) {
                if (s != null && !s.trim().isEmpty()) {
                    seen.add(s.trim());
                }
            }
        }
        return new ArrayList<>(seen);
    }

    /** 技能筛选：按逗号、分号或空白拆成多词，任一词在岗位所需技能中子串匹配即视为命中（OR）。 */
    private static List<String> parseSkillFilterTokens(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String[] parts = raw.split("[,，;；\\s]+");
        List<String> out = new ArrayList<>();
        for (String p : parts) {
            if (p != null && !p.trim().isEmpty()) {
                out.add(p.trim());
            }
        }
        return out;
    }

    private static boolean jobMatchesSkillFilterTokens(Job j, List<String> tokens) {
        if (tokens.isEmpty()) {
            return true;
        }
        List<String> req = j.getRequiredSkills();
        if (req == null || req.isEmpty()) {
            return false;
        }
        for (String token : tokens) {
            String tl = token.toLowerCase(Locale.ROOT);
            for (String s : req) {
                if (s != null && s.toLowerCase(Locale.ROOT).contains(tl)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** 开放岗位列表：关键词、类型、技能、隐藏已投、排序；并写入 JSP 用的筛选条件属性。 */
    private List<Job> filterTaJobsForDisplay(HttpServletRequest req, List<Job> all, Set<String> appliedIds,
                                           Applicant user, String resumePlainText) {
        String jobQ = req.getParameter("jobQ");
        jobQ = jobQ != null ? jobQ.trim() : "";
        String jobType = req.getParameter("jobType");
        if (jobType == null || jobType.isEmpty()) {
            jobType = "all";
        } else if (!"all".equals(jobType) && !"course_ta".equals(jobType)
                && !"invigilation".equals(jobType) && !"activity".equals(jobType)) {
            jobType = "all";
        }
        String jobSort = req.getParameter("jobSort");
        if (jobSort == null || jobSort.isEmpty()) {
            jobSort = "newest";
        } else if (!"newest".equals(jobSort) && !"oldest".equals(jobSort) && !"title_asc".equals(jobSort)
                && !"match_desc".equals(jobSort)) {
            jobSort = "newest";
        }
        String jobSkill = req.getParameter("jobSkill");
        jobSkill = jobSkill != null ? jobSkill.trim() : "";
        boolean hideApplied = "1".equals(req.getParameter("hideApplied"));

        req.setAttribute("jobFilterQ", jobQ);
        req.setAttribute("jobFilterType", jobType);
        req.setAttribute("jobFilterSort", jobSort);
        req.setAttribute("jobFilterSkill", jobSkill);
        req.setAttribute("jobFilterHideApplied", hideApplied);
        req.setAttribute("jobSkillOptions", collectOpenJobSkillKeywords(all));

        String qLower = jobQ.toLowerCase(Locale.ROOT);
        List<String> skillTokens = parseSkillFilterTokens(jobSkill);
        List<Job> out = new ArrayList<>();
        for (Job j : all) {
            if (!Job.STATUS_OPEN.equals(j.getStatus())) {
                continue;
            }
            if (!"all".equals(jobType) && !jobType.equals(j.getType())) {
                continue;
            }
            if (hideApplied && appliedIds.contains(j.getId())) {
                continue;
            }
            if (!skillTokens.isEmpty()) {
                if (!jobMatchesSkillFilterTokens(j, skillTokens)) {
                    continue;
                }
            }
            if (!qLower.isEmpty()) {
                boolean qOk = false;
                if (j.getTitle() != null && j.getTitle().toLowerCase(Locale.ROOT).contains(qLower)) {
                    qOk = true;
                } else if (j.getDescription() != null && j.getDescription().toLowerCase(Locale.ROOT).contains(qLower)) {
                    qOk = true;
                } else if (j.getRequiredSkills() != null) {
                    for (String s : j.getRequiredSkills()) {
                        if (s != null && s.toLowerCase(Locale.ROOT).contains(qLower)) {
                            qOk = true;
                            break;
                        }
                    }
                }
                if (!qOk) {
                    continue;
                }
            }
            out.add(j);
        }
        sortTaJobs(out, jobSort, user, resumePlainText);
        Map<String, Integer> matchScores = new LinkedHashMap<>();
        for (Job j : out) {
            matchScores.put(j.getId(), matchHelper.matchScore(user, j, resumePlainText));
        }
        req.setAttribute("jobMatchScores", matchScores);
        return out;
    }

    private void sortTaJobs(List<Job> jobs, String sort, Applicant user, String resumePlainText) {
        switch (sort) {
            case "oldest":
                jobs.sort(Comparator.comparingLong(Job::getCreatedAt));
                break;
            case "title_asc":
                jobs.sort(Comparator.comparing(j -> j.getTitle() != null ? j.getTitle() : "", String.CASE_INSENSITIVE_ORDER));
                break;
            case "match_desc":
                jobs.sort(Comparator.comparingInt((Job j) -> matchHelper.matchScore(user, j, resumePlainText)).reversed());
                break;
            default:
                jobs.sort(Comparator.comparingLong(Job::getCreatedAt).reversed());
                break;
        }
    }

    /** 投递后回到「开放岗位」并保留筛选条件（含 context path，用于站内 redirect）。 */
    private static String jobsTabUrlWithFilters(HttpServletRequest req) {
        return req.getContextPath() + jobsTabRelativePathWithFilters(req);
    }

    /** 登录回跳用：相对应用根路径，不含 context path。 */
    static String jobsTabRelativePathWithFilters(HttpServletRequest req) {
        StringBuilder sb = new StringBuilder("/ta/dashboard?tab=jobs");
        String jobQ = req.getParameter("jobQ");
        if (jobQ != null && !jobQ.trim().isEmpty()) {
            sb.append("&jobQ=").append(URLEncoder.encode(jobQ.trim(), StandardCharsets.UTF_8));
        }
        String jobType = req.getParameter("jobType");
        if (jobType != null && !jobType.isEmpty() && !"all".equals(jobType)) {
            sb.append("&jobType=").append(URLEncoder.encode(jobType.trim(), StandardCharsets.UTF_8));
        }
        String jobSort = req.getParameter("jobSort");
        if (jobSort != null && !jobSort.isEmpty() && !"newest".equals(jobSort)) {
            sb.append("&jobSort=").append(URLEncoder.encode(jobSort.trim(), StandardCharsets.UTF_8));
        }
        String jobSkill = req.getParameter("jobSkill");
        if (jobSkill != null && !jobSkill.trim().isEmpty()) {
            sb.append("&jobSkill=").append(URLEncoder.encode(jobSkill.trim(), StandardCharsets.UTF_8));
        }
        if ("1".equals(req.getParameter("hideApplied"))) {
            sb.append("&hideApplied=1");
        }
        return sb.toString();
    }

    private static String taAuthReturnPathForUnauthenticatedPost(HttpServletRequest req) {
        String action = req.getParameter("action");
        if ("apply".equals(action)) {
            return jobsTabRelativePathWithFilters(req);
        }
        if ("resume".equals(action)) {
            return "/ta/dashboard?tab=resume";
        }
        if ("confirmInterview".equals(action) || "declineInterview".equals(action)
                || "requestRescheduleInterview".equals(action) || "cancelApplication".equals(action)) {
            return "/ta/dashboard?tab=applications";
        }
        if ("requestFriendMo".equals(action) || "acceptFriendRequest".equals(action) || "sendDm".equals(action)) {
            String withMo = req.getParameter("withMo");
            if (withMo != null && !withMo.trim().isEmpty()) {
                return "/ta/dashboard?tab=messages&withMo=" + URLEncoder.encode(withMo.trim(), StandardCharsets.UTF_8);
            }
            return "/ta/dashboard?tab=messages";
        }
        return "/ta/dashboard?tab=jobs";
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        Applicant user = (Applicant) req.getSession().getAttribute("taUser");
        if (user == null) {
            String path = taAuthReturnPathForUnauthenticatedPost(req);
            if (!path.startsWith("/ta/") || path.contains("..")) {
                path = "/ta/dashboard?tab=jobs";
            }
            resp.sendRedirect(req.getContextPath() + "/ta/auth?returnUrl="
                    + URLEncoder.encode(path, StandardCharsets.UTF_8));
            return;
        }
        String action = req.getParameter("action");
        if ("resume".equals(action)) {
            try {
                String content = req.getParameter("resumeContent");
                if (content != null) {
                    String path = applicantService.saveResume(user.getId(), content);
                    user.setResumePath(path);
                    applicantService.update(user);
                }
            } catch (Exception ignored) {}
            resp.sendRedirect(dashboardUrl(req, "resume"));
            return;
        }
        if ("apply".equals(action)) {
            String jobId = req.getParameter("jobId");
            String note = req.getParameter("note");
            if (jobId != null && !jobId.isEmpty()) {
                try {
                    Application applied = applicationService.apply(user.getId(), jobId, note);
                    if (applied == null && applicationService.findByApplicantAndJob(user.getId(), jobId).isPresent()) {
                        req.getSession().setAttribute("applyMessage", I18n.msg(req, "flash.apply.duplicate"));
                    }
                } catch (Exception ignored) {}
            }
            resp.sendRedirect(jobsTabUrlWithFilters(req));
            return;
        }
        if ("confirmInterview".equals(action)) {
            String applicationId = req.getParameter("applicationId");
            if (applicationId != null && !applicationId.trim().isEmpty()) {
                try {
                    applicationService.confirmInterviewByApplicant(applicationId.trim(), user.getId());
                } catch (Exception ignored) {}
            }
            resp.sendRedirect(dashboardUrl(req, "applications"));
            return;
        }
        if ("declineInterview".equals(action)) {
            String applicationId = req.getParameter("applicationId");
            if (applicationId != null && !applicationId.trim().isEmpty()) {
                try {
                    applicationService.declineInterviewByApplicant(applicationId.trim(), user.getId());
                } catch (Exception ignored) {}
            }
            resp.sendRedirect(dashboardUrl(req, "applications"));
            return;
        }
        if ("requestRescheduleInterview".equals(action)) {
            String applicationId = req.getParameter("applicationId");
            if (applicationId != null && !applicationId.trim().isEmpty()) {
                try {
                    applicationService.requestRescheduleByApplicant(applicationId.trim(), user.getId());
                } catch (Exception ignored) {}
            }
            resp.sendRedirect(dashboardUrl(req, "applications"));
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
            resp.sendRedirect(dashboardUrl(req, "applications"));
            return;
        }
        if ("requestFriendMo".equals(action)) {
            String moId = req.getParameter("moId");
            try {
                FriendService fs = new FriendService();
                boolean ok = fs.requestFromTa(user.getId(), moId != null ? moId.trim() : null);
                req.getSession().setAttribute("dmNotice", ok ? I18n.msg(req, "dm.ta.req.ok") : I18n.msg(req, "dm.ta.req.fail"));
            } catch (Exception e) {
                req.getSession().setAttribute("dmNotice", I18n.msg(req, "dm.ta.op.fail"));
            }
            resp.sendRedirect(dashboardUrl(req, "messages"));
            return;
        }
        if ("acceptFriendRequest".equals(action)) {
            String requestId = req.getParameter("requestId");
            String moId = req.getParameter("moId");
            try {
                FriendService fs = new FriendService();
                boolean ok = fs.acceptRequestAsTa(requestId, user.getId());
                req.getSession().setAttribute("dmNotice", ok ? I18n.msg(req, "dm.ta.accept.ok") : I18n.msg(req, "dm.ta.accept.fail"));
            } catch (Exception e) {
                req.getSession().setAttribute("dmNotice", I18n.msg(req, "dm.ta.op.fail"));
            }
            StringBuilder to = new StringBuilder(dashboardUrl(req, "messages"));
            if (moId != null && !moId.trim().isEmpty()) {
                to.append("&withMo=").append(URLEncoder.encode(moId.trim(), StandardCharsets.UTF_8));
            }
            resp.sendRedirect(to.toString());
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
                        req.getSession().setAttribute("dmNotice", I18n.msg(req, "dm.ta.send.fail.rule"));
                    } else {
                        req.getSession().setAttribute("dmNotice", I18n.msg(req, "dm.ta.send.ok"));
                    }
                } catch (Exception ignored) {
                    req.getSession().setAttribute("dmNotice", I18n.msg(req, "dm.ta.send.fail"));
                }
            }
            StringBuilder to = new StringBuilder(dashboardUrl(req, "messages"));
            if (moId != null && !moId.trim().isEmpty()) {
                to.append("&withMo=").append(URLEncoder.encode(moId.trim(), StandardCharsets.UTF_8));
            }
            resp.sendRedirect(to.toString());
            return;
        }
        doGet(req, resp);
    }

    /**
     * 加载应聘者私信分区所需 request 属性（工作台「私信」与个人中心共用）。
     */
    public static void loadApplicantMessagesTab(HttpServletRequest req, Applicant user) throws IOException {
        MessageService messageService = new MessageService();
        ModuleOrganiserService moduleOrganiserService = new ModuleOrganiserService();
        JobService jobService = new JobService();
        FriendService friendService = new FriendService();
        Set<String> moIds = new LinkedHashSet<>(messageService.contactableMoIdsForApplicant(user.getId()));
        List<String> sorted = new ArrayList<>(moIds);
        sorted.sort(Comparator.comparingLong((String moId) -> {
            try {
                OptionalLong t = messageService.lastMessageTime(user.getId(), moId);
                return t.isPresent() ? t.getAsLong() : 0L;
            } catch (IOException e) {
                return 0L;
            }
        }).reversed());
        String withMo = req.getParameter("withMo");
        if (withMo != null) {
            withMo = withMo.trim();
        }
        String dmJobIdParam = req.getParameter("dmJobId");
        if (dmJobIdParam != null) {
            dmJobIdParam = dmJobIdParam.trim();
        }
        if ((withMo == null || withMo.isEmpty()) && dmJobIdParam != null && !dmJobIdParam.isEmpty()) {
            Optional<Job> j = jobService.findById(dmJobIdParam);
            if (j.isPresent()) {
                withMo = j.get().getModuleOrganiserId();
            }
        }
        if (dmJobIdParam != null && !dmJobIdParam.isEmpty()) {
            req.setAttribute("taDmPrefillJobId", dmJobIdParam);
            jobService.findById(dmJobIdParam).ifPresent(j -> req.setAttribute("taDmPrefillMoId", j.getModuleOrganiserId()));
        }
        String activeMo = null;
        if (withMo != null && !withMo.isEmpty() && moIds.contains(withMo)) {
            activeMo = withMo;
        } else if (!sorted.isEmpty()) {
            activeMo = sorted.get(0);
        }
        if (activeMo != null) {
            messageService.markConversationReadByTa(user.getId(), activeMo);
        }
        List<TaMoThreadRow> rows = new ArrayList<>();
        for (String moId : sorted) {
            Optional<ModuleOrganiser> mo = moduleOrganiserService.findById(moId);
            String name = mo.map(ModuleOrganiser::getName).orElse(I18n.msg(req, "role.recruiter"));
            List<DirectMessage> conv = messageService.findConversation(user.getId(), moId);
            String preview = "";
            long lastAt = 0;
            if (!conv.isEmpty()) {
                DirectMessage last = conv.get(conv.size() - 1);
                preview = MessageService.lastPreview(last.getBody(), 80);
                lastAt = last.getSentAt();
            }
            int unread = messageService.countUnreadForTa(user.getId(), moId);
            rows.add(new TaMoThreadRow(moId, name, preview, lastAt, unread));
        }
        req.setAttribute("taDmThreads", rows);
        req.setAttribute("taDmTotalUnread", messageService.totalUnreadForApplicant(user.getId()));
        if (activeMo != null) {
            req.setAttribute("taDmWithMo", activeMo);
            req.setAttribute("taDmConversation", messageService.findConversation(user.getId(), activeMo));
            moduleOrganiserService.findById(activeMo).ifPresent(m -> req.setAttribute("taDmMo", m));
        }
        if (activeMo != null && !activeMo.isEmpty()) {
            req.setAttribute("taDmIsFriend", friendService.isFriend(user.getId(), activeMo));
            req.setAttribute("taDmHasApplicationToMo", messageService.hasNonCancelledApplicationToMo(user.getId(), activeMo));
            req.setAttribute("taDmCanRequestFriendMo",
                    !friendService.isFriend(user.getId(), activeMo)
                            && !messageService.hasNonCancelledApplicationToMo(user.getId(), activeMo));
        }
        List<FriendRequest> pendingFromMo = friendService.listPendingFromMoToApplicant(user.getId());
        List<TaFriendRequestRow> taFriendRows = new ArrayList<>();
        for (FriendRequest fr : pendingFromMo) {
            String name = moduleOrganiserService.findById(fr.getModuleOrganiserId())
                    .map(ModuleOrganiser::getName).orElse(I18n.msg(req, "role.recruiter"));
            taFriendRows.add(new TaFriendRequestRow(fr.getId(), fr.getModuleOrganiserId(), name));
        }
        req.setAttribute("taFriendRequestsFromMo", taFriendRows);
        String dmNotice = (String) req.getSession().getAttribute("dmNotice");
        if (dmNotice != null) {
            req.getSession().removeAttribute("dmNotice");
            req.setAttribute("dmNotice", dmNotice);
        }
    }

    public static class TaFriendRequestRow {
        public final String requestId;
        public final String moId;
        public final String moName;
        public TaFriendRequestRow(String requestId, String moId, String moName) {
            this.requestId = requestId;
            this.moId = moId;
            this.moName = moName;
        }
        public String getRequestId() { return requestId; }
        public String getMoId() { return moId; }
        public String getMoName() { return moName; }
    }

    public static class TaMoThreadRow {
        private final String moId;
        private final String moName;
        private final String lastPreview;
        private final long lastAt;
        private final int unreadCount;
        public TaMoThreadRow(String moId, String moName, String lastPreview, long lastAt, int unreadCount) {
            this.moId = moId;
            this.moName = moName;
            this.lastPreview = lastPreview;
            this.lastAt = lastAt;
            this.unreadCount = unreadCount;
        }
        public String getMoId() { return moId; }
        public String getMoName() { return moName; }
        public String getLastPreview() { return lastPreview; }
        public long getLastAt() { return lastAt; }
        public int getUnreadCount() { return unreadCount; }
        public String getLastAtText() {
            if (lastAt <= 0) {
                return "—";
            }
            return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(lastAt));
        }
    }

}
