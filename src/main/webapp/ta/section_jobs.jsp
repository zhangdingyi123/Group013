<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.bupt.ta.model.Job" %>
<%@ page import="java.util.List" %>
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
    @SuppressWarnings("unchecked")
    Set<String> appliedJobIds = (Set<String>) request.getAttribute("appliedJobIds");
    if (appliedJobIds == null) appliedJobIds = java.util.Collections.emptySet();
    String ctx = request.getContextPath();
%>
<div class="section">
    <h2>开放岗位</h2>
    <p class="section-desc">浏览当前开放的助教岗位并提交申请。</p>
    <% if (openJobs.isEmpty()) { %>
    <p class="empty-hint">当前没有开放岗位。</p>
    <% } else { %>
    <% for (Job j : openJobs) {
        if (!Job.STATUS_OPEN.equals(j.getStatus())) continue;
        boolean already = appliedJobIds.contains(j.getId());
    %>
    <div class="job-card">
        <h3><%= j.getTitle() != null ? j.getTitle() : "（未命名）" %></h3>
        <p class="section-desc" style="margin-bottom:.35rem">类型：<%= j.getType() != null ? j.getType() : "-" %>
            &nbsp;|&nbsp; <span class="badge badge-open">开放</span></p>
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
