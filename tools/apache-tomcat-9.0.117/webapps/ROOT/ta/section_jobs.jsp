<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.bupt.ta.model.Job" %>
<%@ page import="com.bupt.ta.util.I18n" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<%
    Boolean taGuestMode = (Boolean) request.getAttribute("taGuestMode");
    if (request.getAttribute("applicant") == null && !Boolean.TRUE.equals(taGuestMode)) {
        String c = request.getContextPath();
        if (session.getAttribute("taUser") != null) {
            response.sendRedirect(c + "/ta/dashboard");
        } else {
            response.sendRedirect(c + "/ta/auth");
        }
        return;
    }
    @SuppressWarnings("unchecked")
    List<Job> openJobs = (List<Job>) request.getAttribute("openJobs");
    if (openJobs == null) openJobs = java.util.Collections.emptyList();
    Integer openJobsTotal = (Integer) request.getAttribute("openJobsTotal");
    if (openJobsTotal == null) openJobsTotal = openJobs.size();
    @SuppressWarnings("unchecked")
    Set<String> appliedJobIds = (Set<String>) request.getAttribute("appliedJobIds");
    if (appliedJobIds == null) appliedJobIds = java.util.Collections.emptySet();
    @SuppressWarnings("unchecked")
    Map<String, Integer> jobMatchScores = (Map<String, Integer>) request.getAttribute("jobMatchScores");
    if (jobMatchScores == null) jobMatchScores = java.util.Collections.emptyMap();
    String jobFilterQ = (String) request.getAttribute("jobFilterQ");
    if (jobFilterQ == null) jobFilterQ = "";
    String jobFilterType = (String) request.getAttribute("jobFilterType");
    if (jobFilterType == null) jobFilterType = "all";
    String jobFilterSort = (String) request.getAttribute("jobFilterSort");
    if (jobFilterSort == null) jobFilterSort = "newest";
    String jobFilterSkill = (String) request.getAttribute("jobFilterSkill");
    if (jobFilterSkill == null) jobFilterSkill = "";
    Boolean jobFilterHideApplied = (Boolean) request.getAttribute("jobFilterHideApplied");
    boolean hideApplied = jobFilterHideApplied != null && jobFilterHideApplied;
    @SuppressWarnings("unchecked")
    Set<String> favoriteJobIds = (Set<String>) request.getAttribute("favoriteJobIds");
    if (favoriteJobIds == null) favoriteJobIds = java.util.Collections.emptySet();
    Boolean jobFilterFavoritesOnly = (Boolean) request.getAttribute("jobFilterFavoritesOnly");
    boolean favoritesOnly = jobFilterFavoritesOnly != null && jobFilterFavoritesOnly;
    Boolean semanticMatchAvailable = (Boolean) request.getAttribute("semanticMatchAvailable");
    boolean semanticEnabled = semanticMatchAvailable != null && semanticMatchAvailable;
    String qEsc = jobFilterQ.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;");
    String skillEsc = jobFilterSkill.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;");
    @SuppressWarnings("unchecked")
    java.util.List<String> jobSkillOptions = (java.util.List<String>) request.getAttribute("jobSkillOptions");
    if (jobSkillOptions == null) jobSkillOptions = java.util.Collections.emptyList();
    String ctx = request.getContextPath();
    String guestJobsReturnPath = "/ta/dashboard";
    if (request.getQueryString() != null && !request.getQueryString().trim().isEmpty()) {
        guestJobsReturnPath += "?" + request.getQueryString();
    } else {
        guestJobsReturnPath += "?tab=jobs";
    }
    String guestJobsReturnEnc = java.net.URLEncoder.encode(guestJobsReturnPath, StandardCharsets.UTF_8);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
%>
<div class="section">
    <h2><%= I18n.msg(request, "jobs.title") %></h2>
    <p class="section-desc"><%= I18n.msg(request, "jobs.desc") %></p>

    <form class="job-filter-bar" method="get" action="<%= ctx %>/ta/dashboard">
        <input type="hidden" name="tab" value="jobs">
        <div>
            <label for="job-q"><%= I18n.msg(request, "jobs.keyword") %></label>
            <input type="text" id="job-q" name="jobQ" value="<%= qEsc %>" placeholder="<%= I18n.msg(request, "jobs.keyword.ph") %>" autocomplete="off">
        </div>
        <div>
            <label for="job-type"><%= I18n.msg(request, "jobs.type") %></label>
            <select id="job-type" name="jobType">
                <option value="all" <%= "all".equals(jobFilterType) ? "selected" : "" %>><%= I18n.msg(request, "jobs.type.all") %></option>
                <option value="course_ta" <%= "course_ta".equals(jobFilterType) ? "selected" : "" %>><%= I18n.msg(request, "jobs.type.course_ta") %></option>
                <option value="invigilation" <%= "invigilation".equals(jobFilterType) ? "selected" : "" %>><%= I18n.msg(request, "jobs.type.invigilation") %></option>
                <option value="activity" <%= "activity".equals(jobFilterType) ? "selected" : "" %>><%= I18n.msg(request, "jobs.type.activity") %></option>
            </select>
        </div>
        <%
            boolean oneSkillFilter = !jobFilterSkill.isEmpty()
                    && !jobFilterSkill.contains(",") && !jobFilterSkill.contains("，")
                    && !jobFilterSkill.contains(";") && !jobFilterSkill.contains("；");
            boolean presetMatchesAny = false;
            if (oneSkillFilter) {
                String t0 = jobFilterSkill.trim();
                for (String skOpt : jobSkillOptions) {
                    if (skOpt != null && skOpt.trim().equalsIgnoreCase(t0)) {
                        presetMatchesAny = true;
                        break;
                    }
                }
            }
            boolean allSkillsPresetSelected = jobFilterSkill.isEmpty() || !oneSkillFilter || !presetMatchesAny;
        %>
        <div class="job-skill-wrap">
            <span class="job-skill-label"><%= I18n.msg(request, "jobs.skill") %></span>
            <label class="sr-only" for="job-skill-preset"><%= I18n.msg(request, "jobs.skill.preset") %></label>
            <select id="job-skill-preset" class="job-skill-preset" title="<%= I18n.msg(request, "jobs.skill.preset.title") %>">
                <option value="" <%= allSkillsPresetSelected ? "selected" : "" %>><%= I18n.msg(request, "jobs.skill.all") %></option>
                <% for (String skOpt : jobSkillOptions) {
                    if (skOpt == null) continue;
                    String skTrim = skOpt.trim();
                    if (skTrim.isEmpty()) continue;
                    String skAttr = skTrim.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;");
                    boolean presetSel = oneSkillFilter && presetMatchesAny && skTrim.equalsIgnoreCase(jobFilterSkill.trim());
                %>
                <option value="<%= skAttr %>" <%= presetSel ? "selected" : "" %>><%= skAttr %></option>
                <% } %>
            </select>
            <label for="job-skill"><%= I18n.msg(request, "jobs.skill.fuzzy") %></label>
            <input type="text" id="job-skill" name="jobSkill" value="<%= skillEsc %>" placeholder="<%= I18n.msg(request, "jobs.skill.fuzzy.ph") %>" autocomplete="off" list="job-skill-datalist">
            <datalist id="job-skill-datalist">
                <% for (String skOpt : jobSkillOptions) {
                    if (skOpt == null) continue;
                    String skTrim = skOpt.trim();
                    if (skTrim.isEmpty()) continue;
                    String skAttr = skTrim.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;");
                %><option value="<%= skAttr %>"></option><% } %>
            </datalist>
        </div>
        <script>
        (function () {
            var preset = document.getElementById('job-skill-preset');
            var skillInp = document.getElementById('job-skill');
            if (preset && skillInp) {
                preset.addEventListener('change', function () {
                    skillInp.value = this.value || '';
                });
            }
        })();
        </script>
        <div>
            <label for="job-sort"><%= I18n.msg(request, "jobs.sort") %></label>
            <select id="job-sort" name="jobSort">
                <option value="newest" <%= "newest".equals(jobFilterSort) ? "selected" : "" %>><%= I18n.msg(request, "jobs.sort.newest") %></option>
                <option value="oldest" <%= "oldest".equals(jobFilterSort) ? "selected" : "" %>><%= I18n.msg(request, "jobs.sort.oldest") %></option>
                <option value="match_desc" <%= "match_desc".equals(jobFilterSort) ? "selected" : "" %>><%= I18n.msg(request, "jobs.sort.matchDesc") %></option>
                <option value="title_asc" <%= "title_asc".equals(jobFilterSort) ? "selected" : "" %>><%= I18n.msg(request, "jobs.sort.titleAsc") %></option>
            </select>
        </div>
        <% if (!Boolean.TRUE.equals(taGuestMode)) { %>
        <div class="chk">
            <input type="checkbox" id="hide-applied" name="hideApplied" value="1" <%= hideApplied ? "checked" : "" %>>
            <label for="hide-applied"><%= I18n.msg(request, "jobs.hideApplied") %></label>
        </div>
        <% } %>
        <button type="submit" class="btn btn-secondary btn-small"><%= I18n.msg(request, "common.apply") %></button>
        <a href="<%= ctx %>/ta/dashboard?tab=jobs" class="btn btn-secondary btn-small" style="text-decoration:none;display:inline-block;line-height:1.25"><%= I18n.msg(request, "jobs.reset") %></a>
    </form>
    <p class="job-filter-meta"><%= I18n.msg(request, "jobs.meta", openJobsTotal, openJobs.size()) %></p>

    <% @SuppressWarnings("unchecked") java.util.List<Job> recommendedJobs = (java.util.List<Job>) request.getAttribute("recommendedJobs");
       if (recommendedJobs != null && !recommendedJobs.isEmpty()) { %>
    <div class="job-recommend-section">
        <h3><%= I18n.msg(request, "jobs.recommended.title") %></h3>
        <p class="section-desc"><%= I18n.msg(request, "jobs.recommended.desc") %></p>
        <div class="recommended-grid">
            <% for (Job rec : recommendedJobs) {
                String title = rec.getTitle() != null ? rec.getTitle() : I18n.msg(request, "jobs.unnamed");
                Integer recScore = jobMatchScores.get(rec.getId());
            %>
            <a class="recommended-card" href="<%= ctx %>/ta/dashboard?tab=jobs&jobQ=<%= java.net.URLEncoder.encode(title, java.nio.charset.StandardCharsets.UTF_8) %>">
                <strong><%= title %></strong>
                <span><%= I18n.msg(request, "jobs.match", recScore != null ? I18n.msg(request, "jobs.score.points", recScore) : I18n.msg(request, "common.dash")) %></span>
            </a>
            <% } %>
        </div>
    </div>
    <% } %>

    <% if (favoritesOnly) { %>
    <p class="section-desc job-favorites-note"><%= I18n.msg(request, "jobs.favorites.filterActive") %></p>
    <% } %>
    <% if (openJobsTotal == 0) { %>
    <p class="empty-hint"><%= I18n.msg(request, "jobs.empty.none") %></p>
    <% } else if (openJobs.isEmpty()) { %>
    <p class="empty-hint"><%= I18n.msg(request, "jobs.empty.filtered") %></p>
    <% } else { %>
    <% for (Job j : openJobs) {
        if (!Job.STATUS_OPEN.equals(j.getStatus())) continue;
        boolean already = appliedJobIds.contains(j.getId());
        boolean favorited = favoriteJobIds.contains(j.getId());
        String typeLabel = "-";
        if ("course_ta".equals(j.getType())) typeLabel = I18n.msg(request, "jobs.type.course_ta");
        else if ("invigilation".equals(j.getType())) typeLabel = I18n.msg(request, "jobs.type.invigilation");
        else if ("activity".equals(j.getType())) typeLabel = I18n.msg(request, "jobs.type.activity");
        else if (j.getType() != null) typeLabel = j.getType();
        Integer mscore = jobMatchScores.get(j.getId());
        String scoreLabel = mscore != null ? I18n.msg(request, "jobs.score.points", mscore) : I18n.msg(request, "common.dash");
    %>
    <div class="job-card">
        <h3><%= j.getTitle() != null ? j.getTitle() : I18n.msg(request, "jobs.unnamed") %>
            <% if (mscore != null) { %><span class="job-match-pill" title="<%= I18n.msg(request, "jobs.match.title") %>"><%= I18n.msg(request, "jobs.match", scoreLabel) %></span><% } %>
            <% if (semanticEnabled && mscore != null) { %><span class="job-match-hint"><%= I18n.msg(request, "jobs.semanticMatch") %></span><% } %>
        </h3>
        <p class="section-desc job-type-label" style="margin-bottom:.35rem">
            <span class="badge badge-open"><%= I18n.msg(request, "common.open") %></span>
            &nbsp;·&nbsp;<%= typeLabel %>
            <% if (j.getCreatedAt() > 0) { %>&nbsp;·&nbsp;<%= I18n.msg(request, "jobs.published") %> <%= sdf.format(new Date(j.getCreatedAt())) %><% } %>
        </p>
        <% if (j.getDescription() != null && !j.getDescription().isEmpty()) { %>
        <p class="section-desc"><%= j.getDescription() %></p>
        <% } %>
        <% if (j.getRequiredSkills() != null && !j.getRequiredSkills().isEmpty()) { %>
        <p class="section-desc"><%= I18n.msg(request, "jobs.required") %> <%= String.join("、", j.getRequiredSkills()) %></p>
        <% } %>
        <p style="margin:.5rem 0 0">
            <% if (Boolean.TRUE.equals(taGuestMode)) {
                String dmRet = "/ta/dashboard?tab=messages&dmJobId=" + j.getId();
                String dmEsc = java.net.URLEncoder.encode(dmRet, java.nio.charset.StandardCharsets.UTF_8);
            %>
            <a href="<%= ctx %>/ta/auth?returnUrl=<%= dmEsc %>" class="btn btn-secondary btn-small"><%= I18n.msg(request, "jobs.dmRecruiter") %></a>
            <% } else { %>
            <a href="<%= ctx %>/ta/dashboard?tab=messages&amp;dmJobId=<%= j.getId() %>" class="btn btn-secondary btn-small"><%= I18n.msg(request, "jobs.dmRecruiter") %></a>
            <% } %>
        </p>
        <% if (!Boolean.TRUE.equals(taGuestMode)) { %>
        <form method="post" action="<%= ctx %>/ta/dashboard" class="favorite-form">
            <input type="hidden" name="action" value="toggleFavorite">
            <input type="hidden" name="jobId" value="<%= j.getId() %>">
            <input type="hidden" name="favorited" value="<%= favorited ? "0" : "1" %>">
            <button type="submit" class="btn btn-outline btn-small"><%= favorited ? I18n.msg(request, "jobs.unfavorite") : I18n.msg(request, "jobs.favorite") %></button>
        </form>
        <% } %>
        <% if (already) { %>
        <p class="applied-tag"><%= I18n.msg(request, "jobs.appliedTag") %></p>
        <% } else if (Boolean.TRUE.equals(taGuestMode)) { %>
        <p style="margin:.5rem 0 0">
            <a href="<%= ctx %>/ta/auth?returnUrl=<%= guestJobsReturnEnc %>" class="btn btn-primary"><%= I18n.msg(request, "dash.ta.guest.applyLogin") %></a>
        </p>
        <% } else { %>
        <form method="post" action="<%= ctx %>/ta/dashboard">
            <input type="hidden" name="action" value="apply">
            <input type="hidden" name="jobId" value="<%= j.getId() %>">
            <input type="hidden" name="jobQ" value="<%= qEsc %>">
            <input type="hidden" name="jobType" value="<%= jobFilterType %>">
            <input type="hidden" name="jobSort" value="<%= jobFilterSort %>">
            <input type="hidden" name="jobSkill" value="<%= skillEsc %>">
            <% if (hideApplied) { %><input type="hidden" name="hideApplied" value="1"><% } %>
            <div class="form-group">
                <label><%= I18n.msg(request, "jobs.note") %></label>
                <input type="text" name="note" placeholder="<%= I18n.msg(request, "jobs.note.ph") %>">
            </div>
            <button type="submit" class="btn btn-primary"><%= I18n.msg(request, "jobs.apply.btn") %></button>
        </form>
        <% } %>
    </div>
    <% } %>
    <% } %>
</div>
<style>
.job-recommend-section { margin: 1.5rem 0 0; padding: 1rem 1rem 0; border-top: 1px solid #e5e7eb; }
.recommended-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 0.75rem; margin-top: 0.75rem; }
.recommended-card { display: block; padding: 1rem; border: 1px solid #e2e8f0; border-radius: 0.9rem; text-decoration: none; color: inherit; background: #ffffff; transition: transform .15s ease, border-color .15s ease; }
.recommended-card:hover { transform: translateY(-2px); border-color: #60a5fa; }
.recommended-card strong { display: block; margin-bottom: 0.45rem; font-size: 0.95rem; }
.recommended-card span { color: #475569; font-size: 0.85rem; }
.job-favorites-note { margin-top: 0.5rem; font-size: 0.95rem; color: #065f46; }
.job-match-hint { margin-left: 0.6rem; font-size: 0.8rem; color: #2563eb; padding: 0.08rem 0.45rem; background: rgba(37,99,235,.08); border-radius: 0.65rem; vertical-align: middle; }
.favorite-form { margin-top: 0.5rem; }
.favorite-form .btn { padding: 0.5rem 0.9rem; }
</style>
