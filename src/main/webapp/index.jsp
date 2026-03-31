<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>助教招聘系统 - 北京邮电大学国际学院</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=3">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/assistant.css?v=1">
    <style>
      * { box-sizing: border-box; }
      body.home { margin: 0; font-family: "PingFang SC","Microsoft YaHei",sans-serif; min-height: 100vh;
        background: linear-gradient(160deg, #f0f9ff 0%, #e0f2fe 50%, #f8fafc 100%); color: #1e293b; }
      .home-top-pc { position: fixed; top: 1rem; right: 1.25rem; z-index: 20; }
      .home-top-pc a {
        display: inline-flex; align-items: center; gap: 0.45rem; padding: 0.45rem 0.9rem 0.45rem 0.65rem;
        background: rgba(255,255,255,0.92); border: 1px solid #e2e8f0; border-radius: 999px;
        text-decoration: none; color: #1e293b; font-size: 0.9rem; font-weight: 500;
        box-shadow: 0 2px 10px rgba(0,0,0,0.06); transition: box-shadow 0.2s ease, border-color 0.2s ease;
      }
      .home-top-pc a:hover { border-color: #93c5fd; box-shadow: 0 4px 16px rgba(37,99,235,0.12); }
      .home-top-pc .pc-icon { width: 28px; height: 28px; border-radius: 50%; background: #dbeafe; color: #2563eb;
        display: flex; align-items: center; justify-content: center; font-size: 0.95rem; flex-shrink: 0; }
      .container { max-width: 920px; margin: 0 auto; padding: 3rem 1.5rem; }
      header { text-align: center; margin-bottom: 2.5rem; }
      header h1 { font-size: 1.85rem; font-weight: 700; color: #1e293b; margin: 0; }
      .subtitle { color: #64748b; font-size: 0.95rem; margin-top: 0.5rem; }
      .cards { display: grid; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); gap: 1.5rem; }
      .card { display: block; background: #fff; border-radius: 10px; padding: 1.75rem; text-decoration: none; color: inherit;
        box-shadow: 0 4px 12px rgba(0,0,0,0.08); border: 1px solid #e2e8f0;
        transition: transform 0.2s ease, box-shadow 0.2s ease; }
      .card:hover { transform: translateY(-3px); box-shadow: 0 10px 40px rgba(0,0,0,0.1); border-color: #dbeafe; }
      .card-icon { width: 48px; height: 48px; display: flex; align-items: center; justify-content: center; font-size: 1.5rem;
        background: #dbeafe; color: #2563eb; border-radius: 6px; margin-bottom: 1rem; }
      .card h2 { font-size: 1.15rem; font-weight: 600; margin: 0 0 0.4rem; color: #1e293b; }
      .card p { font-size: 0.9rem; color: #64748b; margin: 0; line-height: 1.5; }
      footer { text-align: center; margin-top: 2.5rem; font-size: 0.8rem; color: #64748b; }
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
    <div class="home-top-pc" role="navigation" aria-label="个人中心">
        <a href="<%= personalCenterHref %>"><span class="pc-icon" aria-hidden="true">👤</span>个人中心</a>
    </div>
    <div class="container">
        <header>
            <h1>助教招聘系统</h1>
            <p class="subtitle">北京邮电大学国际学院 · TA Recruitment System</p>
        </header>
        <main class="cards">
            <a href="${pageContext.request.contextPath}/ta/auth" class="card">
                <div class="card-icon">👤</div>
                <h2>应聘者入口</h2>
                <p>创建档案、上传简历、申请岗位、查询状态</p>
            </a>
            <a href="${pageContext.request.contextPath}/mo/auth" class="card">
                <div class="card-icon">📋</div>
                <h2>课程组织者入口</h2>
                <p>发布岗位、筛选应聘者、录用管理</p>
            </a>
            <a href="<%= request.getContextPath() %>/admin/auth" class="card">
                <div class="card-icon">📊</div>
                <h2>管理员 · 工作负荷</h2>
                <p>登录后查看助教整体工作负荷</p>
            </a>
            <a href="${pageContext.request.contextPath}/forum" class="card">
                <div class="card-icon">💬</div>
                <h2>交流论坛</h2>
                <p>公开浏览；登录后可发帖与回复，与大家一起交流</p>
            </a>
            <a href="${pageContext.request.contextPath}/assistant" class="card">
                <div class="card-icon">🤖</div>
                <h2>智能小助手</h2>
                <p>Kimi K2.5 / 通义千问，解答申请与系统使用问题</p>
            </a>
        </main>
        <footer>
            <p>数据存储于文本/JSON 文件，无需数据库</p>
        </footer>
    </div>
    <a href="${pageContext.request.contextPath}/assistant" class="assistant-fab" title="打开智能小助手"><span class="assistant-fab-icon" aria-hidden="true">🤖</span>小助手</a>
</body>
</html>
