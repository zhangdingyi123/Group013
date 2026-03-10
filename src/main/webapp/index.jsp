<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>助教招聘系统 - 北京邮电大学国际学院</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=3">
    <style>
      /* 内联关键样式，确保外部 CSS 未加载时首页仍有优化效果 */
      * { box-sizing: border-box; }
      body.home { margin: 0; font-family: "PingFang SC","Microsoft YaHei",sans-serif; min-height: 100vh;
        background: linear-gradient(160deg, #f0f9ff 0%, #e0f2fe 50%, #f8fafc 100%); color: #1e293b; }
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
            <a href="${pageContext.request.contextPath}/admin/workload" class="card">
                <div class="card-icon">📊</div>
                <h2>管理员 · 工作负荷</h2>
                <p>查看助教整体工作负荷</p>
            </a>
        </main>
        <footer>
            <p>数据存储于文本/JSON 文件，无需数据库</p>
        </footer>
    </div>
</body>
</html>
