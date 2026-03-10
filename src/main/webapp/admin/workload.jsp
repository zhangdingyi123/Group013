<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.bupt.ta.web.AdminServlet" %>
<%@ page import="java.util.List" %>
<%
    @SuppressWarnings("unchecked")
    List<AdminServlet.WorkloadEntry> workload = (List<AdminServlet.WorkloadEntry>) request.getAttribute("workload");
    Integer totalAssignments = (Integer) request.getAttribute("totalAssignments");
    if (workload == null) workload = java.util.Collections.emptyList();
    if (totalAssignments == null) totalAssignments = 0;
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>助教工作负荷 - 管理员</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=3">
    <style>
      *{box-sizing:border-box} body{margin:0;font-family:"PingFang SC","Microsoft YaHei",sans-serif;background:#f8fafc;color:#1e293b;min-height:100vh;line-height:1.6}
      .dashboard{max-width:720px;margin:0 auto;padding:1.5rem}
      .page-header{display:flex;align-items:center;justify-content:space-between;flex-wrap:wrap;gap:.75rem;margin-bottom:1rem;padding-bottom:1rem;border-bottom:1px solid #e2e8f0}
      .page-header h1{margin:0;font-size:1.4rem;font-weight:600;color:#1e293b}
      .back-link{padding:.45rem .85rem;color:#2563eb;text-decoration:none;font-size:.9rem;border-radius:6px}
      .back-link:hover{background:#dbeafe}
      .summary{background:#dbeafe;color:#2563eb;padding:.75rem 1rem;border-radius:6px;margin-bottom:1.25rem;font-weight:600}
      .error{color:#dc2626;font-size:.9rem;margin-bottom:1rem;padding:.6rem .85rem;background:#fef2f2;border-radius:6px}
      .empty-hint{color:#64748b;font-size:.9rem;padding:1rem 0}
      .table-wrap{overflow-x:auto;border-radius:6px;border:1px solid #e2e8f0;margin-top:.5rem}
      table{width:100%;border-collapse:collapse;font-size:.9rem}
      th,td{padding:.7rem .9rem;text-align:left;border-bottom:1px solid #e2e8f0}
      tr:last-child td{border-bottom:none}
      th{background:#f1f5f9;color:#1e293b;font-weight:600;font-size:.85rem}
      tbody tr:hover{background:#f8fafc}
    </style>
</head>
<body>
    <div class="dashboard admin-workload">
        <div class="page-header">
            <a href="${pageContext.request.contextPath}/" class="back-link">← 返回首页</a>
            <h1>助教整体工作负荷</h1>
        </div>
        <p>下表为已被录用（accepted）的助教及其当前承担的岗位数。</p>
        <p class="summary">总录用岗位数：<%= totalAssignments %></p>
        <% if (request.getAttribute("error") != null) { %>
        <p class="error"><%= request.getAttribute("error") %></p>
        <% } %>
        <% if (workload.isEmpty()) { %>
        <p class="empty-hint">暂无录用记录。</p>
        <% } else { %>
        <div class="table-wrap">
        <table>
            <thead>
                <tr><th>姓名</th><th>邮箱</th><th>录用岗位数</th></tr>
            </thead>
            <tbody>
            <% for (AdminServlet.WorkloadEntry e : workload) { %>
                <tr>
                    <td><%= e.name %></td>
                    <td><%= e.email %></td>
                    <td><strong><%= e.count %></strong></td>
                </tr>
            <% } %>
            </tbody>
        </table>
        </div>
        <% } %>
    </div>
</body>
</html>
