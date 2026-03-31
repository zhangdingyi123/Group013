<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.bupt.ta.model.Application" %>
<%@ page import="com.bupt.ta.model.Job" %>
<%@ page import="com.bupt.ta.web.TADashboardServlet.ApplicationWithJob" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.List" %>
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
    List<ApplicationWithJob> myApplications = (List<ApplicationWithJob>) request.getAttribute("myApplications");
    if (myApplications == null) myApplications = java.util.Collections.emptyList();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    String ctx = request.getContextPath();
    String applicationsPostUrl = (String) request.getAttribute("applicationsPostUrl");
    if (applicationsPostUrl == null) {
        applicationsPostUrl = ctx + "/ta/dashboard";
    }
%>
<div class="section">
    <h2>我的申请</h2>
    <p class="section-desc">查看申请状态；待审核时可撤销申请。</p>
    <% if (myApplications.isEmpty()) { %>
    <p class="empty-hint">暂无申请记录。</p>
    <% } else { %>
    <div class="table-wrap">
    <table>
        <thead>
            <tr><th>岗位</th><th>状态</th><th>申请时间</th><th>操作</th></tr>
        </thead>
        <tbody>
        <% for (ApplicationWithJob aw : myApplications) {
            Application app = aw.application;
            Job job = aw.job;
            String title = job != null && job.getTitle() != null ? job.getTitle() : "（岗位已删除）";
            String st = app.getStatus() != null ? app.getStatus() : "";
            String badgeClass = "badge-pending";
            if (Application.STATUS_ACCEPTED.equals(st)) badgeClass = "badge-accepted";
            else if (Application.STATUS_REJECTED.equals(st)) badgeClass = "badge-rejected";
            else if (Application.STATUS_CANCELLED.equals(st)) badgeClass = "badge-cancelled";
        %>
            <tr>
                <td><%= title %></td>
                <td><span class="badge <%= badgeClass %>"><%= st %></span></td>
                <td><%= sdf.format(new Date(app.getAppliedAt())) %></td>
                <td>
                    <% if (Application.STATUS_PENDING.equals(st)) { %>
                    <form method="post" action="<%= applicationsPostUrl %>" style="display:inline;">
                        <input type="hidden" name="action" value="cancelApplication">
                        <input type="hidden" name="applicationId" value="<%= app.getId() %>">
                        <button type="submit" class="btn btn-secondary btn-small">撤销申请</button>
                    </form>
                    <% } else { %>—<% } %>
                </td>
            </tr>
        <% } %>
        </tbody>
    </table>
    </div>
    <% } %>
</div>
