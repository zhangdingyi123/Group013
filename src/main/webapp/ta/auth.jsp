<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<%@ page import="com.bupt.ta.util.I18n" %>
<%
    String ctx = request.getContextPath();
    String ruParam = request.getParameter("returnUrl");
    String taRegLink = ctx + "/ta/register.jsp";
    if (ruParam != null && !ruParam.isEmpty() && !ruParam.contains("..")
            && (ruParam.startsWith("/ta/") || "/assistant".equals(ruParam))) {
        taRegLink += "?returnUrl=" + URLEncoder.encode(ruParam, StandardCharsets.UTF_8);
    }
%>
<!DOCTYPE html>
<html lang="<%= I18n.htmlLangAttr(request) %>">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= I18n.msg(request, "auth.ta.title") %> - <%= I18n.msg(request, "common.sysName") %></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=4">
</head>
<body class="auth-page">
    <div class="auth-bg" aria-hidden="true"></div>
    <main class="auth-shell">
        <div style="display:flex;justify-content:flex-end;margin-bottom:0.65rem;">
            <jsp:include page="/WEB-INF/jsp/lang_switch.jsp"/>
        </div>
        <% request.setAttribute("authReturnPrefix", "/ta/"); %>
        <jsp:include page="/WEB-INF/jsp/auth_back_login_cancel.jsp"/>
        <div class="form-page">
        <h1><%= I18n.msg(request, "auth.ta.title") %></h1>
        <% if (request.getAttribute("error") != null) { %>
        <p class="error"><%= request.getAttribute("error") %></p>
        <% } %>
        <form method="post" action="${pageContext.request.contextPath}/ta/auth" autocomplete="off" id="ta-login-form">
            <input type="hidden" name="action" value="login">
            <% if (request.getParameter("returnUrl") != null && !request.getParameter("returnUrl").isEmpty()) { %>
            <input type="hidden" name="returnUrl" value="<%= request.getParameter("returnUrl") %>">
            <% } %>
            <div class="login-autofill-decoy" aria-hidden="true">
                <input type="text" tabindex="-1" autocomplete="username">
                <input type="password" tabindex="-1" autocomplete="current-password">
            </div>
            <div class="form-group">
                <label for="ta-login-email"><%= I18n.msg(request, "common.email") %></label>
                <input type="email" name="email" id="ta-login-email" required placeholder="your@email.com"
                       autocomplete="off" autocapitalize="none" autocorrect="off" spellcheck="false" readonly
                       onfocus="this.removeAttribute('readonly')">
            </div>
            <div class="form-group">
                <label for="ta-login-password"><%= I18n.msg(request, "common.password") %></label>
                <input type="password" name="password" id="ta-login-password" required
                       autocomplete="new-password" readonly
                       onfocus="this.removeAttribute('readonly')">
            </div>
            <div class="form-actions">
            <button type="submit" class="btn btn-primary"><%= I18n.msg(request, "common.login") %></button>
        </div>
        </form>
        <p class="links"><a href="<%= taRegLink %>"><%= I18n.msg(request, "auth.link.noAccount") %></a><span class="dot">·</span><a href="<%= ctx %>/"><%= I18n.msg(request, "auth.link.home") %></a></p>
        </div>
    </main>
    <script src="${pageContext.request.contextPath}/js/ui.js?v=1" defer></script>
</body>
</html>
