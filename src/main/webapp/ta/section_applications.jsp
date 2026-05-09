<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.bupt.ta.util.I18n" %>
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
<section id="pc-applications" class="section">
    <h2><%= I18n.msg(request, "apps.title") %></h2>
    <p class="section-desc"><%= I18n.msg(request, "apps.desc") %></p>
    <% if (myApplications.isEmpty()) { %>
    <p class="empty-hint"><%= I18n.msg(request, "apps.empty") %></p>
    <% } else { %>
    <div class="table-wrap">
    <table>
        <thead>
            <tr>
                <th><%= I18n.msg(request, "apps.th.job") %></th>
                <th><%= I18n.msg(request, "apps.th.status") %></th>
                <th><%= I18n.msg(request, "apps.th.iv") %></th>
                <th><%= I18n.msg(request, "apps.th.time") %></th>
                <th><%= I18n.msg(request, "apps.th.ops") %></th>
            </tr>
        </thead>
        <tbody>
        <% for (ApplicationWithJob aw : myApplications) {
            Application app = aw.application;
            Job job = aw.job;
            String title = job != null && job.getTitle() != null ? job.getTitle() : I18n.msg(request, "apps.job.deleted");
            String st = app.getStatus() != null ? app.getStatus() : "";
            String badgeClass = "badge-pending";
            if (Application.STATUS_ACCEPTED.equals(st)) badgeClass = "badge-accepted";
            else if (Application.STATUS_REJECTED.equals(st)) badgeClass = "badge-rejected";
            else if (Application.STATUS_CANCELLED.equals(st)) badgeClass = "badge-cancelled";
            String stLabel = st;
            if (Application.STATUS_PENDING.equals(st)) stLabel = I18n.msg(request, "apps.status.pending");
            else if (Application.STATUS_INTERVIEW.equals(st)) stLabel = I18n.msg(request, "apps.status.interview");
            else if (Application.STATUS_ACCEPTED.equals(st)) stLabel = I18n.msg(request, "apps.status.accepted");
            else if (Application.STATUS_REJECTED.equals(st)) stLabel = I18n.msg(request, "apps.status.rejected");
            else if (Application.STATUS_CANCELLED.equals(st)) stLabel = I18n.msg(request, "apps.status.cancelled");
            if (Application.STATUS_INTERVIEW.equals(st)) badgeClass = "badge-interview";
            String ivTa = Application.STATUS_INTERVIEW.equals(st) ? app.getInterviewTaStatus() : "";
            boolean ivAwaitTa = Application.STATUS_INTERVIEW.equals(st)
                    && Application.TA_IV_PENDING.equals(ivTa);
            String ivTaLabel = "";
            if (Application.STATUS_INTERVIEW.equals(st)) {
                if (Application.TA_IV_PENDING.equals(ivTa)) ivTaLabel = I18n.msg(request, "apps.iv.ta.pending");
                else if (Application.TA_IV_CONFIRMED.equals(ivTa)) ivTaLabel = I18n.msg(request, "apps.iv.ta.confirmed");
                else if (Application.TA_IV_DECLINED.equals(ivTa)) ivTaLabel = I18n.msg(request, "apps.iv.ta.declined");
                else if (Application.TA_IV_RESCHEDULE.equals(ivTa)) ivTaLabel = I18n.msg(request, "apps.iv.ta.reschedule");
            }
            String det = app.getInterviewDetail();
            String detEsc = det == null ? "" : det.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
        %>
            <tr>
                <td><%= title %></td>
                <td>
                    <span class="badge <%= badgeClass %>"><%= stLabel %></span>
                    <% if (Application.STATUS_INTERVIEW.equals(st) && !ivTaLabel.isEmpty()) { %>
                    <div style="font-size:.78rem;margin-top:.2rem;color:#64748b;"><%= ivTaLabel %></div>
                    <% } %>
                </td>
                <td>
                    <% if (Application.STATUS_INTERVIEW.equals(st)) { %>
                    <% if (app.getInterviewAt() > 0) { %>
                    <div class="app-iv-note"><strong><%= I18n.msg(request, "apps.iv.time") %></strong><%= sdf.format(new Date(app.getInterviewAt())) %></div>
                    <% } %>
                    <div class="app-iv-note"><strong><%= I18n.msg(request, "apps.iv.place") %></strong><%= detEsc.isEmpty() ? I18n.msg(request, "common.dash") : detEsc %></div>
                    <% } else { %><%= I18n.msg(request, "common.dash") %><% } %>
                </td>
                <td><%= sdf.format(new Date(app.getAppliedAt())) %></td>
                <td>
                    <% if (Application.STATUS_PENDING.equals(st)) { %>
                    <form method="post" action="<%= applicationsPostUrl %>" style="display:inline;">
                        <input type="hidden" name="action" value="cancelApplication">
                        <input type="hidden" name="applicationId" value="<%= app.getId() %>">
                        <button type="submit" class="btn btn-secondary btn-small"><%= I18n.msg(request, "apps.cancel") %></button>
                    </form>
                    <% } else if (ivAwaitTa) { %>
                    <div style="display:flex;flex-wrap:wrap;gap:.35rem;align-items:center;">
                    <form method="post" action="<%= applicationsPostUrl %>" style="display:inline;">
                        <input type="hidden" name="action" value="confirmInterview">
                        <input type="hidden" name="applicationId" value="<%= app.getId() %>">
                        <button type="submit" class="btn btn-primary btn-small"><%= I18n.msg(request, "apps.confirmAttend") %></button>
                    </form>
                    <form method="post" action="<%= applicationsPostUrl %>" style="display:inline;" onsubmit="return confirm('<%= I18n.msg(request, "apps.confirm.decline").replace(\"\\\\\", \"\\\\\\\\\").replace(\"'\", \"\\\\'\").replace(\"\\r\", \"\").replace(\"\\n\", \"\\\\n\") %>');">
                        <input type="hidden" name="action" value="declineInterview">
                        <input type="hidden" name="applicationId" value="<%= app.getId() %>">
                        <button type="submit" class="btn btn-secondary btn-small"><%= I18n.msg(request, "apps.decline") %></button>
                    </form>
                    <form method="post" action="<%= applicationsPostUrl %>" style="display:inline;" onsubmit="return confirm('<%= I18n.msg(request, "apps.confirm.reschedule").replace(\"\\\\\", \"\\\\\\\\\").replace(\"'\", \"\\\\'\").replace(\"\\r\", \"\").replace(\"\\n\", \"\\\\n\") %>');">
                        <input type="hidden" name="action" value="requestRescheduleInterview">
                        <input type="hidden" name="applicationId" value="<%= app.getId() %>">
                        <button type="submit" class="btn btn-secondary btn-small"><%= I18n.msg(request, "apps.reschedule") %></button>
                    </form>
                    </div>
                    <% } else { %><%= I18n.msg(request, "common.dash") %><% } %>
                </td>
            </tr>
        <% } %>
        </tbody>
    </table>
    </div>
    <% } %>
</section>
