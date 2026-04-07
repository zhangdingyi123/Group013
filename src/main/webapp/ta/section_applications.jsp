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
<section id="pc-applications" class="section">
    <h2>我的申请</h2>
    <p class="section-desc">查看申请状态；待审核时可撤销申请。面试/试讲安排需选择：确认参加、拒绝或希望更换时间。</p>
    <% if (myApplications.isEmpty()) { %>
    <p class="empty-hint">暂无申请记录。</p>
    <% } else { %>
    <div class="table-wrap">
    <table>
        <thead>
            <tr><th>岗位</th><th>状态</th><th>面试/试讲</th><th>申请时间</th><th>操作</th></tr>
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
            String stLabel = st;
            if (Application.STATUS_PENDING.equals(st)) stLabel = "待审核";
            else if (Application.STATUS_INTERVIEW.equals(st)) stLabel = "待面试";
            else if (Application.STATUS_ACCEPTED.equals(st)) stLabel = "已录用";
            else if (Application.STATUS_REJECTED.equals(st)) stLabel = "已拒绝";
            else if (Application.STATUS_CANCELLED.equals(st)) stLabel = "已撤销";
            if (Application.STATUS_INTERVIEW.equals(st)) badgeClass = "badge-interview";
            String ivTa = Application.STATUS_INTERVIEW.equals(st) ? app.getInterviewTaStatus() : "";
            boolean ivAwaitTa = Application.STATUS_INTERVIEW.equals(st)
                    && Application.TA_IV_PENDING.equals(ivTa);
            String ivTaLabel = "";
            if (Application.STATUS_INTERVIEW.equals(st)) {
                if (Application.TA_IV_PENDING.equals(ivTa)) ivTaLabel = "待确认";
                else if (Application.TA_IV_CONFIRMED.equals(ivTa)) ivTaLabel = "已确认";
                else if (Application.TA_IV_DECLINED.equals(ivTa)) ivTaLabel = "拒绝";
                else if (Application.TA_IV_RESCHEDULE.equals(ivTa)) ivTaLabel = "更换时间";
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
                    <div class="app-iv-note"><strong>时间：</strong><%= sdf.format(new Date(app.getInterviewAt())) %></div>
                    <% } %>
                    <div class="app-iv-note"><strong>地点/链接：</strong><%= detEsc.isEmpty() ? "—" : detEsc %></div>
                    <% } else { %>—<% } %>
                </td>
                <td><%= sdf.format(new Date(app.getAppliedAt())) %></td>
                <td>
                    <% if (Application.STATUS_PENDING.equals(st)) { %>
                    <form method="post" action="<%= applicationsPostUrl %>" style="display:inline;">
                        <input type="hidden" name="action" value="cancelApplication">
                        <input type="hidden" name="applicationId" value="<%= app.getId() %>">
                        <button type="submit" class="btn btn-secondary btn-small">撤销申请</button>
                    </form>
                    <% } else if (ivAwaitTa) { %>
                    <div style="display:flex;flex-wrap:wrap;gap:.35rem;align-items:center;">
                    <form method="post" action="<%= applicationsPostUrl %>" style="display:inline;">
                        <input type="hidden" name="action" value="confirmInterview">
                        <input type="hidden" name="applicationId" value="<%= app.getId() %>">
                        <button type="submit" class="btn btn-primary btn-small">确认参加</button>
                    </form>
                    <form method="post" action="<%= applicationsPostUrl %>" style="display:inline;" onsubmit="return confirm('确定拒绝本次面试/试讲安排吗？');">
                        <input type="hidden" name="action" value="declineInterview">
                        <input type="hidden" name="applicationId" value="<%= app.getId() %>">
                        <button type="submit" class="btn btn-secondary btn-small">拒绝</button>
                    </form>
                    <form method="post" action="<%= applicationsPostUrl %>" style="display:inline;" onsubmit="return confirm('将通知招聘方您希望更换时间，确定吗？');">
                        <input type="hidden" name="action" value="requestRescheduleInterview">
                        <input type="hidden" name="applicationId" value="<%= app.getId() %>">
                        <button type="submit" class="btn btn-secondary btn-small">更换时间</button>
                    </form>
                    </div>
                    <% } else { %>—<% } %>
                </td>
            </tr>
        <% } %>
        </tbody>
    </table>
    </div>
    <% } %>
</section>
