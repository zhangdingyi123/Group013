<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.bupt.ta.util.I18n" %>
<%
    String ruReg = request.getParameter("returnUrl");
    if (ruReg == null || ruReg.isEmpty()) {
        ruReg = (String) request.getAttribute("regReturnUrl");
    }
    boolean safeTaReturn = ruReg != null && !ruReg.isEmpty() && ruReg.startsWith("/ta/") && !ruReg.contains("..");
%>
<!DOCTYPE html>
<html lang="<%= I18n.htmlLangAttr(request) %>">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= I18n.msg(request, "auth.register.ta.title") %> - <%= I18n.msg(request, "common.sysName") %></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=4">
</head>
<body class="auth-page">
    <div class="auth-bg" aria-hidden="true"></div>
    <main class="auth-shell">
        <div style="display:flex;justify-content:flex-end;margin-bottom:0.65rem;">
            <jsp:include page="/WEB-INF/jsp/lang_switch.jsp"/>
        </div>
        <% request.setAttribute("authRegisterLoginPath", "/ta/auth");
           request.setAttribute("authReturnPrefix", "/ta/"); %>
        <jsp:include page="/WEB-INF/jsp/auth_back_register_to_login.jsp"/>
        <div class="form-page">
        <h1><%= I18n.msg(request, "auth.register.ta.title") %></h1>
        <% if (request.getAttribute("error") != null) { %>
        <p class="error"><%= request.getAttribute("error") %></p>
        <% } %>
        <form method="post" action="${pageContext.request.contextPath}/ta/auth">
            <input type="hidden" name="action" value="confirm">
            <% if (safeTaReturn) { %>
            <input type="hidden" name="returnUrl" value="<%= ruReg %>">
            <% } %>
            <div class="form-group">
                <label><%= I18n.msg(request, "auth.label.name") %></label>
                <input type="text" name="name" autocomplete="name" required placeholder="<%= I18n.msg(request, "auth.placeholder.name") %>" value="<%= request.getAttribute("regName") != null ? request.getAttribute("regName") : "" %>">
            </div>
            <div class="form-group">
                <label><%= I18n.msg(request, "auth.label.studentId") %></label>
                <input type="text" name="studentId" autocomplete="username" required placeholder="<%= I18n.msg(request, "auth.placeholder.studentId") %>" value="<%= request.getAttribute("regStudentId") != null ? request.getAttribute("regStudentId") : "" %>">
            </div>
            <div class="form-group">
                <label><%= I18n.msg(request, "common.email") %> *</label>
                <input type="email" name="email" autocomplete="email" required placeholder="your@email.com" value="<%= request.getAttribute("regEmail") != null ? request.getAttribute("regEmail") : "" %>">
            </div>
            <div class="form-group">
                <label><%= I18n.msg(request, "auth.label.phone") %></label>
                <input type="tel" name="phone" autocomplete="tel" inputmode="tel" required placeholder="<%= I18n.msg(request, "auth.placeholder.phone") %>" value="<%= request.getAttribute("regPhone") != null ? request.getAttribute("regPhone") : "" %>">
            </div>
            <div class="form-group">
                <label><%= I18n.msg(request, "common.password") %> *</label>
                <input type="password" name="password" autocomplete="new-password" required placeholder="<%= I18n.msg(request, "auth.placeholder.password.set") %>">
            </div>
            <div class="form-actions">
            <button type="submit" class="btn btn-primary"><%= I18n.msg(request, "auth.ta.next") %></button>
        </div>
        </form>
        <p class="links"><a href="${pageContext.request.contextPath}/ta/auth"><%= I18n.msg(request, "auth.link.hasAccount") %></a><span class="dot">·</span><a href="${pageContext.request.contextPath}/"><%= I18n.msg(request, "auth.link.home") %></a></p>
        </div>
    </main>
    <script src="${pageContext.request.contextPath}/js/ui.js?v=1" defer></script>
</body>
</html>
