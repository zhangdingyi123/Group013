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
    String tab = (String) request.getAttribute("moDashboardTab");
    if (tab == null) tab = "positions";
    String pageTitle = "我的岗位";
    if ("post".equals(tab)) pageTitle = "发布新岗位";
    else if ("messages".equals(tab)) pageTitle = "私信";
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= pageTitle %> - 课程组织者</title>
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
      .mo-subnav{display:flex;flex-wrap:wrap;gap:.35rem;margin-bottom:1.25rem;padding:.35rem;background:#e2e8f0;border-radius:10px;position:relative;z-index:2}
      .mo-subnav a{display:inline-block;padding:.5rem 1rem;border-radius:8px;font-size:.9rem;font-weight:500;color:#475569;text-decoration:none;cursor:pointer;-webkit-tap-highlight-color:rgba(37,99,235,.15)}
      .mo-subnav a:hover{background:#cbd5e1;color:#1e293b}
      .mo-subnav a.active{background:#fff;color:#2563eb;box-shadow:0 1px 3px rgba(0,0,0,.08)}
      .section{background:#fff;border-radius:10px;padding:1.35rem;margin-bottom:1.25rem;box-shadow:0 1px 3px rgba(0,0,0,.06);border:1px solid #e2e8f0;border-left:4px solid #2563eb}
      .section h2{font-size:1.05rem;font-weight:600;margin:0 0 1rem;color:#1e293b}
      .section p,.section-desc{margin:0 0 .75rem;color:#64748b;font-size:.9rem}
      .form-group{margin-bottom:1.1rem}
      .form-group label{display:block;font-size:.9rem;font-weight:500;margin-bottom:.4rem;color:#1e293b}
      .form-group input,.form-group textarea,.form-group select{width:100%;padding:.65rem .85rem;border:1px solid #e2e8f0;border-radius:6px;font-size:.95rem;font-family:inherit}
      .form-group input:focus,.form-group textarea:focus,.form-group select:focus{outline:none;border-color:#2563eb;box-shadow:0 0 0 3px #dbeafe}
      .form-group textarea{min-height:100px;resize:vertical}
      .btn{display:inline-block;padding:.4rem .85rem;border:none;border-radius:6px;font-size:.875rem;font-weight:500;cursor:pointer;font-family:inherit;text-decoration:none}
      .btn-primary{background:#2563eb;color:#fff}.btn-primary:hover{background:#1d4ed8}
      .btn-secondary{background:#e2e8f0;color:#475569}.btn-secondary:hover{background:#cbd5e1}
      .btn-small{font-size:.8rem;padding:.35rem .65rem}
      .table-wrap{overflow-x:auto;border-radius:6px;border:1px solid #e2e8f0;margin-top:.5rem}
      table{width:100%;border-collapse:collapse;font-size:.9rem}
      th,td{padding:.7rem .9rem;text-align:left;border-bottom:1px solid #e2e8f0}
      tr:last-child td{border-bottom:none}
      th{background:#f1f5f9;color:#1e293b;font-weight:600;font-size:.85rem}
      tbody tr:hover{background:#f8fafc}
      .badge{display:inline-block;padding:.25rem .6rem;border-radius:999px;font-size:.8rem;font-weight:500}
      .badge-pending{background:#fef3c7;color:#92400e}.badge-accepted{background:#d1fae5;color:#065f46}.badge-rejected{background:#fee2e2;color:#991b1b}
      .badge-open{background:#dbeafe;color:#1e40af}.badge-closed{background:#f1f5f9;color:#475569}
      .score{font-weight:600;color:#2563eb}.gaps{font-size:.85rem;color:#64748b}
      .empty-hint{color:#64748b;font-size:.9rem;padding:1rem 0}
      .muted-op{color:#94a3b8;font-size:.85rem}
      .msg-err{color:#991b1b;background:#fee2e2;padding:.6rem .85rem;border-radius:6px;margin-bottom:1rem;font-size:.9rem}
      .msg-ok{color:#065f46;background:#d1fae5;padding:.6rem .85rem;border-radius:6px;margin-bottom:1rem;font-size:.9rem}
    </style>
</head>
<body>
    <div class="dashboard">
        <div class="page-header">
            <a href="<%= ctx %>/" class="back-link">← 首页</a>
            <h1>课程组织者工作台</h1>
            <div class="header-actions">
                <a href="<%= ctx %>/mo/profile" class="header-nav-link">个人中心</a>
                <a href="<%= ctx %>/mo/auth?logout=1" class="logout">退出登录</a>
            </div>
        </div>

        <nav class="mo-subnav" aria-label="工作台分区">
            <a href="<%= ctx %>/mo/dashboard?tab=positions" class="<%= "positions".equals(tab) ? "active" : "" %>">我的岗位</a>
            <a href="<%= ctx %>/mo/dashboard?tab=post" class="<%= "post".equals(tab) ? "active" : "" %>">发布新岗位</a>
            <a href="<%= ctx %>/mo/dashboard?tab=messages" class="<%= "messages".equals(tab) ? "active" : "" %>">私信</a>
        </nav>

        <% if (request.getAttribute("error") != null) { %>
        <p class="msg-err"><%= request.getAttribute("error") %></p>
        <% } %>
        <% if (request.getAttribute("moNotice") != null) { %>
        <p class="msg-ok"><%= request.getAttribute("moNotice") %></p>
        <% } %>

        <% if ("post".equals(tab)) { %>
        <jsp:include page="/mo/section_post_job.jsp"/>
        <% } else if ("messages".equals(tab)) { %>
        <jsp:include page="/mo/section_messages.jsp"/>
        <% } else { %>
        <jsp:include page="/mo/section_my_positions.jsp"/>
        <% } %>
    </div>
</body>
</html>
