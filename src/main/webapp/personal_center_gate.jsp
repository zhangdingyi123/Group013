<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String ctx = request.getContextPath();
    if (session.getAttribute("taUser") != null) {
        response.sendRedirect(ctx + "/ta/profile");
        return;
    }
    if (session.getAttribute("moUser") != null) {
        response.sendRedirect(ctx + "/mo/profile");
        return;
    }
    if (session.getAttribute("adminUser") != null) {
        response.sendRedirect(ctx + "/admin/workload");
        return;
    }
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>个人中心 - 助教招聘系统</title>
    <link rel="stylesheet" href="<%= ctx %>/css/style.css?v=3">
    <style>
      * { box-sizing: border-box; }
      body.gate { margin: 0; font-family: "PingFang SC","Microsoft YaHei",sans-serif; min-height: 100vh;
        background: linear-gradient(160deg, #f0f9ff 0%, #e0f2fe 50%, #f8fafc 100%); color: #1e293b; }
      .wrap { max-width: 520px; margin: 0 auto; padding: 4rem 1.5rem 2rem; }
      .back { display: inline-block; margin-bottom: 1.5rem; color: #2563eb; text-decoration: none; font-size: 0.9rem; }
      .back:hover { text-decoration: underline; }
      h1 { font-size: 1.35rem; font-weight: 700; margin: 0 0 0.35rem; }
      .lead { color: #64748b; font-size: 0.95rem; margin: 0 0 1.75rem; line-height: 1.5; }
      .choices { display: flex; flex-direction: column; gap: 0.75rem; }
      .choices a { display: block; padding: 1rem 1.15rem; background: #fff; border-radius: 10px; text-decoration: none; color: #1e293b;
        border: 1px solid #e2e8f0; box-shadow: 0 2px 8px rgba(0,0,0,0.06); font-weight: 500; transition: border-color .15s, box-shadow .15s; }
      .choices a:hover { border-color: #93c5fd; box-shadow: 0 4px 16px rgba(37,99,235,0.12); }
      .choices a span { display: block; font-size: 0.85rem; font-weight: 400; color: #64748b; margin-top: 0.25rem; }
    </style>
</head>
<body class="gate">
    <div class="wrap">
        <a href="<%= ctx %>/" class="back">← 返回首页</a>
        <h1>个人中心</h1>
        <p class="lead">请先选择身份并登录，登录成功后将进入对应个人中心或工作台。</p>
        <nav class="choices" aria-label="登录入口">
            <a href="<%= ctx %>/ta/auth">应聘者登录<span>简历、申请与个人信息</span></a>
            <a href="<%= ctx %>/mo/auth">课程组织者登录<span>岗位与筛选管理</span></a>
            <a href="<%= ctx %>/admin/auth">管理员登录<span>工作负荷与录用管理</span></a>
        </nav>
    </div>
</body>
</html>
