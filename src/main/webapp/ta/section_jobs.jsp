<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.bupt.ta.model.Job" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Set" %>
<%
    if (request.getAttribute("applicant") == null) {
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
    String qEsc = jobFilterQ.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;");
    String skillEsc = jobFilterSkill.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;");
    @SuppressWarnings("unchecked")
    java.util.List<String> jobSkillOptions = (java.util.List<String>) request.getAttribute("jobSkillOptions");
    if (jobSkillOptions == null) jobSkillOptions = java.util.Collections.emptyList();
    String ctx = request.getContextPath();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
%>
<div class="section">
    <h2>开放岗位</h2>
    <p class="section-desc">按类型、关键词与技能筛选岗位；技能可从列表快速选择，或在输入框中模糊匹配，多个词用逗号或空格分隔（满足其一即可）。可按匹配度或发布时间排序。</p>

    <form class="job-filter-bar" method="get" action="<%= ctx %>/ta/dashboard">
        <input type="hidden" name="tab" value="jobs">
        <div>
            <label for="job-q">关键词</label>
            <input type="text" id="job-q" name="jobQ" value="<%= qEsc %>" placeholder="岗位名称、描述或技能" autocomplete="off">
        </div>
        <div>
            <label for="job-type">岗位类型</label>
            <select id="job-type" name="jobType">
                <option value="all" <%= "all".equals(jobFilterType) ? "selected" : "" %>>全部类型</option>
                <option value="course_ta" <%= "course_ta".equals(jobFilterType) ? "selected" : "" %>>课程助教</option>
                <option value="invigilation" <%= "invigilation".equals(jobFilterType) ? "selected" : "" %>>监考</option>
                <option value="activity" <%= "activity".equals(jobFilterType) ? "selected" : "" %>>活动支持</option>
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
            <span class="job-skill-label">技能</span>
            <label class="sr-only" for="job-skill-preset">快速选择技能</label>
            <select id="job-skill-preset" class="job-skill-preset" title="从当前开放岗位所需技能中选择">
                <option value="" <%= allSkillsPresetSelected ? "selected" : "" %>>全部技能</option>
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
            <label for="job-skill">模糊匹配</label>
            <input type="text" id="job-skill" name="jobSkill" value="<%= skillEsc %>" placeholder="如 java、英语 或 Java, Python" autocomplete="off" list="job-skill-datalist">
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
            <label for="job-sort">排序</label>
            <select id="job-sort" name="jobSort">
                <option value="newest" <%= "newest".equals(jobFilterSort) ? "selected" : "" %>>发布时间 · 最新</option>
                <option value="oldest" <%= "oldest".equals(jobFilterSort) ? "selected" : "" %>>发布时间 · 最早</option>
                <option value="match_desc" <%= "match_desc".equals(jobFilterSort) ? "selected" : "" %>>匹配度 · 高→低</option>
                <option value="title_asc" <%= "title_asc".equals(jobFilterSort) ? "selected" : "" %>>岗位名称 A→Z</option>
            </select>
        </div>
        <div class="chk">
            <input type="checkbox" id="hide-applied" name="hideApplied" value="1" <%= hideApplied ? "checked" : "" %>>
            <label for="hide-applied">隐藏已投递</label>
        </div>
        <button type="submit" class="btn btn-secondary btn-small">应用筛选</button>
        <a href="<%= ctx %>/ta/dashboard?tab=jobs" class="btn btn-secondary btn-small" style="text-decoration:none;display:inline-block;line-height:1.25">重置</a>
    </form>
    <p class="job-filter-meta">共 <strong><%= openJobsTotal %></strong> 个开放岗位；当前列表 <strong><%= openJobs.size() %></strong> 个。</p>

    <% if (openJobsTotal == 0) { %>
    <p class="empty-hint">当前没有开放岗位。</p>
    <% } else if (openJobs.isEmpty()) { %>
    <p class="empty-hint">没有符合当前筛选条件的岗位，请尝试放宽关键词或取消「隐藏已投递」。</p>
    <% } else { %>
    <% for (Job j : openJobs) {
        if (!Job.STATUS_OPEN.equals(j.getStatus())) continue;
        boolean already = appliedJobIds.contains(j.getId());
        String typeLabel = "-";
        if ("course_ta".equals(j.getType())) typeLabel = "课程助教";
        else if ("invigilation".equals(j.getType())) typeLabel = "监考";
        else if ("activity".equals(j.getType())) typeLabel = "活动支持";
        else if (j.getType() != null) typeLabel = j.getType();
        Integer mscore = jobMatchScores.get(j.getId());
        String scoreLabel = mscore != null ? mscore + " 分" : "—";
    %>
    <div class="job-card">
        <h3><%= j.getTitle() != null ? j.getTitle() : "（未命名）" %>
            <% if (mscore != null) { %><span class="job-match-pill" title="与您的技能标签匹配度">匹配 <%= scoreLabel %></span><% } %>
        </h3>
        <p class="section-desc job-type-label" style="margin-bottom:.35rem">
            <span class="badge badge-open">开放</span>
            &nbsp;·&nbsp;<%= typeLabel %>
            <% if (j.getCreatedAt() > 0) { %>&nbsp;·&nbsp;发布 <%= sdf.format(new Date(j.getCreatedAt())) %><% } %>
        </p>
        <% if (j.getDescription() != null && !j.getDescription().isEmpty()) { %>
        <p class="section-desc"><%= j.getDescription() %></p>
        <% } %>
        <% if (j.getRequiredSkills() != null && !j.getRequiredSkills().isEmpty()) { %>
        <p class="section-desc">所需技能：<%= String.join("、", j.getRequiredSkills()) %></p>
        <% } %>
        <p style="margin:.5rem 0 0">
            <a href="<%= ctx %>/ta/dashboard?tab=messages&amp;dmJobId=<%= j.getId() %>" class="btn btn-secondary btn-small">私信招聘者</a>
        </p>
        <% if (already) { %>
        <p class="applied-tag">您已申请该岗位</p>
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
                <label>申请备注（选填）</label>
                <input type="text" name="note" placeholder="简短说明">
            </div>
            <button type="submit" class="btn btn-primary">申请</button>
        </form>
        <% } %>
    </div>
    <% } %>
    <% } %>
</div>
