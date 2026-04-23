<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.bupt.ta.model.Job" %>
<%@ page import="java.util.List" %>
<%@ page import="com.bupt.ta.util.I18n" %>
<%
    @SuppressWarnings("unchecked")
    List<Job> myJobs = (List<Job>) request.getAttribute("myJobs");
    if (myJobs == null) myJobs = java.util.Collections.emptyList();
    Boolean moGuestMode = (Boolean) request.getAttribute("moGuestMode");
    String ctx = request.getContextPath();
%>
<div class="section">
    <h2><%= I18n.msg(request, "mo.pos.title") %></h2>
    <% if (myJobs.isEmpty()) { %>
    <% if (Boolean.TRUE.equals(moGuestMode)) { %>
    <p class="empty-hint"><%= I18n.msg(request, "dash.mo.guest.noJobs") %></p>
    <% } else { %>
    <p class="empty-hint"><%= I18n.msg(request, "mo.pos.empty") %></p>
    <% } %>
    <% } %>
    <% if (!myJobs.isEmpty()) { %>
    <div class="table-wrap">
    <table>
        <thead>
            <tr><th><%= I18n.msg(request, "mo.pos.th.title") %></th><th><%= I18n.msg(request, "mo.pos.th.type") %></th><th><%= I18n.msg(request, "mo.pos.th.status") %></th><th><%= I18n.msg(request, "mo.pos.th.ops") %></th></tr>
        </thead>
        <tbody>
        <% for (Job j : myJobs) {
            String statusClass = Job.STATUS_OPEN.equals(j.getStatus()) ? "badge-open" : "badge-closed";
            String jt = j.getType();
            String typeLabel = (jt != null && (jt.equals("course_ta") || jt.equals("invigilation") || jt.equals("activity")))
                    ? I18n.msg(request, "jobs.type." + jt) : (jt != null ? jt : "-");
            String statusLabel = Job.STATUS_OPEN.equals(j.getStatus())
                    ? I18n.msg(request, "mo.pos.status.open") : I18n.msg(request, "mo.pos.status.closed");
        %>
            <tr>
                <td><%= j.getTitle() %></td>
                <td><%= typeLabel %></td>
                <td><span class="badge <%= statusClass %>"><%= statusLabel %></span></td>
                <td>
                    <% if (Job.STATUS_OPEN.equals(j.getStatus())) { %>
                    <a href="<%= ctx %>/mo/dashboard?tab=edit&jobId=<%= j.getId() %>" class="btn btn-secondary btn-small"><%= I18n.msg(request, "mo.pos.edit") %></a>
                    <a href="<%= ctx %>/mo/job-applicants?jobId=<%= j.getId() %>" class="btn btn-secondary btn-small"><%= I18n.msg(request, "mo.pos.filter") %></a>
                    <form method="post" action="<%= ctx %>/mo/dashboard" style="display:inline;" onsubmit="return confirm('<%= I18n.msg(request, "mo.pos.close.confirm").replace("'", "\\'") %>');">
                        <input type="hidden" name="action" value="closeJob">
                        <input type="hidden" name="jobId" value="<%= j.getId() %>">
                        <button type="submit" class="btn btn-secondary btn-small"><%= I18n.msg(request, "mo.pos.close") %></button>
                    </form>
                    <% } else { %>
                    <span class="muted-op"><%= I18n.msg(request, "mo.pos.closedHint") %></span>
                    <% } %>
                </td>
            </tr>
        <% } %>
        </tbody>
    </table>
    </div>
    <% } %>
</div>
