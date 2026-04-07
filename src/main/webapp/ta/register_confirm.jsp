<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String regName = (String) request.getAttribute("regName");
    String regEmail = (String) request.getAttribute("regEmail");
    String regStudentId = (String) request.getAttribute("regStudentId");
    String regPhone = (String) request.getAttribute("regPhone");
    if (regName == null) regName = "";
    if (regEmail == null) regEmail = "";
    if (regStudentId == null) regStudentId = "";
    if (regPhone == null) regPhone = "";
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>个人信息核准 - 助教招聘系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=3">
    <style>
      *{box-sizing:border-box} body{margin:0;font-family:"PingFang SC","Microsoft YaHei",sans-serif;background:#f8fafc;color:#1e293b;min-height:100vh;padding:2rem 0}
      .form-page{max-width:420px;margin:0 auto;padding:2.25rem;background:#fff;border-radius:10px;box-shadow:0 4px 12px rgba(0,0,0,.08);border:1px solid #e2e8f0}
      .form-page h1{font-size:1.35rem;font-weight:600;margin:0 0 1.25rem;text-align:center;color:#1e293b}
      .confirm-tip{font-size:.9rem;color:#64748b;margin-bottom:1.25rem;text-align:center}
      .confirm-list{background:#f8fafc;border-radius:8px;padding:1rem 1.25rem;margin-bottom:1.5rem;border:1px solid #e2e8f0}
      .confirm-row{display:flex;padding:.5rem 0;border-bottom:1px solid #e2e8f0;font-size:.95rem}
      .confirm-row:last-child{border-bottom:none}
      .confirm-label{width:5rem;color:#64748b;flex-shrink:0}
      .confirm-value{font-weight:500;color:#1e293b}
      .form-actions{display:flex;gap:.75rem;margin-top:1.25rem}
      .btn{display:inline-block;padding:.65rem 1.35rem;border:none;border-radius:6px;font-size:.95rem;font-weight:500;cursor:pointer;text-align:center;font-family:inherit;text-decoration:none}
      .btn-primary{background:#2563eb;color:#fff;flex:1}.btn-primary:hover{background:#1d4ed8}
      .btn-secondary{background:#e2e8f0;color:#475569}.btn-secondary:hover{background:#cbd5e1}
      .links{text-align:center;margin-top:1.25rem;font-size:.9rem;color:#64748b}.links a{color:#2563eb;text-decoration:none}
    </style>
</head>
<body>
    <div class="form-page">
        <h1>个人信息核准</h1>
        <p class="confirm-tip">请核对以下信息，确认无误后点击「确认注册」。</p>
        <div class="confirm-list">
            <div class="confirm-row">
                <span class="confirm-label">姓名</span>
                <span class="confirm-value"><%= regName %></span>
            </div>
            <div class="confirm-row">
                <span class="confirm-label">学号</span>
                <span class="confirm-value"><%= regStudentId %></span>
            </div>
            <div class="confirm-row">
                <span class="confirm-label">邮箱</span>
                <span class="confirm-value"><%= regEmail %></span>
            </div>
            <div class="confirm-row">
                <span class="confirm-label">电话</span>
                <span class="confirm-value"><%= regPhone %></span>
            </div>
        </div>
        <form method="post" action="${pageContext.request.contextPath}/ta/auth">
            <input type="hidden" name="action" value="register">
            <div class="form-actions">
                <a href="${pageContext.request.contextPath}/ta/register.jsp" class="btn btn-secondary">返回修改</a>
                <button type="submit" class="btn btn-primary">确认注册</button>
            </div>
        </form>
        <p class="links"><a href="${pageContext.request.contextPath}/ta/auth">返回登录</a></p>
    </div>
</body>
</html>
