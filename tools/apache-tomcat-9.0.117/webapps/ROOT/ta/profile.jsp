<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.bupt.ta.util.I18n" %>
<%@ page import="com.bupt.ta.model.Applicant" %>
<%
    Applicant applicant = (Applicant) request.getAttribute("applicant");
    if (applicant == null) {
        String ctx = request.getContextPath();
        if (session.getAttribute("taUser") != null) {
            response.sendRedirect(ctx + "/ta/dashboard");
        } else {
            response.sendRedirect(ctx + "/ta/auth");
        }
        return;
    }
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="<%= I18n.htmlLangAttr(request) %>">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= I18n.msg(request, "pc.ta.title") %></title>
    <link rel="stylesheet" href="<%= ctx %>/css/style.css?v=4">
</head>
<body class="profile-center">
    <div class="pc-page">
        <header class="pc-topbar">
            <h1><%= I18n.msg(request, "pc.h1") %></h1>
            <div class="pc-top-actions">
                <a href="<%= ctx %>/" class="pc-link"><%= I18n.msg(request, "common.home") %></a>
                <a href="<%= ctx %>/forum" class="pc-link"><%= I18n.msg(request, "common.forum") %></a>
                <a href="<%= ctx %>/assistant" class="pc-link"><%= I18n.msg(request, "common.assistantShort") %></a>
                <a href="<%= ctx %>/ta/dashboard" class="pc-link pc-link-primary"><%= I18n.msg(request, "common.dashboard") %></a>
                <a href="<%= ctx %>/ta/auth?logout=1" class="pc-link"><%= I18n.msg(request, "common.logout") %></a>
            </div>
        </header>

        <% if (request.getAttribute("error") != null) { %>
        <p class="msg-err"><%= request.getAttribute("error") %></p>
        <% } %>
        <% if (request.getAttribute("profileNotice") != null) { %>
        <p class="msg-ok"><%= request.getAttribute("profileNotice") %></p>
        <% } %>

        <div class="pc-layout">
            <aside class="pc-sidebar" aria-label="<%= I18n.msg(request, "pc.nav.aria") %>">
                <div class="pc-side-title"><%= I18n.msg(request, "pc.nav.title") %></div>
                <ul class="pc-nav">
                    <li><a href="#pc-overview" class="pc-nav-item is-active" data-nav><%= I18n.msg(request, "pc.nav.overview") %></a></li>
                    <li><a href="#pc-quick" class="pc-nav-item" data-nav><%= I18n.msg(request, "pc.nav.quick") %></a></li>
                    <li><a href="#pc-edit" class="pc-nav-item" data-nav><%= I18n.msg(request, "profile.edit.title") %></a></li>
                </ul>
            </aside>
            <main class="pc-main">
                <jsp:include page="/ta/section_profile.jsp"/>
                <jsp:include page="/ta/section_profile_edit.jsp"/>
            </main>
        </div>
    </div>
    <script>
      (function(){
        var nav = document.querySelectorAll('[data-nav]');
        var ids = ['pc-overview','pc-quick','pc-edit'];
        function setActive(){
          var mid = scrollY + innerHeight/3;
          var cur = ids[0];
          ids.forEach(function(id){
            var el = document.getElementById(id);
            if (el && el.offsetTop <= mid) cur = id;
          });
          nav.forEach(function(a){
            a.classList.toggle('is-active', a.getAttribute('href') === '#' + cur);
          });
        }
        addEventListener('scroll', setActive, {passive:true});
        setActive();
      })();
    </script>
</body>
</html>
