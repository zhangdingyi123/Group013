<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="com.bupt.ta.model.Applicant" %>
<%@ page import="com.bupt.ta.util.I18n" %>
<%
    Applicant applicant = (Applicant) request.getAttribute("applicant");
    Boolean taGuestMode = (Boolean) request.getAttribute("taGuestMode");
    if (applicant == null && !Boolean.TRUE.equals(taGuestMode)) {
        String ctx = request.getContextPath();
        if (session.getAttribute("taUser") != null) {
            response.sendRedirect(ctx + "/ta/dashboard");
        } else {
            response.sendRedirect(ctx + "/ta/auth");
        }
        return;
    }
    String tab = (String) request.getAttribute("taDashboardTab");
    if (tab == null) tab = Boolean.TRUE.equals(taGuestMode) ? "jobs" : "resume";
    String pageTitle = I18n.msg(request, "dash.ta.tab.resume");
    if ("jobs".equals(tab)) pageTitle = I18n.msg(request, "dash.ta.tab.jobs");
    else if ("applications".equals(tab)) pageTitle = I18n.msg(request, "dash.ta.tab.apps");
    else if ("messages".equals(tab)) pageTitle = I18n.msg(request, "dash.ta.tab.msg");
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="<%= I18n.htmlLangAttr(request) %>">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= I18n.msg(request, "dash.ta.pageTitle", pageTitle) %></title>
    <link rel="stylesheet" href="<%= ctx %>/css/style.css?v=4">
</head>
<body class="dashboard-app">
    <div class="dashboard">
        <div class="page-header has-lang">
            <jsp:include page="/WEB-INF/jsp/lang_switch.jsp"/>
            <a href="<%= ctx %>/" class="back-link"><%= I18n.msg(request, "header.backHome") %></a>
            <h1><%= I18n.msg(request, "dash.ta.title") %></h1>
            <div class="header-actions">
                <a href="<%= ctx %>/forum" class="header-nav-link"><%= I18n.msg(request, "header.forum") %></a>
                <a href="<%= ctx %>/assistant" class="header-nav-link"><%= I18n.msg(request, "header.assistant") %></a>
                <% if (Boolean.TRUE.equals(taGuestMode)) { %>
                <c:url var="taLoginProfile" value="/ta/auth"><c:param name="returnUrl" value="/ta/profile"/></c:url>
                <a href="${taLoginProfile}" class="header-nav-link"><%= I18n.msg(request, "header.profile") %></a>
                <c:url var="taLoginBar" value="/ta/auth"><c:param name="returnUrl" value="/ta/dashboard?tab=jobs"/></c:url>
                <a href="${taLoginBar}" class="logout"><%= I18n.msg(request, "common.login") %></a>
                <% } else { %>
                <a href="<%= ctx %>/ta/profile" class="header-nav-link"><%= I18n.msg(request, "header.profile") %></a>
                <a href="<%= ctx %>/ta/auth?logout=1" class="logout"><%= I18n.msg(request, "common.logout") %></a>
                <% } %>
            </div>
        </div>

        <c:url var="navResume" value="/ta/dashboard"><c:param name="tab" value="resume"/></c:url>
        <c:url var="navJobs" value="/ta/dashboard"><c:param name="tab" value="jobs"/></c:url>
        <c:url var="navApplications" value="/ta/dashboard"><c:param name="tab" value="applications"/></c:url>
        <c:url var="navMessages" value="/ta/dashboard"><c:param name="tab" value="messages"/></c:url>
        <c:url var="authResume" value="/ta/auth"><c:param name="returnUrl" value="/ta/dashboard?tab=resume"/></c:url>
        <c:url var="authApps" value="/ta/auth"><c:param name="returnUrl" value="/ta/dashboard?tab=applications"/></c:url>
        <c:url var="authMsg" value="/ta/auth"><c:param name="returnUrl" value="/ta/dashboard?tab=messages"/></c:url>
        <nav class="ta-subnav" aria-label="<%= I18n.msg(request, "dash.ta.title") %>">
            <% if (Boolean.TRUE.equals(taGuestMode)) { %>
            <a href="${authResume}" class=""><%= I18n.msg(request, "dash.ta.nav.resume") %></a>
            <a href="${navJobs}" class="active"><%= I18n.msg(request, "dash.ta.nav.jobs") %></a>
            <a href="${authApps}"><%= I18n.msg(request, "dash.ta.nav.apps") %></a>
            <a href="${authMsg}"><%= I18n.msg(request, "dash.ta.nav.msg") %></a>
            <% } else { %>
            <a href="${navResume}" class="<%= "resume".equals(tab) ? "active" : "" %>"><%= I18n.msg(request, "dash.ta.nav.resume") %></a>
            <a href="${navJobs}" class="<%= "jobs".equals(tab) ? "active" : "" %>"><%= I18n.msg(request, "dash.ta.nav.jobs") %></a>
            <a href="${navApplications}" class="<%= "applications".equals(tab) ? "active" : "" %>"><%= I18n.msg(request, "dash.ta.nav.apps") %></a>
            <a href="${navMessages}" class="<%= "messages".equals(tab) ? "active" : "" %>"><%= I18n.msg(request, "dash.ta.nav.msg") %><c:if test="${taDmTotalUnread > 0}"><span class="nav-dm-badge" title="<%= I18n.msg(request, "dash.ta.nav.unreadTitle") %>">${taDmTotalUnread > 99 ? '99+' : taDmTotalUnread}</span></c:if></a>
            <% } %>
        </nav>

        <% if (request.getAttribute("error") != null) { %>
        <p class="msg-err"><%= request.getAttribute("error") %></p>
        <% } %>
        <% if (request.getAttribute("applyMessage") != null) { %>
        <p class="msg-ok"><%= request.getAttribute("applyMessage") %></p>
        <% } %>
        <% if (Boolean.TRUE.equals(taGuestMode)) { %>
        <p class="section-desc" style="margin-top:0"><%= I18n.msg(request, "dash.ta.guest.banner") %></p>
        <% } %>

        <c:choose>
            <c:when test="${taDashboardTab eq 'jobs'}">
                <jsp:include page="/ta/section_jobs.jsp"/>
            </c:when>
            <c:when test="${taDashboardTab eq 'applications'}">
                <jsp:include page="/ta/section_applications.jsp"/>
            </c:when>
            <c:when test="${taDashboardTab eq 'messages'}">
                <jsp:include page="/ta/section_messages.jsp"/>
            </c:when>
            <c:otherwise>
                <jsp:include page="/ta/section_resume.jsp"/>
            </c:otherwise>
        </c:choose>
    </div>
    <script src="<%= ctx %>/js/ui.js?v=1" defer></script>
</body>
</html>
