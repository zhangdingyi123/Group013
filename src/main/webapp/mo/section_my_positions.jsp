<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.bupt.ta.model.Job" %>
<%@ page import="java.util.List" %>
<%
    @SuppressWarnings("unchecked")
    List<Job> myJobs = (List<Job>) request.getAttribute("myJobs");
    if (myJobs == null) myJobs = java.util.Collections.emptyList();
    String ctx = request.getContextPath();
%>
<div class="section">
    <h2>我的岗位</h2>
    <% if (myJobs.isEmpty()) { %>
    <p class="empty-hint">暂无岗位。请切换到「发布新岗位」发布职位。</p>
    <% } else { %>
    <div class="table-wrap">
    <table>
        <thead>
            <tr><th>岗位名称</th><th>类型</th><th>状态</th><th>操作</th></tr>
        </thead>
        <tbody>
        <% for (Job j : myJobs) {
            String statusClass = "open".equals(j.getStatus()) ? "badge-open" : "badge-closed";
        %>
            <tr>
                <td><%= j.getTitle() %></td>
                <td><%= j.getType() != null ? j.getType() : "-" %></td>
                <td><span class="badge <%= statusClass %>"><%= j.getStatus() %></span></td>
                <td>
                    <% if ("open".equals(j.getStatus())) { %>
                    <a href="<%= ctx %>/mo/job-applicants?jobId=<%= j.getId() %>" class="btn btn-secondary btn-small">筛选应聘者</a>
                    <form method="post" action="<%= ctx %>/mo/dashboard" style="display:inline;" onsubmit="return confirm('关闭后无法再进入筛选页面对应聘者进行录用或拒绝，确定要关闭该岗位吗？');">
                        <input type="hidden" name="action" value="closeJob">
                        <input type="hidden" name="jobId" value="<%= j.getId() %>">
                        <button type="submit" class="btn btn-secondary btn-small">关闭岗位</button>
                    </form>
                    <% } else { %>
                    <span class="muted-op">已关闭（不可筛选）</span>
                    <% } %>
                </td>
            </tr>
        <% } %>
        </tbody>
    </table>
    </div>
    <% } %>
</div>
