<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.bupt.ta.model.ModuleOrganiser" %>
<%
    ModuleOrganiser mo = (ModuleOrganiser) request.getAttribute("mo");
    if (mo == null) {
        String ctx = request.getContextPath();
        if (session.getAttribute("moUser") != null) {
            response.sendRedirect(ctx + "/mo/dashboard");
        } else {
            response.sendRedirect(ctx + "/mo/auth");
        }
        return;
    }
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>个人中心 - 课程组织者</title>
    <link rel="stylesheet" href="<%= ctx %>/css/style.css?v=3">
    <style>
      *{box-sizing:border-box} body{margin:0;font-family:"PingFang SC","Microsoft YaHei",sans-serif;background:#f8fafc;color:#1e293b;min-height:100vh;line-height:1.6}
      .dashboard{max-width:960px;margin:0 auto;padding:1.5rem}
      .page-header{display:grid;grid-template-columns:minmax(0,1fr) auto minmax(0,1fr);align-items:center;gap:.75rem;margin-bottom:1rem;padding-bottom:1rem;border-bottom:1px solid #e2e8f0;position:relative;z-index:1}
      .page-header .back-link{justify-self:start}
      .page-header h1{margin:0;font-size:1.4rem;font-weight:600;color:#1e293b;justify-self:center;text-align:center}
      .header-actions{justify-self:end;display:flex;align-items:center;gap:.15rem;flex-wrap:wrap}
      .back-link{padding:.45rem .85rem;color:#2563eb;text-decoration:none;font-size:.9rem;border-radius:6px}
      .back-link:hover{background:#dbeafe}
      .header-nav-link{font-size:.9rem;color:#2563eb;text-decoration:none;padding:.45rem .75rem;border-radius:6px;font-weight:500}
      .header-nav-link:hover{background:#dbeafe}
      .logout{font-size:.9rem;color:#64748b;text-decoration:none;padding:.45rem .85rem;border-radius:6px}
      .logout:hover{color:#dc2626;background:#fef2f2}
      .section{background:#fff;border-radius:10px;padding:1.35rem;margin-bottom:1.25rem;box-shadow:0 1px 3px rgba(0,0,0,.06);border:1px solid #e2e8f0;border-left:4px solid #2563eb}
      .section h2{font-size:1.05rem;font-weight:600;margin:0 0 1rem;color:#1e293b}
      .section p,.section-desc{margin:0 0 .75rem;color:#64748b;font-size:.9rem}
      .form-group{margin-bottom:1.1rem}
      .form-group label{display:block;font-size:.9rem;font-weight:500;margin-bottom:.4rem;color:#1e293b}
      .form-group input,.form-group textarea,.form-group select{width:100%;padding:.65rem .85rem;border:1px solid #e2e8f0;border-radius:6px;font-size:.95rem;font-family:inherit}
      .form-group input:focus,.form-group textarea:focus,.form-group select:focus{outline:none;border-color:#2563eb;box-shadow:0 0 0 3px #dbeafe}
      .btn{display:inline-block;padding:.4rem .85rem;border:none;border-radius:6px;font-size:.875rem;font-weight:500;cursor:pointer;font-family:inherit;text-decoration:none}
      .btn-primary{background:#2563eb;color:#fff}.btn-primary:hover{background:#1d4ed8}
      .msg-ok{color:#065f46;background:#d1fae5;padding:.6rem .85rem;border-radius:6px;margin-bottom:1rem;font-size:.9rem}
      .msg-err{color:#991b1b;background:#fee2e2;padding:.6rem .85rem;border-radius:6px;margin-bottom:1rem;font-size:.9rem}
      .profile-center-wrap{display:flex;flex-direction:column;gap:1.25rem}
      .profile-hero{display:flex;gap:1.25rem;align-items:flex-start;flex-wrap:wrap;padding:1.35rem 1.5rem;background:linear-gradient(135deg,#0f172a 0%,#1e3a5f 45%,#2563eb 100%);border-radius:12px;color:#fff;box-shadow:0 6px 20px rgba(15,23,42,.25)}
      .profile-avatar{width:76px;height:76px;border-radius:50%;background:rgba(255,255,255,.15);border:3px solid rgba(255,255,255,.35);display:flex;align-items:center;justify-content:center;font-size:1.85rem;font-weight:700;flex-shrink:0;line-height:1}
      .profile-hero-main{flex:1;min-width:0}
      .profile-hero-main .profile-title{margin:0 0 .4rem;font-size:1.2rem;font-weight:600;color:#fff}
      .profile-meta{margin:0;font-size:.9rem;color:rgba(255,255,255,.9);line-height:1.55}
      .profile-meta .sep{opacity:.55;margin:0 .35rem}
      .profile-stats{display:grid;grid-template-columns:repeat(auto-fit,minmax(148px,1fr));gap:.75rem}
      .profile-stat{background:#fff;border-radius:10px;padding:1rem .85rem;border:1px solid #e2e8f0;text-align:center;box-shadow:0 1px 3px rgba(0,0,0,.06)}
      .profile-stat-value{font-size:1.55rem;font-weight:700;color:#2563eb;line-height:1.15;margin-bottom:.3rem}
      .profile-stat-label{font-size:.78rem;color:#64748b;font-weight:500}
      .section.profile-edit{border-left-color:#64748b}
      .field-ro{background:#f8fafc;color:#475569}
    </style>
</head>
<body>
    <div class="dashboard">
        <div class="page-header">
            <a href="<%= ctx %>/" class="back-link">← 首页</a>
            <h1>个人中心</h1>
            <div class="header-actions">
                <a href="<%= ctx %>/mo/dashboard" class="header-nav-link">工作台</a>
                <a href="<%= ctx %>/mo/auth?logout=1" class="logout">退出登录</a>
            </div>
        </div>

        <% if (request.getAttribute("error") != null) { %>
        <p class="msg-err"><%= request.getAttribute("error") %></p>
        <% } %>
        <% if (request.getAttribute("moNotice") != null) { %>
        <p class="msg-ok"><%= request.getAttribute("moNotice") %></p>
        <% } %>

        <jsp:include page="/mo/section_profile.jsp"/>
    </div>
</body>
</html>
