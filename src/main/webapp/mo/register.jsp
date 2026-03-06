<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
  String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>课程负责人注册 - TA 招聘系统</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="page-auth">
  <div class="auth-wrap">
    <div class="auth-back"><a href="<%= ctx %>/home">← 返回首页</a></div>
    <div class="auth-card">
      <h1>课程负责人 · 注册</h1>
      <% if (request.getAttribute("error") != null) { %>
      <div class="msg-error"><%= request.getAttribute("error") %></div>
      <% } %>
      <form action="<%= ctx %>/mo/register" method="post" class="auth-form">
        <label>用户名 <input type="text" name="name" required placeholder="李老师" autocomplete="name"></label>
        <label>邮箱 <input type="email" name="email" required placeholder="mo@email.com" autocomplete="email"></label>
        <button type="submit" class="btn">注册并进入工作台</button>
      </form>
      <p class="auth-prompt">已有账号？ <a href="<%= ctx %>/mo/auth" class="btn-link">去登录</a></p>
    </div>
  </div>
</body>
</html>
