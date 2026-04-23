<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<%@ page import="com.bupt.ta.util.I18n" %>
<%
    String ctx = request.getContextPath();
    String loginServlet = (String) request.getAttribute("authRegisterLoginPath");
    if (loginServlet == null) {
        loginServlet = "/ta/auth";
    }
    String prefix = (String) request.getAttribute("authReturnPrefix");
    if (prefix == null) {
        prefix = "/ta/";
    }
    String ru = request.getParameter("returnUrl");
    if (ru == null || ru.isEmpty()) {
        ru = (String) request.getAttribute("regReturnUrl");
    }
    String loginHref = ctx + loginServlet;
    if (ru != null && !ru.isEmpty() && ru.startsWith(prefix) && !ru.contains("..")) {
        loginHref += "?returnUrl=" + URLEncoder.encode(ru, StandardCharsets.UTF_8);
    }
%>
<a href="<%= loginHref %>" class="auth-back"><%= I18n.msg(request, "common.backLogin") %></a>
