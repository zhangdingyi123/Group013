<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.bupt.ta.util.I18n" %>
<!DOCTYPE html>
<html lang="<%= I18n.htmlLangAttr(request) %>">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= I18n.msg(request, "home.pageTitle") %></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=4">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/assistant.css?v=9">
    <style>
      * { box-sizing: border-box; }
      body.home { margin: 0; font-family: "PingFang SC","Microsoft YaHei",sans-serif; min-height: 100vh;
        background: linear-gradient(165deg, #eef6ff 0%, #e0f2fe 38%, #f1f5f9 72%, #f8fafc 100%); color: #1e293b; }
      .home-top-pc {
        position: fixed; top: 0.85rem; right: 1rem; left: 1rem; z-index: 20;
        display: flex; align-items: center; justify-content: flex-end; flex-wrap: wrap; gap: 0.5rem 0.65rem;
        max-width: 960px; margin-left: auto; margin-right: auto;
      }
      .home-top-pc .home-quick-nav {
        display: inline-flex; align-items: center; gap: 0.35rem; flex-wrap: wrap;
        padding: 0.35rem 0.65rem; background: rgba(255,255,255,0.88); border: 1px solid #e2e8f0; border-radius: 999px;
        box-shadow: 0 2px 10px rgba(0,0,0,0.05); font-size: 0.82rem;
      }
      .home-top-pc .home-quick-nav a { color: #475569; text-decoration: none; padding: 0.2rem 0.45rem; border-radius: 6px; }
      .home-top-pc .home-quick-nav a:hover { color: #2563eb; background: #eff6ff; }
      .home-top-pc .home-quick-nav span { color: #cbd5e1; user-select: none; }
      .home-top-pc > a.personal-center-btn {
        display: inline-flex; align-items: center; gap: 0.45rem; padding: 0.45rem 0.9rem 0.45rem 0.65rem;
        background: rgba(255,255,255,0.92); border: 1px solid #e2e8f0; border-radius: 999px;
        text-decoration: none; color: #1e293b; font-size: 0.9rem; font-weight: 500;
        box-shadow: 0 2px 10px rgba(0,0,0,0.06); transition: box-shadow 0.2s ease, border-color 0.2s ease;
      }
      .home-top-pc > a.personal-center-btn:hover { border-color: #93c5fd; box-shadow: 0 4px 16px rgba(37,99,235,0.12); }
      .home-top-pc .pc-icon { width: 28px; height: 28px; border-radius: 50%; background: #dbeafe; color: #2563eb;
        display: flex; align-items: center; justify-content: center; font-size: 0.95rem; flex-shrink: 0; }
      .container { max-width: 920px; margin: 0 auto; padding: 4.25rem 1.5rem 2.5rem; }
      header {
        text-align: center; margin-bottom: 2.35rem;
        padding: 2rem 1.5rem 2.25rem;
        background: rgba(255,255,255,0.72);
        border: 1px solid rgba(226,232,240,0.9);
        border-radius: 20px;
        box-shadow: 0 4px 32px rgba(15,23,42,0.06), 0 1px 0 rgba(255,255,255,0.9) inset;
        backdrop-filter: blur(12px);
        -webkit-backdrop-filter: blur(12px);
        position: relative;
        overflow: hidden;
      }
      header::before {
        content: ""; position: absolute; left: 0; right: 0; top: 0; height: 4px;
        background: linear-gradient(90deg, #3b82f6, #6366f1, #8b5cf6);
        opacity: 0.92;
      }
      header h1 { font-size: clamp(1.5rem, 4vw, 1.95rem); font-weight: 700; color: #0f172a; margin: 0; letter-spacing: -0.03em; }
      .subtitle { color: #64748b; font-size: 0.95rem; margin-top: 0.65rem; line-height: 1.55; }
      .home-section { margin-bottom: 2.25rem; }
      .home-section:last-of-type { margin-bottom: 0; }
      .home-section-title {
        font-size: 0.72rem; font-weight: 700; letter-spacing: 0.08em; text-transform: uppercase;
        color: #94a3b8; margin: 0 0 0.35rem;
      }
      .home-section-desc { font-size: 0.88rem; color: #64748b; margin: 0 0 1rem; line-height: 1.5; }
      .cards { display: grid; gap: 1.25rem; }
      .cards--identity { grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); }
      .cards--tools { grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); }
      .card {
        display: block; background: #fff; border-radius: 14px; padding: 1.75rem 1.65rem; text-decoration: none; color: inherit;
        box-shadow: 0 2px 8px rgba(15,23,42,0.06), 0 8px 24px rgba(15,23,42,0.04); border: 1px solid rgba(226,232,240,0.95);
        transition: transform 0.22s ease, box-shadow 0.22s ease, border-color 0.2s ease;
      }
      .card:hover { transform: translateY(-4px); box-shadow: 0 12px 40px rgba(37,99,235,0.12); border-color: #bfdbfe; }
      .card--emphasis { border-color: #bfdbfe; box-shadow: 0 6px 20px rgba(37,99,235,0.08); }
      .card--soft { padding: 1.35rem 1.5rem; background: #fafbfc; }
      .card-icon { width: 48px; height: 48px; display: flex; align-items: center; justify-content: center; font-size: 1.5rem;
        background: #dbeafe; color: #2563eb; border-radius: 6px; margin-bottom: 1rem; }
      .card--soft .card-icon { width: 40px; height: 40px; font-size: 1.25rem; background: #eef2ff; color: #4f46e5; }
      .card h2 { font-size: 1.15rem; font-weight: 600; margin: 0 0 0.4rem; color: #1e293b; }
      .card p { font-size: 0.9rem; color: #64748b; margin: 0; line-height: 1.5; }
      .home-admin-row {
        display: flex; flex-wrap: wrap; align-items: center; justify-content: space-between; gap: 0.75rem 1rem;
        padding: 1rem 1.25rem; background: rgba(255,255,255,0.75); border: 1px solid #e2e8f0; border-radius: 10px;
        text-decoration: none; color: #475569; font-size: 0.9rem; transition: border-color 0.2s, box-shadow 0.2s;
      }
      .home-admin-row:hover { border-color: #c7d2fe; box-shadow: 0 4px 16px rgba(79,70,229,0.08); color: #1e293b; }
      .home-admin-row strong { color: #334155; font-weight: 600; }
      .home-admin-row span.meta { font-size: 0.82rem; color: #94a3b8; }
      footer { text-align: center; margin-top: 2rem; font-size: 0.8rem; color: #64748b; }
    </style>
</head>
<body class="home">
    <%
        String ctx = request.getContextPath();
        String personalCenterHref = ctx + "/personal-center";
        if (session.getAttribute("taUser") != null) {
            personalCenterHref = ctx + "/ta/profile";
        } else if (session.getAttribute("moUser") != null) {
            personalCenterHref = ctx + "/mo/profile";
        } else if (session.getAttribute("adminUser") != null) {
            personalCenterHref = ctx + "/admin/workload";
        }
    %>
    <div class="home-top-pc">
        <jsp:include page="/WEB-INF/jsp/lang_switch.jsp"/>
        <nav class="home-quick-nav" aria-label="<%= I18n.msg(request, "home.quickNavAria") %>">
            <a href="${pageContext.request.contextPath}/forum"><%= I18n.msg(request, "common.forum") %></a>
            <span aria-hidden="true">·</span>
            <a href="${pageContext.request.contextPath}/assistant"><%= I18n.msg(request, "common.assistant") %></a>
        </nav>
        <a href="<%= personalCenterHref %>" class="personal-center-btn"><span class="pc-icon" aria-hidden="true">👤</span><%= I18n.msg(request, "common.personalCenter") %></a>
    </div>
    <div class="container">
        <header>
            <h1><%= I18n.msg(request, "home.title") %></h1>
            <p class="subtitle"><%= I18n.msg(request, "home.subtitle") %></p>
        </header>
        <main>
            <section class="home-section" aria-labelledby="home-identity">
                <h2 id="home-identity" class="home-section-title"><%= I18n.msg(request, "home.section.identity") %></h2>
                <p class="home-section-desc"><%= I18n.msg(request, "home.section.identity.desc") %></p>
                <div class="cards cards--identity">
                    <a href="${pageContext.request.contextPath}/ta/dashboard" class="card card--emphasis">
                        <div class="card-icon">👤</div>
                        <h2><%= I18n.msg(request, "home.card.ta") %></h2>
                        <p><%= I18n.msg(request, "home.card.ta.desc") %></p>
                    </a>
                    <a href="${pageContext.request.contextPath}/mo/dashboard" class="card card--emphasis">
                        <div class="card-icon">📋</div>
                        <h2><%= I18n.msg(request, "home.card.mo") %></h2>
                        <p><%= I18n.msg(request, "home.card.mo.desc") %></p>
                    </a>
                </div>
            </section>
            <section class="home-section" aria-labelledby="home-tools">
                <h2 id="home-tools" class="home-section-title"><%= I18n.msg(request, "home.section.tools") %></h2>
                <p class="home-section-desc"><%= I18n.msg(request, "home.section.tools.desc") %></p>
                <div class="cards cards--tools">
                    <a href="${pageContext.request.contextPath}/forum" class="card card--soft">
                        <div class="card-icon">💬</div>
                        <h2><%= I18n.msg(request, "home.card.forum") %></h2>
                        <p><%= I18n.msg(request, "home.card.forum.desc") %></p>
                    </a>
                    <a href="${pageContext.request.contextPath}/assistant" class="card card--soft">
                        <div class="card-icon">🤖</div>
                        <h2><%= I18n.msg(request, "home.card.assistant") %></h2>
                        <p><%= I18n.msg(request, "home.card.assistant.desc") %></p>
                    </a>
                </div>
            </section>
            <section class="home-section" aria-labelledby="home-admin">
                <h2 id="home-admin" class="home-section-title"><%= I18n.msg(request, "home.section.admin") %></h2>
                <p class="home-section-desc"><%= I18n.msg(request, "home.section.admin.desc") %></p>
                <a href="<%= request.getContextPath() %>/admin/auth" class="home-admin-row">
                    <span><%= I18n.msg(request, "home.admin.row") %></span>
                    <span class="meta"><%= I18n.msg(request, "home.admin.meta") %></span>
                </a>
            </section>
        </main>
        <footer>
            <p><%= I18n.msg(request, "home.footer") %></p>
        </footer>
    </div>
    <a href="${pageContext.request.contextPath}/assistant" class="assistant-fab" title="<%= I18n.msg(request, "home.fab.title") %>"><span class="assistant-fab-icon" aria-hidden="true">🤖</span><%= I18n.msg(request, "home.fab.label") %></a>
</body>
</html>
