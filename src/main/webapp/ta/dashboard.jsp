<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.bupt.ta.model.Applicant" %>
<%@ page import="com.bupt.ta.model.Application" %>
<%@ page import="com.bupt.ta.model.Job" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%
  Applicant ta = (Applicant) request.getAttribute("ta");
  if (ta == null) { response.sendRedirect(request.getContextPath() + "/ta/auth"); return; }
  @SuppressWarnings("unchecked") List<Map<String, Object>> jobsWithMatch = (List<Map<String, Object>>) request.getAttribute("jobsWithMatch");
  List<Application> myApps = (List<Application>) request.getAttribute("myApplications");
  String[] statusText = {"PENDING", "待处理", "SELECTED", "已录用", "REJECTED", "已拒绝"};
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>助教工作台 - TA 招聘系统</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
  <div class="container">
    <nav class="dash-nav">
      <a href="${pageContext.request.contextPath}/home">← 返回首页</a>
      <span class="dash-user"><%= ta.getName() %></span>
      <a href="${pageContext.request.contextPath}/ta/auth?logout=1">退出</a>
    </nav>
    <div class="panel">
      <h2>助教工作台</h2>
      <div class="tabs">
        <a href="#profile" class="active">我的资料</a>
        <a href="#jobs">可申请岗位</a>
        <a href="#apps">我的申请状态</a>
      </div>
      <div id="profile">
        <form action="${pageContext.request.contextPath}/ta/dashboard" method="post">
          <input type="hidden" name="action" value="saveCv">
          <div class="form-row"><label>简历路径</label><input type="text" name="cvPath" value="<%= ta.getCvPath() != null ? ta.getCvPath() : "" %>" placeholder="如 cv.txt"></div>
          <button type="submit" class="btn-primary">保存简历路径</button>
        </form>
        <form action="${pageContext.request.contextPath}/ta/dashboard" method="post" style="margin-top:1rem;">
          <input type="hidden" name="action" value="saveSkills">
          <div class="form-row"><label>技能（逗号分隔）</label><input type="text" name="skills" value="<%= ta.getSkills() != null ? String.join(", ", ta.getSkills()) : "" %>" placeholder="Java, 数学"></div>
          <button type="submit" class="btn-primary">保存技能</button>
        </form>
      </div>
      <div id="jobs" style="display:none;">
        <p class="meta">以下为可申请岗位，含匹配度与技能短板（按匹配度排序）。</p>
        <% if (jobsWithMatch != null && !jobsWithMatch.isEmpty()) { %>
        <ul class="job-list">
          <% for (Map<String, Object> item : jobsWithMatch) {
            Job j = (Job) item.get("job");
            int score = (Integer) item.get("matchScore");
            List<String> missing = (List<String>) item.get("missingSkills");
            String cls = score >= 80 ? "match-high" : score >= 50 ? "match-mid" : "match-low";
          %>
          <li><strong><%= j.getTitle() %></strong> (<%= j.getModuleCode() %>) <span class="match-tag <%= cls %>">匹配 <%= score %>%</span>
            <% if (missing != null && !missing.isEmpty()) { %><p class="meta">技能短板：<%= String.join("、", missing) %></p><% } %>
            <p class="meta">职位 ID：<code><%= j.getId() %></code></p>
          </li>
          <% } %>
        </ul>
        <form action="${pageContext.request.contextPath}/ta/dashboard" method="post" style="margin-top:1rem;">
          <input type="hidden" name="action" value="apply">
          <div class="form-row"><label>申请职位 ID</label><input type="text" name="jobId" placeholder="粘贴职位 ID"></div>
          <button type="submit" class="btn-primary">提交岗位申请</button>
        </form>
        <% } else { %><p>暂无开放岗位。</p><% } %>
      </div>
      <div id="apps" style="display:none;">
        <% if (myApps != null && !myApps.isEmpty()) { %>
        <ul class="app-list">
          <% for (Application a : myApps) {
            String st = a.getStatus();
            for (int i = 0; i < statusText.length; i += 2) if (statusText[i].equals(st)) { st = statusText[i + 1]; break; }
          %><li>职位 <%= a.getJobId() %> → <strong><%= st %></strong></li><% } %>
        </ul>
        <% } else { %><p>暂无申请记录。</p><% } %>
      </div>
    </div>
  </div>
  <script>
    document.querySelectorAll('.tabs a').forEach(function(a) {
      a.addEventListener('click', function(e) { e.preventDefault();
        document.querySelectorAll('.tabs a').forEach(function(x) { x.classList.remove('active'); });
        this.classList.add('active');
        document.querySelectorAll('#profile, #jobs, #apps').forEach(function(d) { d.style.display = 'none'; });
        document.getElementById(this.getAttribute('href').slice(1)).style.display = 'block';
      });
    });
  </script>
</body>
</html>
