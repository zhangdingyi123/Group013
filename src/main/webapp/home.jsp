<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>TA 招聘系统 - 北邮国际学院</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="page-home">
  <div class="container">
    <header class="site-header">
      <h1>TA 招聘系统</h1>
      <p class="site-subtitle">北邮国际学院 · 轻量级 Servlet/JSP 应用</p>
    </header>
    <main class="role-cards">
      <a href="${pageContext.request.contextPath}/ta/auth" class="role-card role-ta">
        <span class="role-icon">👤</span>
        <span class="role-title">助教 (TA)</span>
        <span class="role-desc">登录 / 注册 · 申请岗位 · 查看状态</span>
      </a>
      <a href="${pageContext.request.contextPath}/mo/auth" class="role-card role-mo">
        <span class="role-icon">📋</span>
        <span class="role-title">课程负责人 (MO)</span>
        <span class="role-desc">发布岗位 · 筛选应聘者 · 录用</span>
      </a>
      <a href="${pageContext.request.contextPath}/admin" class="role-card role-admin">
        <span class="role-icon">📊</span>
        <span class="role-title">管理员</span>
        <span class="role-desc">查看助教工作负荷</span>
      </a>
    </main>
  </div>
</body>
</html>
