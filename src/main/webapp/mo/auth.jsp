<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
  String tab = request.getAttribute("tab") != null ? (String) request.getAttribute("tab") : request.getParameter("tab");
  if (tab == null) tab = "login";
  boolean isLogin = "login".equals(tab);
  boolean promptGoRegister = request.getAttribute("promptGoRegister") != null;
  boolean promptGoLogin = request.getAttribute("promptGoLogin") != null;
  String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title><%= isLogin ? "课程负责人登录" : "课程负责人注册" %> - TA 招聘系统</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="page-auth">
  <div class="auth-wrap">
    <div class="auth-back"><a href="<%= ctx %>/home">← 返回首页</a></div>
    <div class="auth-card">
      <h1>课程负责人 · 登录 / 注册</h1>
      <nav class="auth-tabs">
        <a href="<%= ctx %>/mo/auth" class="<%= isLogin ? "active" : "" %>">登录</a>
        <a href="<%= ctx %>/mo/auth?tab=register" class="<%= isLogin ? "" : "active" %>">注册</a>
      </nav>
      <% if (request.getAttribute("error") != null) { %>
      <div class="msg-error"><%= request.getAttribute("error") %></div>
      <% } %>
      <% if (isLogin) { %>
      <form action="<%= ctx %>/mo/auth" method="post" class="auth-form">
        <input type="hidden" name="action" value="login">
        <label>邮箱 <input type="email" name="email" required placeholder="mo@email.com" autocomplete="email"></label>
        <button type="submit" class="btn">登录</button>
      </form>
      <p class="auth-prompt"><%= promptGoRegister ? "是否跳转到注册页面？" : "没有账号？" %> <a href="<%= ctx %>/mo/auth?tab=register" class="btn-link">去注册</a></p>
      <% } else { %>
      <form action="<%= ctx %>/mo/auth" method="post" class="auth-form">
        <input type="hidden" name="action" value="register">
        <label>姓名 <input type="text" name="name" required placeholder="李老师" autocomplete="name"></label>
        <label>邮箱 <input type="email" name="email" required placeholder="mo@email.com" autocomplete="email"></label>
        <button type="submit" class="btn">注册并进入工作台</button>
      </form>
      <p class="auth-prompt"><%= promptGoLogin ? "是否跳转到登录页面？" : "已有账号？" %> <a href="<%= ctx %>/mo/auth" class="btn-link">去登录</a></p>
      <% } %>
    </div>
  </div>
</body>
</html>
