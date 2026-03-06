<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.bupt.ta.model.Job" %>
<%@ page import="com.bupt.ta.model.Application" %>
<%@ page import="com.bupt.ta.model.ModuleOrganiser" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%
  ModuleOrganiser mo = (ModuleOrganiser) request.getAttribute("mo");
  if (mo == null) { response.sendRedirect(request.getContextPath() + "/mo/auth"); return; }
  List<Job> myJobs = (List<Job>) request.getAttribute("myJobs");
  String selectedJobId = (String) request.getAttribute("selectedJobId");
  @SuppressWarnings("unchecked") List<Map<String, Object>> appsWithStats = (List<Map<String, Object>>) request.getAttribute("applicationsWithStats");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>课程负责人工作台 - TA 招聘系统</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
  <div class="container">
    <nav class="dash-nav">
      <a href="${pageContext.request.contextPath}/home">← 返回首页</a>
      <span class="dash-user"><%= mo.getName() %></span>
      <a href="${pageContext.request.contextPath}/mo/auth?logout=1">退出</a>
    </nav>
    <div class="panel">
      <h2>发布招聘岗位</h2>
      <form action="${pageContext.request.contextPath}/mo/dashboard" method="post">
        <input type="hidden" name="action" value="postJob">
        <div class="form-row"><label>职位标题</label><input type="text" name="title" required placeholder="如：Java 课程助教"></div>
        <div class="form-row"><label>课程代码</label><input type="text" name="moduleCode" required placeholder="如：CS101"></div>
        <div class="form-row"><label>所需技能（逗号分隔）</label><input type="text" name="requiredSkills" placeholder="Java, 辅导"></div>
        <button type="submit" class="btn-primary">发布岗位</button>
      </form>
    </div>
    <div class="panel">
      <h2>我的岗位</h2>
      <% if (myJobs != null && !myJobs.isEmpty()) { %>
      <ul class="job-list">
        <% for (Job j : myJobs) { %>
        <li><strong><%= j.getTitle() %></strong> (<%= j.getModuleCode() %>) <%= j.getStatus() %> · <a href="${pageContext.request.contextPath}/mo/dashboard?jobId=<%= java.net.URLEncoder.encode(j.getId(), "UTF-8") %>">查看应聘者</a> · ID: <code><%= j.getId() %></code></li>
        <% } %>
      </ul>
      <% } else { %><p>暂无发布岗位。</p><% } %>
    </div>
    <% if (selectedJobId != null) { %>
    <div class="panel">
      <h2>筛选并录用应聘者（含匹配度与当前负荷）</h2>
      <p class="meta">职位 ID：<%= selectedJobId %> · 建议优先考虑负荷较低且匹配度高的申请人。</p>
      <% if (appsWithStats != null && !appsWithStats.isEmpty()) { %>
      <ul class="app-list">
        <% for (Map<String, Object> r : appsWithStats) {
          Application app = (Application) r.get("application");
          int score = (Integer) r.get("matchScore");
          int workload = (Integer) r.get("workload");
          String cls = score >= 80 ? "match-high" : score >= 50 ? "match-mid" : "match-low";
        %>
        <li><strong><%= r.get("applicantName") %></strong> <span class="match-tag <%= cls %>">匹配 <%= score %>%</span> · 当前负荷 <%= workload %> · <%= app.getStatus() %>
          <form action="${pageContext.request.contextPath}/mo/dashboard" method="post" style="display:inline; margin-left:1rem;">
            <input type="hidden" name="action" value="select">
            <input type="hidden" name="applicationId" value="<%= app.getId() %>">
            <input type="hidden" name="jobId" value="<%= selectedJobId %>">
            <button type="submit" class="btn-primary">录用</button>
          </form>
        </li>
        <% } %>
      </ul>
      <% } else { %><p>该职位暂无应聘者。</p><% } %>
    </div>
    <% } %>
  </div>
</body>
</html>
