<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>应聘者登录 - 助教招聘系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=3">
    <style>
      *{box-sizing:border-box} body{margin:0;font-family:"PingFang SC","Microsoft YaHei",sans-serif;background:#f8fafc;color:#1e293b;min-height:100vh;padding:2rem 0}
      .form-page{position:relative;max-width:400px;margin:0 auto;padding:2.25rem;background:#fff;border-radius:10px;box-shadow:0 4px 12px rgba(0,0,0,.08);border:1px solid #e2e8f0}
      .form-page h1{font-size:1.4rem;font-weight:600;margin:0 0 1.5rem;text-align:center}
      .form-group{margin-bottom:1.1rem}.form-group label{display:block;font-size:.9rem;font-weight:500;margin-bottom:.4rem}
      .form-group input{width:100%;padding:.65rem .85rem;border:1px solid #e2e8f0;border-radius:6px;font-size:.95rem}
      .form-group input:focus{outline:none;border-color:#2563eb;box-shadow:0 0 0 3px #dbeafe}
      .form-actions{margin-top:1.25rem}
      .btn{display:inline-block;padding:.65rem 1.35rem;border:none;border-radius:6px;font-size:.95rem;font-weight:500;cursor:pointer;width:100%;text-align:center;background:#2563eb;color:#fff;font-family:inherit}
      .btn:hover{background:#1d4ed8}.error{color:#dc2626;font-size:.9rem;margin-bottom:1rem;padding:.6rem .85rem;background:#fef2f2;border-radius:6px}
      .links{text-align:center;margin-top:1.25rem;font-size:.9rem;color:#64748b}.links a{color:#2563eb;text-decoration:none}.links a:hover{text-decoration:underline}
      .login-autofill-decoy{position:absolute;left:-9999px;width:1px;height:1px;overflow:hidden;opacity:0;pointer-events:none}
    </style>
</head>
<body>
    <div class="form-page">
        <h1>应聘者登录</h1>
        <% if (request.getAttribute("error") != null) { %>
        <p class="error"><%= request.getAttribute("error") %></p>
        <% } %>
        <form method="post" action="${pageContext.request.contextPath}/ta/auth" autocomplete="off" id="ta-login-form">
            <input type="hidden" name="action" value="login">
            <%-- 诱饵字段：部分浏览器会把已保存账号填到这里，避免落到真实输入框 --%>
            <div class="login-autofill-decoy" aria-hidden="true">
                <input type="text" tabindex="-1" autocomplete="username">
                <input type="password" tabindex="-1" autocomplete="current-password">
            </div>
            <div class="form-group">
                <label for="ta-login-email">邮箱</label>
                <input type="email" name="email" id="ta-login-email" required placeholder="your@email.com"
                       autocomplete="off" autocapitalize="none" autocorrect="off" spellcheck="false" readonly
                       onfocus="this.removeAttribute('readonly')">
            </div>
            <div class="form-group">
                <label for="ta-login-password">密码</label>
                <input type="password" name="password" id="ta-login-password" required
                       autocomplete="new-password" readonly
                       onfocus="this.removeAttribute('readonly')">
            </div>
            <div class="form-actions">
            <button type="submit" class="btn btn-primary">登录</button>
        </div>
        </form>
        <p class="links"><a href="${pageContext.request.contextPath}/ta/register.jsp">没有账号？注册</a> &nbsp;|&nbsp; <a href="${pageContext.request.contextPath}/">返回首页</a></p>
    </div>
</body>
</html>
