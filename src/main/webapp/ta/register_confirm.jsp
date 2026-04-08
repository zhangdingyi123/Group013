<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.bupt.ta.util.I18n" %>
<%
    String regName = (String) request.getAttribute("regName");
    String regEmail = (String) request.getAttribute("regEmail");
    String regStudentId = (String) request.getAttribute("regStudentId");
    String regPhone = (String) request.getAttribute("regPhone");
    if (regName == null) regName = "";
    if (regEmail == null) regEmail = "";
    if (regStudentId == null) regStudentId = "";
    if (regPhone == null) regPhone = "";
%>
<!DOCTYPE html>
<html lang="<%= I18n.htmlLangAttr(request) %>">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= I18n.msg(request, "reg.confirm.title") %></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=4">
</head>
<body class="auth-page">
    <div class="auth-bg" aria-hidden="true"></div>
    <main class="auth-shell auth-shell--wide">
        <div style="display:flex;justify-content:flex-end;margin-bottom:0.65rem;">
            <jsp:include page="/WEB-INF/jsp/lang_switch.jsp"/>
        </div>
        <a href="${pageContext.request.contextPath}/ta/register.jsp" class="auth-back"><%= I18n.msg(request, "reg.confirm.backStep") %></a>
        <div class="form-page">
        <h1><%= I18n.msg(request, "reg.confirm.h1") %></h1>
        <p class="confirm-tip"><%= I18n.msg(request, "reg.confirm.tip") %></p>
        <div class="confirm-list">
            <div class="confirm-row">
                <span class="confirm-label"><%= I18n.msg(request, "reg.confirm.name") %></span>
                <span class="confirm-value"><%= regName %></span>
            </div>
            <div class="confirm-row">
                <span class="confirm-label"><%= I18n.msg(request, "reg.confirm.sid") %></span>
                <span class="confirm-value"><%= regStudentId %></span>
            </div>
            <div class="confirm-row">
                <span class="confirm-label"><%= I18n.msg(request, "reg.confirm.email") %></span>
                <span class="confirm-value"><%= regEmail %></span>
            </div>
            <div class="confirm-row">
                <span class="confirm-label"><%= I18n.msg(request, "reg.confirm.phone") %></span>
                <span class="confirm-value"><%= regPhone %></span>
            </div>
        </div>
        <form method="post" action="${pageContext.request.contextPath}/ta/auth">
            <input type="hidden" name="action" value="register">
            <div class="form-actions">
                <a href="${pageContext.request.contextPath}/ta/register.jsp" class="btn btn-secondary"><%= I18n.msg(request, "reg.confirm.edit") %></a>
                <button type="submit" class="btn btn-primary"><%= I18n.msg(request, "reg.confirm.submit") %></button>
            </div>
        </form>
        <p class="links"><a href="${pageContext.request.contextPath}/ta/auth"><%= I18n.msg(request, "reg.confirm.login") %></a></p>
        </div>
    </main>
    <script src="${pageContext.request.contextPath}/js/ui.js?v=1" defer></script>
</body>
</html>
