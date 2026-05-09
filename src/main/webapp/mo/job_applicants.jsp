<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.bupt.ta.util.I18n" %>
<%@ page import="com.bupt.ta.model.Job" %>
<%@ page import="com.bupt.ta.model.Application" %>
<%@ page import="com.bupt.ta.service.MatchHelper" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.List" %>
<%
    Job job = (Job) request.getAttribute("job");
    String pageError = (String) request.getAttribute("error");
    @SuppressWarnings("unchecked")
    List<MatchHelper.ApplicantMatch> applicantsForJob = (List<MatchHelper.ApplicantMatch>) request.getAttribute("applicantsForJob");
    @SuppressWarnings("unchecked")
    List<Application> applicationsForJob = (List<Application>) request.getAttribute("applicationsForJob");
    MatchHelper.JobMatchStats jobMatchStats = (MatchHelper.JobMatchStats) request.getAttribute("jobMatchStats");
    if (applicantsForJob == null) applicantsForJob = java.util.Collections.emptyList();
    if (applicationsForJob == null) applicationsForJob = java.util.Collections.emptyList();
    String pageTitle = job != null ? I18n.msg(request, "mo.ja.title.with", job.getTitle()) : I18n.msg(request, "mo.ja.title");
    String filterAttr = (String) request.getAttribute("filter");
    if (filterAttr == null) filterAttr = "all";
    String qAttr = (String) request.getAttribute("q");
    if (qAttr == null) qAttr = "";
    String qAttrEsc = qAttr.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;");
    String sortAttr = (String) request.getAttribute("sort");
    if (sortAttr == null) sortAttr = "match_desc";
    Integer minScoreAttr = (Integer) request.getAttribute("minScore");
    if (minScoreAttr == null) minScoreAttr = 0;
    Integer totalApplicantsForJob = (Integer) request.getAttribute("totalApplicantsForJob");
    if (totalApplicantsForJob == null) totalApplicantsForJob = 0;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
%>
<!DOCTYPE html>
<html lang="<%= I18n.htmlLangAttr(request) %>">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= pageTitle %> - <%= I18n.msg(request, "common.sysName") %></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=4">
</head>
<body class="mo-job-page">
    <div class="page">
        <div class="page-header">
            <a href="${pageContext.request.contextPath}/mo/dashboard?tab=positions" class="back-link"><%= I18n.msg(request, "mo.ja.back") %></a>
            <h1><%= pageTitle %></h1>
        </div>
        <% if (request.getAttribute("moNotice") != null) { %>
        <p class="msg-ok"><%= request.getAttribute("moNotice") %></p>
        <% } %>

        <% if (job == null) { %>
        <div class="section">
            <p class="empty-hint" style="margin:0;"><%= pageError != null ? pageError : I18n.msg(request, "mo.ja.err.access") %></p>
            <p style="margin-top:1rem;"><a href="${pageContext.request.contextPath}/mo/dashboard?tab=positions" class="back-link"><%= I18n.msg(request, "mo.ja.back") %></a></p>
        </div>
        <% } else { %>
        <div class="section">
            <h2><%= I18n.msg(request, "mo.ja.section.rec") %></h2>
            <p class="section-desc"><%= I18n.msg(request, "mo.ja.section.desc") %></p>
            <% if (jobMatchStats != null && jobMatchStats.total > 0) { %>
            <details class="match-stats" open>
                <summary><%= I18n.msg(request, "mo.ja.stats.summary") %></summary>
                <div class="match-stats-body">
                    <div class="match-stats-grid">
                        <div class="match-stats-card">
                            <div class="label"><%= I18n.msg(request, "mo.ja.stats.total") %></div>
                            <div class="value"><%= jobMatchStats.total %></div>
                        </div>
                        <div class="match-stats-card">
                            <div class="label"><%= I18n.msg(request, "mo.ja.stats.avg") %></div>
                            <div class="value"><%= jobMatchStats.avgScore %></div>
                        </div>
                        <div class="match-stats-card">
                            <div class="label"><%= I18n.msg(request, "mo.ja.stats.median") %></div>
                            <div class="value"><%= jobMatchStats.medianScore %></div>
                        </div>
                        <div class="match-stats-card">
                            <div class="label"><%= I18n.msg(request, "mo.ja.stats.range") %></div>
                            <div class="value"><%= jobMatchStats.maxScore %>/<%= jobMatchStats.minScore %></div>
                        </div>
                    </div>
                    <div class="match-stats-pills">
                        <span class="badge badge-accepted">90–100: <strong><%= jobMatchStats.bucket90to100 %></strong></span>
                        <span class="badge badge-open">80–89: <strong><%= jobMatchStats.bucket80to89 %></strong></span>
                        <span class="badge badge-pending">70–79: <strong><%= jobMatchStats.bucket70to79 %></strong></span>
                        <span class="badge badge-closed">60–69: <strong><%= jobMatchStats.bucket60to69 %></strong></span>
                        <span class="badge badge-rejected">0–59: <strong><%= jobMatchStats.bucket0to59 %></strong></span>
                    </div>
                    <% if (jobMatchStats.topStrengths != null && !jobMatchStats.topStrengths.isEmpty()) { %>
                    <div class="match-stats-list">
                        <div class="label"><%= I18n.msg(request, "mo.ja.stats.topStrengths") %></div>
                        <div class="skills">
                            <% for (MatchHelper.SkillCount sc : jobMatchStats.topStrengths) {
                                String sk = sc != null && sc.skill != null ? sc.skill : "";
                                String skEsc = sk.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;");
                            %>
                            <span class="badge badge-open"><%= skEsc %> · <%= I18n.msg(request, "mo.ja.stats.people", sc.count) %></span>
                            <% } %>
                        </div>
                    </div>
                    <% } %>
                    <% if (jobMatchStats.topGaps != null && !jobMatchStats.topGaps.isEmpty()) { %>
                    <div class="match-stats-list">
                        <div class="label"><%= I18n.msg(request, "mo.ja.stats.topGaps") %></div>
                        <div class="skills">
                            <% for (MatchHelper.SkillCount sc : jobMatchStats.topGaps) {
                                String sk = sc != null && sc.skill != null ? sc.skill : "";
                                String skEsc = sk.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;");
                            %>
                            <span class="badge badge-pending"><%= skEsc %> · <%= I18n.msg(request, "mo.ja.stats.people", sc.count) %></span>
                            <% } %>
                        </div>
                    </div>
                    <% } %>
                    <div class="match-stats-foot"><%= I18n.msg(request, "mo.ja.stats.note") %></div>
                </div>
            </details>
            <% } %>
            <form class="filter-bar" method="get" action="${pageContext.request.contextPath}/mo/job-applicants">
                <input type="hidden" name="jobId" value="<%= job.getId() %>">
                <div>
                    <label for="filter-status"><%= I18n.msg(request, "mo.ja.filter.status") %></label>
                    <select id="filter-status" name="filter">
                        <option value="all" <%= "all".equals(filterAttr) ? "selected" : "" %>><%= I18n.msg(request, "mo.ja.filter.all") %></option>
                        <option value="pending" <%= "pending".equals(filterAttr) ? "selected" : "" %>><%= I18n.msg(request, "mo.ja.filter.pending") %></option>
                        <option value="interview" <%= "interview".equals(filterAttr) ? "selected" : "" %>><%= I18n.msg(request, "mo.ja.filter.interview") %></option>
                        <option value="accepted" <%= "accepted".equals(filterAttr) ? "selected" : "" %>><%= I18n.msg(request, "mo.ja.filter.accepted") %></option>
                        <option value="rejected" <%= "rejected".equals(filterAttr) ? "selected" : "" %>><%= I18n.msg(request, "mo.ja.filter.rejected") %></option>
                        <option value="cancelled" <%= "cancelled".equals(filterAttr) ? "selected" : "" %>><%= I18n.msg(request, "mo.ja.filter.cancelled") %></option>
                    </select>
                </div>
                <div>
                    <label for="filter-minScore"><%= I18n.msg(request, "mo.ja.minScore") %></label>
                    <select id="filter-minScore" name="minScore">
                        <option value="0" <%= minScoreAttr == 0 ? "selected" : "" %>><%= I18n.msg(request, "mo.ja.min.none") %></option>
                        <option value="60" <%= minScoreAttr == 60 ? "selected" : "" %>>≥ 60</option>
                        <option value="70" <%= minScoreAttr == 70 ? "selected" : "" %>>≥ 70</option>
                        <option value="80" <%= minScoreAttr == 80 ? "selected" : "" %>>≥ 80</option>
                        <option value="90" <%= minScoreAttr == 90 ? "selected" : "" %>>≥ 90</option>
                    </select>
                </div>
                <div>
                    <label for="filter-sort"><%= I18n.msg(request, "mo.ja.sort") %></label>
                    <select id="filter-sort" name="sort">
                        <option value="match_desc" <%= "match_desc".equals(sortAttr) ? "selected" : "" %>><%= I18n.msg(request, "mo.ja.sort.matchDesc") %></option>
                        <option value="match_asc" <%= "match_asc".equals(sortAttr) ? "selected" : "" %>><%= I18n.msg(request, "mo.ja.sort.matchAsc") %></option>
                        <option value="time_desc" <%= "time_desc".equals(sortAttr) ? "selected" : "" %>><%= I18n.msg(request, "mo.ja.sort.timeDesc") %></option>
                        <option value="time_asc" <%= "time_asc".equals(sortAttr) ? "selected" : "" %>><%= I18n.msg(request, "mo.ja.sort.timeAsc") %></option>
                        <option value="name_asc" <%= "name_asc".equals(sortAttr) ? "selected" : "" %>><%= I18n.msg(request, "mo.ja.sort.nameAsc") %></option>
                    </select>
                </div>
                <div>
                    <label for="filter-q"><%= I18n.msg(request, "mo.ja.keyword") %></label>
                    <input type="text" id="filter-q" name="q" value="<%= qAttrEsc %>" placeholder="<%= I18n.msg(request, "mo.ja.keyword.ph") %>" autocomplete="off">
                </div>
                <button type="submit" class="btn btn-secondary btn-small"><%= I18n.msg(request, "mo.ja.applyFilter") %></button>
            </form>
            <p class="filter-meta"><%= I18n.msg(request, "mo.ja.meta", totalApplicantsForJob, applicantsForJob.size()) %></p>
            <% if (applicantsForJob.isEmpty()) { %>
            <p class="empty-hint"><%= totalApplicantsForJob > 0 ? I18n.msg(request, "mo.ja.empty.filter") : I18n.msg(request, "mo.ja.empty.none") %></p>
            <% } else { %>
            <div class="table-wrap">
            <table>
                <thead>
                    <tr><th><%= I18n.msg(request, "mo.ja.th.name") %></th><th><%= I18n.msg(request, "mo.ja.th.sid") %></th><th><%= I18n.msg(request, "mo.ja.th.email") %></th><th><%= I18n.msg(request, "mo.ja.th.phone") %></th><th><%= I18n.msg(request, "mo.ja.th.score") %></th><th><%= I18n.msg(request, "mo.ja.th.gaps") %></th><th><%= I18n.msg(request, "mo.ja.th.applied") %></th><th><%= I18n.msg(request, "mo.ja.th.ops") %></th></tr>
                </thead>
                <tbody>
                <% for (MatchHelper.ApplicantMatch m : applicantsForJob) {
                    String appId = "";
                    String status = "pending";
                    long appliedAt = 0L;
                    Application appRow = null;
                    for (Application app : applicationsForJob) {
                        if (app.getApplicantId().equals(m.applicant.getId())) {
                            appId = app.getId();
                            appRow = app;
                            status = app.getStatus() != null ? app.getStatus() : "pending";
                            appliedAt = app.getAppliedAt();
                            break;
                        }
                    }
                    String dispName = m.applicant.getName() != null ? m.applicant.getName() : "";
                    String sidDisp = m.applicant.getStudentId() != null && !m.applicant.getStudentId().isEmpty()
                            ? m.applicant.getStudentId() : I18n.msg(request, "common.dash");
                    String phoneDisp = m.applicant.getPhone() != null && !m.applicant.getPhone().trim().isEmpty()
                            ? m.applicant.getPhone().trim() : I18n.msg(request, "common.dash");
                    String confirmAcceptJs = I18n.msg(request, "mo.ja.confirm.accept", dispName)
                            .replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"").replace("\r", " ").replace("\n", "\\n");
                    String confirmAcceptShortJs = I18n.msg(request, "mo.ja.confirm.accept.short", dispName)
                            .replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"").replace("\r", " ").replace("\n", "\\n");
                    String confirmRejectJs = I18n.msg(request, "mo.ja.confirm.reject", dispName)
                            .replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"").replace("\r", " ").replace("\n", "\\n");
                %>
                    <tr>
                        <td><%= m.applicant.getName() %></td>
                        <td><%= sidDisp %></td>
                        <td><%= m.applicant.getEmail() %></td>
                        <td><%= phoneDisp %></td>
                        <td><span class="score"><%= I18n.msg(request, "mo.ja.score.pt", m.score) %></span></td>
                        <td class="gaps"><%= m.gaps != null && !m.gaps.isEmpty() ? String.join(", ", m.gaps) : I18n.msg(request, "common.none") %></td>
                        <td><%= appliedAt > 0 ? sdf.format(new Date(appliedAt)) : I18n.msg(request, "common.dash") %></td>
                        <td>
                            <% if ("pending".equals(status) && appId != null && !appId.isEmpty()) { %>
                            <form method="post" action="${pageContext.request.contextPath}/mo/job-applicants" style="display:inline;" onsubmit="return confirm('<%= confirmAcceptJs %>');">
                                <input type="hidden" name="action" value="applicationStatus">
                                <input type="hidden" name="jobId" value="<%= job.getId() %>">
                                <input type="hidden" name="applicationId" value="<%= appId %>">
                                <input type="hidden" name="status" value="accepted">
                                <input type="hidden" name="filter" value="<%= filterAttr %>">
                                <input type="hidden" name="q" value="<%= qAttrEsc %>">
                                <input type="hidden" name="sort" value="<%= sortAttr %>">
                                <input type="hidden" name="minScore" value="<%= minScoreAttr %>">
                                <button type="submit" class="btn btn-primary btn-small"><%= I18n.msg(request, "mo.ja.accept") %></button>
                            </form>
                            <form method="post" action="${pageContext.request.contextPath}/mo/job-applicants" style="display:inline;" onsubmit="return confirm('<%= confirmRejectJs %>');">
                                <input type="hidden" name="action" value="applicationStatus">
                                <input type="hidden" name="jobId" value="<%= job.getId() %>">
                                <input type="hidden" name="applicationId" value="<%= appId %>">
                                <input type="hidden" name="status" value="rejected">
                                <input type="hidden" name="filter" value="<%= filterAttr %>">
                                <input type="hidden" name="q" value="<%= qAttrEsc %>">
                                <input type="hidden" name="sort" value="<%= sortAttr %>">
                                <input type="hidden" name="minScore" value="<%= minScoreAttr %>">
                                <button type="submit" class="btn btn-secondary btn-small"><%= I18n.msg(request, "mo.ja.reject") %></button>
                            </form>
                            <details class="interview-box">
                                <summary><%= I18n.msg(request, "mo.ja.schedule") %></summary>
                                <form class="schedule-form" method="post" action="${pageContext.request.contextPath}/mo/job-applicants">
                                    <input type="hidden" name="action" value="scheduleInterview">
                                    <input type="hidden" name="jobId" value="<%= job.getId() %>">
                                    <input type="hidden" name="applicationId" value="<%= appId %>">
                                    <input type="hidden" name="filter" value="<%= filterAttr %>">
                                    <input type="hidden" name="q" value="<%= qAttrEsc %>">
                                    <input type="hidden" name="sort" value="<%= sortAttr %>">
                                    <input type="hidden" name="minScore" value="<%= minScoreAttr %>">
                                    <label for="iv-at-<%= appId %>"><%= I18n.msg(request, "mo.ja.iv.time") %></label>
                                    <input id="iv-at-<%= appId %>" type="datetime-local" name="interviewAt" required>
                                    <label for="iv-d-<%= appId %>"><%= I18n.msg(request, "mo.ja.iv.place") %></label>
                                    <textarea id="iv-d-<%= appId %>" name="interviewDetail" rows="2" placeholder="<%= I18n.msg(request, "mo.ja.iv.place.ph") %>" required></textarea>
                                    <button type="submit" class="btn btn-primary btn-small"><%= I18n.msg(request, "mo.ja.iv.mark") %></button>
                                </form>
                            </details>
                            <% } else if ("pending".equals(status)) { %>
                            <span class="empty-hint" style="padding:0;font-size:.85rem;"><%= I18n.msg(request, "mo.ja.noApp") %></span>
                            <% } else if ("interview".equals(status) && appId != null && !appId.isEmpty()) {
                                String det = appRow != null && appRow.getInterviewDetail() != null ? appRow.getInterviewDetail() : "";
                                String detEsc = det.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
                                long ivAt = appRow != null ? appRow.getInterviewAt() : 0L;
                                String ivTa = appRow != null ? appRow.getInterviewTaStatus() : Application.TA_IV_PENDING;
                                String taBadge;
                                String taBadgeClass;
                                if (Application.TA_IV_CONFIRMED.equals(ivTa)) { taBadge = I18n.msg(request, "apps.iv.ta.confirmed"); taBadgeClass = "badge-accepted"; }
                                else if (Application.TA_IV_DECLINED.equals(ivTa)) { taBadge = I18n.msg(request, "apps.iv.ta.declined"); taBadgeClass = "badge-rejected"; }
                                else if (Application.TA_IV_RESCHEDULE.equals(ivTa)) { taBadge = I18n.msg(request, "apps.iv.ta.reschedule"); taBadgeClass = "badge-interview"; }
                                else { taBadge = I18n.msg(request, "apps.iv.ta.pending"); taBadgeClass = "badge-pending"; }
                                boolean needReschedule = Application.TA_IV_DECLINED.equals(ivTa) || Application.TA_IV_RESCHEDULE.equals(ivTa);
                            %>
                            <div style="font-size:.8rem;color:#64748b;margin-bottom:.4rem;">
                                <% if (ivAt > 0) { %><strong><%= sdf.format(new Date(ivAt)) %></strong><br><% } %>
                                <span style="word-break:break-all;"><%= detEsc.isEmpty() ? I18n.msg(request, "common.dash") : detEsc %></span><br>
                                <span class="badge <%= taBadgeClass %>" style="margin-top:.25rem;"><%= taBadge %></span>
                            </div>
                            <form method="post" action="${pageContext.request.contextPath}/mo/job-applicants" style="display:inline;" onsubmit="return confirm('<%= confirmAcceptShortJs %>');">
                                <input type="hidden" name="action" value="applicationStatus">
                                <input type="hidden" name="jobId" value="<%= job.getId() %>">
                                <input type="hidden" name="applicationId" value="<%= appId %>">
                                <input type="hidden" name="status" value="accepted">
                                <input type="hidden" name="filter" value="<%= filterAttr %>">
                                <input type="hidden" name="q" value="<%= qAttrEsc %>">
                                <input type="hidden" name="sort" value="<%= sortAttr %>">
                                <input type="hidden" name="minScore" value="<%= minScoreAttr %>">
                                <button type="submit" class="btn btn-primary btn-small"><%= I18n.msg(request, "mo.ja.accept") %></button>
                            </form>
                            <form method="post" action="${pageContext.request.contextPath}/mo/job-applicants" style="display:inline;" onsubmit="return confirm('<%= confirmRejectJs %>');">
                                <input type="hidden" name="action" value="applicationStatus">
                                <input type="hidden" name="jobId" value="<%= job.getId() %>">
                                <input type="hidden" name="applicationId" value="<%= appId %>">
                                <input type="hidden" name="status" value="rejected">
                                <input type="hidden" name="filter" value="<%= filterAttr %>">
                                <input type="hidden" name="q" value="<%= qAttrEsc %>">
                                <input type="hidden" name="sort" value="<%= sortAttr %>">
                                <input type="hidden" name="minScore" value="<%= minScoreAttr %>">
                                <button type="submit" class="btn btn-secondary btn-small"><%= I18n.msg(request, "mo.ja.reject") %></button>
                            </form>
                            <% if (needReschedule) { %>
                            <details class="interview-box" style="margin-top:.5rem;">
                                <summary><%= I18n.msg(request, "mo.ja.reschedule") %></summary>
                                <form class="schedule-form" method="post" action="${pageContext.request.contextPath}/mo/job-applicants">
                                    <input type="hidden" name="action" value="scheduleInterview">
                                    <input type="hidden" name="jobId" value="<%= job.getId() %>">
                                    <input type="hidden" name="applicationId" value="<%= appId %>">
                                    <input type="hidden" name="filter" value="<%= filterAttr %>">
                                    <input type="hidden" name="q" value="<%= qAttrEsc %>">
                                    <input type="hidden" name="sort" value="<%= sortAttr %>">
                                    <input type="hidden" name="minScore" value="<%= minScoreAttr %>">
                                    <label for="iv-at-re-<%= appId %>"><%= I18n.msg(request, "mo.ja.newTime") %></label>
                                    <input id="iv-at-re-<%= appId %>" type="datetime-local" name="interviewAt" required>
                                    <label for="iv-d-re-<%= appId %>"><%= I18n.msg(request, "mo.ja.iv.place") %></label>
                                    <textarea id="iv-d-re-<%= appId %>" name="interviewDetail" rows="2" placeholder="<%= I18n.msg(request, "mo.ja.placeUpdate.ph") %>" required></textarea>
                                    <button type="submit" class="btn btn-primary btn-small"><%= I18n.msg(request, "mo.ja.saveSchedule") %></button>
                                </form>
                            </details>
                            <% } %>
                            <% } else if ("cancelled".equals(status)) { %>
                            <span class="badge badge-cancelled"><%= I18n.msg(request, "mo.ja.filter.cancelled") %></span>
                            <% } else { %>
                            <span class="badge badge-<%= "accepted".equals(status) ? "accepted" : "rejected" %>"><%= "accepted".equals(status) ? I18n.msg(request, "mo.ja.filter.accepted") : I18n.msg(request, "mo.ja.filter.rejected") %></span>
                            <% } %>
                        </td>
                    </tr>
                <% } %>
                </tbody>
            </table>
            </div>
            <% } %>
        </div>
        <% } %>
    </div>
    <script src="${pageContext.request.contextPath}/js/ui.js?v=1" defer></script>
</body>
</html>
