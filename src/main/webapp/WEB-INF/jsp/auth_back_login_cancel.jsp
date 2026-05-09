<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.bupt.ta.util.I18n" %>
<%
    String ctx = request.getContextPath();
    String prefix = (String) request.getAttribute("authReturnPrefix");
    if (prefix == null) {
        prefix = "/ta/";
    }
    String ru = request.getParameter("returnUrl");
    boolean safeReturn = ru != null && !ru.isEmpty() && ru.startsWith(prefix) && !ru.contains("..");
    String cancelHref = safeReturn ? (ctx + ru) : null;
%>
<a href="<%= cancelHref != null ? cancelHref : "#" %>" class="auth-back" id="auth-back-cancel"><%= I18n.msg(request, "common.backPrev") %></a>
<% if (cancelHref == null) { %>
<script>(function(){
  var a = document.getElementById('auth-back-cancel');
  if (!a) return;
  a.addEventListener('click', function(e) {
    e.preventDefault();
    if (window.history.length > 1) { history.back(); }
    else { window.location.href = '<%= ctx %>/'; }
  });
})();</script>
<% } %>
