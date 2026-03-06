<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%
  @SuppressWarnings("unchecked") List<Map<String, Object>> rows = (List<Map<String, Object>>) request.getAttribute("workloadRows");
  if (rows == null) rows = java.util.Collections.emptyList();
  int total = 0;
  for (Map<String, Object> r : rows) total += (Integer) r.get("workload");
  double avg = rows.isEmpty() ? 0 : (double) total / rows.size();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>助教工作负荷 - TA 招聘系统</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
  <div class="container">
    <nav class="dash-nav">
      <a href="${pageContext.request.contextPath}/home">← 返回首页</a>
    </nav>
    <div class="panel">
      <h2>管理员 · 助教整体工作负荷</h2>
      <p class="meta">下表为每位助教当前已分配的职位数量。</p>
      <table>
        <thead><tr><th>姓名</th><th>邮箱</th><th>已分配职位数</th></tr></thead>
        <tbody>
          <% for (Map<String, Object> r : rows) { %>
          <tr><td><%= r.get("name") %></td><td><%= r.get("email") %></td><td><%= r.get("workload") %></td></tr>
          <% } %>
          <% if (rows.size() > 1) { %><tr><td colspan="2"><strong>合计 / 人均</strong></td><td><%= total %> / <%= String.format("%.1f", avg) %></td></tr><% } %>
        </tbody>
      </table>
    </div>
  </div>
</body>
</html>
