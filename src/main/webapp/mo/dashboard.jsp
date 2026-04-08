<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="com.bupt.ta.model.ModuleOrganiser" %>
<%@ page import="com.bupt.ta.util.I18n" %>
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
    String pageTitle = I18n.msg(request, "dash.mo.tab.positions");
    if ("post".equals(tab)) pageTitle = I18n.msg(request, "dash.mo.tab.post");
    else if ("messages".equals(tab)) pageTitle = I18n.msg(request, "dash.mo.tab.msg");
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="<%= I18n.htmlLangAttr(request) %>">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= I18n.msg(request, "dash.mo.pageTitle", pageTitle) %></title>
    <link rel="stylesheet" href="<%= ctx %>/css/style.css?v=4">
</head>
<body class="dashboard-app">
    <div class="dashboard">
        <div class="page-header has-lang">
            <jsp:include page="/WEB-INF/jsp/lang_switch.jsp"/>
            <a href="<%= ctx %>/" class="back-link"><%= I18n.msg(request, "header.backHome") %></a>
            <h1><%= I18n.msg(request, "dash.mo.title") %></h1>
            <div class="header-actions">
                <a href="<%= ctx %>/forum" class="header-nav-link"><%= I18n.msg(request, "header.forum") %></a>
                <a href="<%= ctx %>/assistant" class="header-nav-link"><%= I18n.msg(request, "header.assistant") %></a>
                <a href="<%= ctx %>/mo/profile" class="header-nav-link"><%= I18n.msg(request, "header.profile") %></a>
                <a href="<%= ctx %>/mo/auth?logout=1" class="logout"><%= I18n.msg(request, "common.logout") %></a>
            </div>
        </div>

        <nav class="mo-subnav" aria-label="<%= I18n.msg(request, "dash.mo.title") %>">
            <a href="<%= ctx %>/mo/dashboard?tab=positions" class="<%= "positions".equals(tab) ? "active" : "" %>"><%= I18n.msg(request, "dash.mo.nav.positions") %></a>
            <a href="<%= ctx %>/mo/dashboard?tab=post" class="<%= "post".equals(tab) ? "active" : "" %>"><%= I18n.msg(request, "dash.mo.nav.post") %></a>
            <a href="<%= ctx %>/mo/dashboard?tab=messages" class="<%= "messages".equals(tab) ? "active" : "" %>"><%= I18n.msg(request, "dash.mo.nav.msg") %><c:if test="${moDmTotalUnread > 0}"><span class="nav-dm-badge" title="<%= I18n.msg(request, "dash.ta.nav.unreadTitle") %>">${moDmTotalUnread > 99 ? '99+' : moDmTotalUnread}</span></c:if></a>
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
    <script src="<%= ctx %>/js/ui.js?v=1" defer></script>
</body>
</html>
