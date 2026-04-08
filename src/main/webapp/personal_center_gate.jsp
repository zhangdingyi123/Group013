<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.bupt.ta.util.I18n" %>
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
<html lang="<%= I18n.htmlLangAttr(request) %>">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= I18n.msg(request, "gate.title") %> - <%= I18n.msg(request, "common.sysName") %></title>
    <link rel="stylesheet" href="<%= ctx %>/css/style.css?v=4">
</head>
<body class="gate-page">
    <div class="gate-bg" aria-hidden="true"></div>
    <div class="gate-wrap">
        <div style="display:flex;justify-content:flex-end;margin-bottom:0.75rem;">
            <jsp:include page="/WEB-INF/jsp/lang_switch.jsp"/>
        </div>
        <a href="<%= ctx %>/" class="gate-back"><%= I18n.msg(request, "gate.back") %></a>
        <h1><%= I18n.msg(request, "gate.title") %></h1>
        <p class="gate-lead"><%= I18n.msg(request, "gate.lead") %></p>
        <nav class="gate-choices" aria-label="<%= I18n.msg(request, "common.login") %>">
            <a href="<%= ctx %>/ta/auth"><%= I18n.msg(request, "gate.ta") %><span><%= I18n.msg(request, "gate.ta.sub") %></span></a>
            <a href="<%= ctx %>/mo/auth"><%= I18n.msg(request, "gate.mo") %><span><%= I18n.msg(request, "gate.mo.sub") %></span></a>
            <a href="<%= ctx %>/admin/auth"><%= I18n.msg(request, "gate.admin") %><span><%= I18n.msg(request, "gate.admin.sub") %></span></a>
        </nav>
        <p class="gate-footer-links">
            <a href="<%= ctx %>/forum"><%= I18n.msg(request, "gate.footer.forum") %></a><span class="dot">·</span><a href="<%= ctx %>/assistant"><%= I18n.msg(request, "gate.footer.assistant") %></a>
        </p>
    </div>
</body>
</html>
