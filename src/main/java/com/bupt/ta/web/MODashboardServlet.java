package com.bupt.ta.web;

import com.bupt.ta.model.*;
import com.bupt.ta.service.ApplicantService;
import com.bupt.ta.service.FriendService;
import com.bupt.ta.service.JobService;
import com.bupt.ta.service.MessageService;
import com.bupt.ta.util.I18n;
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
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@WebServlet("/mo/dashboard")
public class MODashboardServlet extends HttpServlet {
    private static final Set<String> MO_DASHBOARD_TABS = new HashSet<>(Arrays.asList("positions", "post", "messages"));
    private final JobService jobService = new JobService();
    private final MessageService messageService = new MessageService();
    private final ApplicantService applicantService = new ApplicantService();
    private final FriendService friendService = new FriendService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ModuleOrganiser user = (ModuleOrganiser) req.getSession().getAttribute("moUser");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/mo/auth");
            return;
        }
        if (wantsLegacyProfileTab(req)) {
            resp.sendRedirect(req.getContextPath() + "/mo/profile");
            return;
        }
        req.setAttribute("mo", user);
        try {
            req.setAttribute("moDmTotalUnread", messageService.totalUnreadForMo(user.getId()));
        } catch (IOException ignored) {}
        HttpSession session = req.getSession();
        String moNotice = (String) session.getAttribute("moNotice");
        if (moNotice != null) {
            session.removeAttribute("moNotice");
            req.setAttribute("moNotice", moNotice);
        }
        try {
            List<Job> myJobs = jobService.findByModuleOrganiserId(user.getId());
            req.setAttribute("myJobs", myJobs);
        } catch (Exception e) {
            req.setAttribute("error", e.getMessage());
        }
        String tab = readMoDashboardTab(req);
        if ("messages".equals(tab)) {
            try {
                loadMoMessagesTab(req, user);
            } catch (Exception e) {
                req.setAttribute("error", e.getMessage());
            }
        }
        req.setAttribute("moDashboardTab", tab);
        req.getRequestDispatcher("/mo/dashboard.jsp").forward(req, resp);
    }

    private void loadMoMessagesTab(HttpServletRequest req, ModuleOrganiser mo) throws Exception {
        List<MessageService.ApplicantMoPair> pairs = messageService.listThreadsForMo(mo.getId());
        List<MoApplicantThreadRow> rows = new ArrayList<>();
        for (MessageService.ApplicantMoPair p : pairs) {
            String name = applicantService.findById(p.applicantId)
                    .map(Applicant::getName).orElse(I18n.msg(req, "role.default.ta"));
            List<DirectMessage> conv = messageService.findConversation(p.applicantId, mo.getId());
            String preview = "";
            long lastAt = 0;
            if (!conv.isEmpty()) {
                DirectMessage last = conv.get(conv.size() - 1);
                preview = MessageService.lastPreview(last.getBody(), 80);
                lastAt = last.getSentAt();
            }
            rows.add(new MoApplicantThreadRow(p.applicantId, name, preview, lastAt, 0));
        }
        rows.sort(Comparator.comparingLong((MoApplicantThreadRow r) -> r.lastAt).reversed());
        String withApplicant = req.getParameter("withApplicant");
        if (withApplicant != null) {
            withApplicant = withApplicant.trim();
        }
        Set<String> applicantIds = new HashSet<>();
        for (MoApplicantThreadRow r : rows) {
            applicantIds.add(r.getApplicantId());
        }
        String activeApplicant = null;
        if (withApplicant != null && !withApplicant.isEmpty() && applicantIds.contains(withApplicant)) {
            activeApplicant = withApplicant;
        } else if (!rows.isEmpty()) {
            activeApplicant = rows.get(0).getApplicantId();
        }
        if (activeApplicant != null) {
            messageService.markConversationReadByMo(activeApplicant, mo.getId());
        }
        List<MoApplicantThreadRow> rowsWithUnread = new ArrayList<>();
        for (MoApplicantThreadRow r : rows) {
            int unread = messageService.countUnreadForMo(r.getApplicantId(), mo.getId());
            rowsWithUnread.add(new MoApplicantThreadRow(r.getApplicantId(), r.getApplicantName(), r.getLastPreview(), r.getLastAt(), unread));
        }
        req.setAttribute("moDmThreads", rowsWithUnread);
        req.setAttribute("moDmTotalUnread", messageService.totalUnreadForMo(mo.getId()));
        if (activeApplicant != null) {
            req.setAttribute("moDmWithApplicant", activeApplicant);
            req.setAttribute("moDmConversation", messageService.findConversation(activeApplicant, mo.getId()));
            applicantService.findById(activeApplicant).ifPresent(a -> req.setAttribute("moDmApplicant", a));
        }
        if (activeApplicant != null && !activeApplicant.isEmpty()) {
            req.setAttribute("moDmIsFriend", friendService.isFriend(activeApplicant, mo.getId()));
            req.setAttribute("moDmHasApplicationToMo", messageService.hasNonCancelledApplicationToMo(activeApplicant, mo.getId()));
            req.setAttribute("moDmCanRequestFriendApplicant",
                    !friendService.isFriend(activeApplicant, mo.getId())
                            && !messageService.hasNonCancelledApplicationToMo(activeApplicant, mo.getId())
                            && friendService.hadAnyApplicationToMoJobs(activeApplicant, mo.getId()));
        }
        List<FriendRequest> pendingFromTa = friendService.listPendingFromTaToMo(mo.getId());
        List<MoFriendRequestRow> moFriendRows = new ArrayList<>();
        for (FriendRequest fr : pendingFromTa) {
            String name = applicantService.findById(fr.getApplicantId()).map(Applicant::getName).orElse(I18n.msg(req, "role.default.ta"));
            moFriendRows.add(new MoFriendRequestRow(fr.getId(), fr.getApplicantId(), name));
        }
        req.setAttribute("moFriendRequestsPending", moFriendRows);
        String dmNotice = (String) req.getSession().getAttribute("moDmNotice");
        if (dmNotice != null) {
            req.getSession().removeAttribute("moDmNotice");
            req.setAttribute("moDmNotice", dmNotice);
        }
    }

    public static class MoFriendRequestRow {
        public final String requestId;
        public final String applicantId;
        public final String applicantName;
        public MoFriendRequestRow(String requestId, String applicantId, String applicantName) {
            this.requestId = requestId;
            this.applicantId = applicantId;
            this.applicantName = applicantName;
        }
    }

    public static class MoApplicantThreadRow {
        private final String applicantId;
        private final String applicantName;
        private final String lastPreview;
        private final long lastAt;
        private final int unreadCount;
        public MoApplicantThreadRow(String applicantId, String applicantName, String lastPreview, long lastAt, int unreadCount) {
            this.applicantId = applicantId;
            this.applicantName = applicantName;
            this.lastPreview = lastPreview;
            this.lastAt = lastAt;
            this.unreadCount = unreadCount;
        }
        public String getApplicantId() { return applicantId; }
        public String getApplicantName() { return applicantName; }
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

    private static String readMoDashboardTab(HttpServletRequest req) {
        String tab = normalizeMoTab(req.getParameter("tab"));
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
                tab = normalizeMoTab(val);
                if (tab != null) {
                    return tab;
                }
            }
        }
        return "positions";
    }

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

    private static String normalizeMoTab(String raw) {
        if (raw == null) {
            return null;
        }
        String t = raw.trim();
        if (t.isEmpty() || !MO_DASHBOARD_TABS.contains(t)) {
            return null;
        }
        return t;
    }

    private static String moDashboardUrl(HttpServletRequest req, String tab) {
        return req.getContextPath() + "/mo/dashboard?tab=" + tab;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        HttpSession session = req.getSession();
        ModuleOrganiser user = (ModuleOrganiser) session.getAttribute("moUser");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/mo/auth");
            return;
        }
        String action = req.getParameter("action");
        if ("createJob".equals(action)) {
            String title = req.getParameter("title");
            String description = req.getParameter("description");
            String type = req.getParameter("type");
            String skillsStr = req.getParameter("requiredSkills");
            if (title != null && !title.trim().isEmpty()) {
                List<String> skills = new ArrayList<>();
                if (skillsStr != null) {
                    for (String s : skillsStr.split("[,，\\s]+")) {
                        if (!s.trim().isEmpty()) skills.add(s.trim());
                    }
                }
                try {
                    jobService.create(title.trim(), user.getId(), description != null ? description.trim() : "",
                            type != null ? type : "course_ta", skills);
                } catch (Exception ignored) {}
            }
            resp.sendRedirect(moDashboardUrl(req, "positions"));
            return;
        }
        if ("closeJob".equals(action)) {
            String jobId = req.getParameter("jobId");
            if (jobId != null) {
                try {
                    Optional<Job> j = jobService.findById(jobId);
                    if (j.isPresent() && user.getId().equals(j.get().getModuleOrganiserId())) {
                        j.get().setStatus(Job.STATUS_CLOSED);
                        jobService.update(j.get());
                    }
                } catch (Exception ignored) {}
            }
            resp.sendRedirect(moDashboardUrl(req, "positions"));
            return;
        }
        if ("acceptFriendRequest".equals(action)) {
            String requestId = req.getParameter("requestId");
            try {
                boolean ok = friendService.acceptRequestAsMo(requestId, user.getId());
                session.setAttribute("moDmNotice", ok ? I18n.msg(req, "dm.mo.accept.ok") : I18n.msg(req, "dm.mo.accept.fail"));
            } catch (Exception e) {
                session.setAttribute("moDmNotice", I18n.msg(req, "dm.ta.op.fail"));
            }
            resp.sendRedirect(moDashboardUrl(req, "messages"));
            return;
        }
        if ("requestFriendApplicant".equals(action)) {
            String applicantId = req.getParameter("applicantId");
            try {
                boolean ok = friendService.requestFromMo(user.getId(), applicantId != null ? applicantId.trim() : null);
                session.setAttribute("moDmNotice", ok ? I18n.msg(req, "dm.mo.req.ok") : I18n.msg(req, "dm.mo.req.fail"));
            } catch (Exception e) {
                session.setAttribute("moDmNotice", I18n.msg(req, "dm.ta.op.fail"));
            }
            resp.sendRedirect(moDashboardUrl(req, "messages"));
            return;
        }
        if ("sendDm".equals(action)) {
            String applicantId = req.getParameter("applicantId");
            String body = req.getParameter("body");
            if (applicantId != null && !applicantId.trim().isEmpty()) {
                try {
                    DirectMessage sent = messageService.sendFromMo(user.getId(), applicantId.trim(), body);
                    if (sent == null) {
                        session.setAttribute("moDmNotice", I18n.msg(req, "dm.mo.send.fail.rule"));
                    } else {
                        session.setAttribute("moDmNotice", I18n.msg(req, "dm.mo.send.ok"));
                    }
                } catch (Exception ignored) {
                    session.setAttribute("moDmNotice", I18n.msg(req, "dm.mo.send.fail"));
                }
            }
            StringBuilder to = new StringBuilder(req.getContextPath() + "/mo/dashboard?tab=messages");
            if (applicantId != null && !applicantId.trim().isEmpty()) {
                to.append("&withApplicant=").append(URLEncoder.encode(applicantId.trim(), StandardCharsets.UTF_8));
            }
            resp.sendRedirect(to.toString());
            return;
        }
        doGet(req, resp);
    }
}
