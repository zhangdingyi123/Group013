<%@ page contentType="text/html;charset=UTF-8" language="java" %>
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
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>个人中心 - 应聘者</title>
    <link rel="stylesheet" href="<%= ctx %>/css/style.css?v=3">
    <style>
      :root{
        --pc-bg:#f5f6f8;
        --pc-card:#fff;
        --pc-border:#e8eaed;
        --pc-text:#1f2329;
        --pc-muted:#8f959e;
        --pc-accent:#2563eb;
        --pc-accent-soft:#eff6ff;
        --pc-radius:12px;
        --pc-shadow:0 1px 2px rgba(0,0,0,.04),0 4px 12px rgba(0,0,0,.06);
      }
      *{box-sizing:border-box}
      body{margin:0;font-family:-apple-system,BlinkMacSystemFont,"PingFang SC","Microsoft YaHei",sans-serif;background:var(--pc-bg);color:var(--pc-text);min-height:100vh;line-height:1.55;font-size:15px}
      .pc-page{max-width:1120px;margin:0 auto;padding:1.25rem 1rem 2.5rem}
      .pc-topbar{display:flex;align-items:center;justify-content:space-between;flex-wrap:wrap;gap:.75rem;margin-bottom:1.25rem}
      .pc-topbar h1{margin:0;font-size:1.25rem;font-weight:600;letter-spacing:-.02em;color:var(--pc-text)}
      .pc-top-actions{display:flex;align-items:center;gap:.35rem;flex-wrap:wrap}
      .pc-link{color:var(--pc-muted);text-decoration:none;font-size:.88rem;padding:.4rem .65rem;border-radius:8px}
      .pc-link:hover{color:var(--pc-accent);background:var(--pc-accent-soft)}
      .pc-link-primary{color:var(--pc-accent);font-weight:500}
      .pc-layout{display:grid;grid-template-columns:200px minmax(0,1fr);gap:1.5rem;align-items:start}
      @media (max-width:900px){.pc-layout{grid-template-columns:1fr}}
      .pc-sidebar{background:var(--pc-card);border-radius:var(--pc-radius);border:1px solid var(--pc-border);box-shadow:var(--pc-shadow);padding:.85rem 0;position:sticky;top:1rem}
      @media (max-width:900px){.pc-sidebar{position:relative;top:0;display:flex;overflow-x:auto;gap:0;padding:.5rem;scrollbar-width:thin}}
      .pc-side-title{font-size:.72rem;font-weight:600;color:var(--pc-muted);text-transform:uppercase;letter-spacing:.06em;padding:0 1rem .5rem}
      @media (max-width:900px){.pc-side-title{display:none}}
      .pc-nav{list-style:none;margin:0;padding:0}
      @media (max-width:900px){.pc-nav{display:flex;gap:.25rem}}
      .pc-nav a{display:block;padding:.55rem 1rem;font-size:.9rem;color:var(--pc-text);text-decoration:none;border-left:3px solid transparent}
      @media (max-width:900px){.pc-nav a{border-left:none;border-radius:8px;white-space:nowrap;padding:.45rem .85rem}}
      .pc-nav a:hover{background:#f7f8fa;color:var(--pc-accent)}
      .pc-nav a.is-active{color:var(--pc-accent);background:var(--pc-accent-soft);border-left-color:var(--pc-accent);font-weight:500}
      @media (max-width:900px){.pc-nav a.is-active{border-left:none}}
      .pc-main{display:flex;flex-direction:column;gap:1.25rem;min-width:0}
      .pc-card{background:var(--pc-card);border-radius:var(--pc-radius);border:1px solid var(--pc-border);box-shadow:var(--pc-shadow)}
      .pc-card-hd{padding:1rem 1.25rem;border-bottom:1px solid var(--pc-border);display:flex;align-items:center;justify-content:space-between;gap:.75rem;flex-wrap:wrap}
      .pc-card-hd h2{margin:0;font-size:1rem;font-weight:600}
      .pc-muted{font-size:.82rem;color:var(--pc-muted);line-height:1.45}
      .pc-card-bd{padding:1.25rem}
      .section.profile-edit{margin:0;border:none;box-shadow:none;padding:0}
      .form-group{margin-bottom:1.1rem}
      .form-group label{display:block;font-size:.86rem;font-weight:500;margin-bottom:.4rem;color:#374151}
      .form-group input,.form-group textarea{width:100%;padding:.65rem .85rem;border:1px solid var(--pc-border);border-radius:8px;font-size:.95rem;font-family:inherit;transition:border-color .15s,box-shadow .15s}
      .form-group input:focus,.form-group textarea:focus{outline:none;border-color:var(--pc-accent);box-shadow:0 0 0 3px rgba(37,99,235,.12)}
      .btn{display:inline-block;padding:.55rem 1.15rem;border:none;border-radius:8px;font-size:.9rem;font-weight:500;cursor:pointer;font-family:inherit}
      .btn-primary{background:var(--pc-accent);color:#fff}
      .btn-primary:hover{filter:brightness(1.05)}
      .msg-ok{color:#065f46;background:#d1fae5;padding:.65rem 1rem;border-radius:8px;margin-bottom:1rem;font-size:.88rem}
      .msg-err{color:#991b1b;background:#fee2e2;padding:.65rem 1rem;border-radius:8px;margin-bottom:1rem;font-size:.88rem}
      #pc-overview,#pc-quick,#pc-edit{scroll-margin-top:5.5rem}
      @media (max-width:640px){#pc-overview,#pc-quick,#pc-edit{scroll-margin-top:4rem}}
    </style>
</head>
<body>
    <div class="pc-page">
        <header class="pc-topbar">
            <h1>个人中心</h1>
            <div class="pc-top-actions">
                <a href="<%= ctx %>/" class="pc-link">首页</a>
                <a href="<%= ctx %>/ta/dashboard" class="pc-link pc-link-primary">工作台</a>
                <a href="<%= ctx %>/ta/auth?logout=1" class="pc-link">退出</a>
            </div>
        </header>

        <% if (request.getAttribute("error") != null) { %>
        <p class="msg-err"><%= request.getAttribute("error") %></p>
        <% } %>
        <% if (request.getAttribute("profileNotice") != null) { %>
        <p class="msg-ok"><%= request.getAttribute("profileNotice") %></p>
        <% } %>

        <div class="pc-layout">
            <aside class="pc-sidebar" aria-label="个人中心导航">
                <div class="pc-side-title">导航</div>
                <ul class="pc-nav">
                    <li><a href="#pc-overview" class="pc-nav-item is-active" data-nav>账户概览</a></li>
                    <li><a href="#pc-quick" class="pc-nav-item" data-nav>常用功能</a></li>
                    <li><a href="#pc-edit" class="pc-nav-item" data-nav>账号资料</a></li>
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
