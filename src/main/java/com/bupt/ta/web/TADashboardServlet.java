package com.bupt.ta.web;

import com.bupt.ta.model.Applicant;
import com.bupt.ta.model.Application;
import com.bupt.ta.model.DirectMessage;
import com.bupt.ta.model.Job;
import com.bupt.ta.model.ModuleOrganiser;
import com.bupt.ta.service.ApplicantService;
import com.bupt.ta.service.ApplicationService;
import com.bupt.ta.service.JobService;
import com.bupt.ta.service.MatchHelper;
import com.bupt.ta.service.MessageService;
import com.bupt.ta.service.ModuleOrganiserService;

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
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
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
            resp.sendRedirect(req.getContextPath() + "/ta/auth");
            return;
        }
        if (wantsLegacyProfileTab(req)) {
            resp.sendRedirect(req.getContextPath() + "/ta/profile");
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
            Set<String> appliedJobIds = new HashSet<>();
            for (ApplicationWithJob aw : withJobs) {
                if (aw.application != null && aw.application.getJobId() != null) {
                    appliedJobIds.add(aw.application.getJobId());
                }
            }
            req.setAttribute("appliedJobIds", appliedJobIds);
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
            // 根据简历与开放岗位识别技能短板并给出提示
            String resumeText = null;
            if (Boolean.TRUE.equals(req.getAttribute("resumeIsText")) && user.getResumePath() != null) {
                try {
                    resumeText = applicantService.getResumeContent(user.getResumePath());
                } catch (Exception ignored) {}
            }
            List<String> resumeSkillGaps = matchHelper.getResumeBasedSkillGaps(user, resumeText, openJobs);
            req.setAttribute("resumeSkillGaps", resumeSkillGaps);
            List<String> resumeStrengths = matchHelper.getResumeBasedStrengths(user, resumeText, openJobs);
            req.setAttribute("resumeStrengths", resumeStrengths);
        } catch (Exception e) {
            req.setAttribute("error", e.getMessage());
        }
        String applyMessage = (String) req.getSession().getAttribute("applyMessage");
        if (applyMessage != null) {
            req.getSession().removeAttribute("applyMessage");
            req.setAttribute("applyMessage", applyMessage);
        }
        String tab = readDashboardTab(req);
        if ("messages".equals(tab)) {
            try {
                loadApplicantMessagesTab(req, user);
            } catch (Exception e) {
                req.setAttribute("error", e.getMessage());
            }
        }
        req.setAttribute("taDashboardTab", tab);
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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        Applicant user = (Applicant) req.getSession().getAttribute("taUser");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/ta/auth");
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
                        req.getSession().setAttribute("applyMessage", "您已经申请过该职位");
                    }
                } catch (Exception ignored) {}
            }
            resp.sendRedirect(dashboardUrl(req, "jobs"));
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
        Set<String> moIds = new LinkedHashSet<>(messageService.contactableMoIdsForApplicant(user.getId()));
        moIds.addAll(messageService.moIdsWithAnyMessage(user.getId()));
        List<String> sorted = new ArrayList<>(moIds);
        sorted.sort(Comparator.comparingLong((String moId) -> {
            try {
                OptionalLong t = messageService.lastMessageTime(user.getId(), moId);
                return t.isPresent() ? t.getAsLong() : 0L;
            } catch (IOException e) {
                return 0L;
            }
        }).reversed());
        List<TaMoThreadRow> rows = new ArrayList<>();
        for (String moId : sorted) {
            Optional<ModuleOrganiser> mo = moduleOrganiserService.findById(moId);
            String name = mo.map(ModuleOrganiser::getName).orElse("招聘者");
            List<DirectMessage> conv = messageService.findConversation(user.getId(), moId);
            String preview = "";
            long lastAt = 0;
            if (!conv.isEmpty()) {
                DirectMessage last = conv.get(conv.size() - 1);
                preview = MessageService.lastPreview(last.getBody(), 80);
                lastAt = last.getSentAt();
            }
            rows.add(new TaMoThreadRow(moId, name, preview, lastAt));
        }
        req.setAttribute("taDmThreads", rows);
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
        if (withMo != null && !withMo.isEmpty() && moIds.contains(withMo)) {
            req.setAttribute("taDmWithMo", withMo);
            req.setAttribute("taDmConversation", messageService.findConversation(user.getId(), withMo));
            moduleOrganiserService.findById(withMo).ifPresent(m -> req.setAttribute("taDmMo", m));
        } else if (!sorted.isEmpty()) {
            String first = sorted.get(0);
            req.setAttribute("taDmWithMo", first);
            req.setAttribute("taDmConversation", messageService.findConversation(user.getId(), first));
            moduleOrganiserService.findById(first).ifPresent(m -> req.setAttribute("taDmMo", m));
        }
        String dmNotice = (String) req.getSession().getAttribute("dmNotice");
        if (dmNotice != null) {
            req.getSession().removeAttribute("dmNotice");
            req.setAttribute("dmNotice", dmNotice);
        }
    }

    public static class TaMoThreadRow {
        private final String moId;
        private final String moName;
        private final String lastPreview;
        private final long lastAt;
        public TaMoThreadRow(String moId, String moName, String lastPreview, long lastAt) {
            this.moId = moId;
            this.moName = moName;
            this.lastPreview = lastPreview;
            this.lastAt = lastAt;
        }
        public String getMoId() { return moId; }
        public String getMoName() { return moName; }
        public String getLastPreview() { return lastPreview; }
        public long getLastAt() { return lastAt; }
        public String getLastAtText() {
            if (lastAt <= 0) {
                return "—";
            }
            return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(lastAt));
        }
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
