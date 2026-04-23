<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.bupt.ta.util.I18n" %>
<!DOCTYPE html>
<html lang="<%= I18n.htmlLangAttr(request) %>">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= I18n.msg(request, "reg.admin.title") %> - <%= I18n.msg(request, "common.sysName") %></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=4">
</head>
<body class="auth-page auth-page--admin">
    <div class="auth-bg" aria-hidden="true"></div>
    <main class="auth-shell">
        <div style="display:flex;justify-content:flex-end;margin-bottom:0.65rem;">
            <jsp:include page="/WEB-INF/jsp/lang_switch.jsp"/>
        </div>
        <% request.setAttribute("authRegisterLoginPath", "/admin/auth");
           request.setAttribute("authReturnPrefix", "/admin/"); %>
        <jsp:include page="/WEB-INF/jsp/auth_back_register_to_login.jsp"/>
        <div class="form-page">
        <h1><%= I18n.msg(request, "reg.admin.title") %></h1>
        <% if (request.getAttribute("error") != null) { %>
        <p class="error"><%= request.getAttribute("error") %></p>
        <% } %>
        <form method="post" action="${pageContext.request.contextPath}/admin/auth">
            <input type="hidden" name="action" value="register">
            <div class="form-group">
                <label><%= I18n.msg(request, "auth.label.name") %></label>
                <input type="text" name="name" placeholder="<%= I18n.msg(request, "reg.admin.name.opt") %>">
            </div>
            <div class="form-group">
                <label><%= I18n.msg(request, "common.email") %> *</label>
                <input type="email" name="email" required placeholder="admin@bupt.edu.cn">
            </div>
            <div class="form-group">
                <label><%= I18n.msg(request, "common.password") %> *</label>
                <input type="password" name="password" required>
            </div>
            <div class="form-actions">
                <button type="submit" class="btn btn-primary"><%= I18n.msg(request, "mo.register.btn") %></button>
            </div>
        </form>
        <p class="links"><a href="${pageContext.request.contextPath}/admin/auth"><%= I18n.msg(request, "auth.link.hasAccount") %></a><span class="dot">·</span><a href="${pageContext.request.contextPath}/"><%= I18n.msg(request, "auth.link.home") %></a></p>
        </div>
    </main>
    <script src="${pageContext.request.contextPath}/js/ui.js?v=1" defer></script>
</body>
</html>
