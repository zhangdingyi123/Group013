<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>交流论坛 - 助教招聘系统</title>
    <link rel="stylesheet" href="<%= ctx %>/css/style.css?v=3">
    <style>
      *{box-sizing:border-box} body{margin:0;font-family:"PingFang SC","Microsoft YaHei",sans-serif;background:#f8fafc;color:#1e293b;min-height:100vh;line-height:1.6}
      .wrap{max-width:920px;margin:0 auto;padding:1.5rem}
      .page-header{display:flex;flex-wrap:wrap;align-items:center;justify-content:space-between;gap:1rem;margin-bottom:1.25rem;padding-bottom:1rem;border-bottom:1px solid #e2e8f0}
      .page-header h1{margin:0;font-size:1.4rem;font-weight:600}
      .back-link{color:#2563eb;text-decoration:none;font-size:.9rem;border-radius:6px;padding:.4rem .75rem}
      .back-link:hover{background:#dbeafe}
      .msg-ok{color:#065f46;background:#d1fae5;padding:.6rem .85rem;border-radius:6px;margin-bottom:1rem;font-size:.9rem}
      .msg-err{color:#991b1b;background:#fee2e2;padding:.6rem .85rem;border-radius:6px;margin-bottom:1rem;font-size:.9rem}
      .section{background:#fff;border-radius:10px;padding:1.35rem;margin-bottom:1.25rem;box-shadow:0 1px 3px rgba(0,0,0,.06);border:1px solid #e2e8f0;border-left:4px solid #2563eb}
      .section h2{font-size:1.05rem;font-weight:600;margin:0 0 1rem;color:#1e293b}
      .thread-list{list-style:none;margin:0;padding:0}
      .thread-item{border-bottom:1px solid #e2e8f0;padding:.85rem 0}
      .thread-item:last-child{border-bottom:none}
      .thread-item a.title{color:#1e293b;font-weight:600;text-decoration:none;font-size:1rem}
      .thread-item a.title:hover{color:#2563eb}
      .thread-meta{font-size:.8rem;color:#64748b;margin-top:.35rem}
      .badge{display:inline-block;padding:.15rem .5rem;border-radius:999px;font-size:.75rem;font-weight:500;color:#475569;background:#e2e8f0;margin-right:.35rem}
      .badge-ta{background:#dbeafe;color:#1e40af}
      .badge-mo{background:#fef3c7;color:#92400e}
      .badge-admin{background:#f3e8ff;color:#6b21a8}
      .empty-hint{color:#64748b;font-size:.9rem;padding:1rem 0}
      .form-group{margin-bottom:1rem}
      .form-group label{display:block;font-size:.9rem;font-weight:500;margin-bottom:.4rem;color:#1e293b}
      .form-group input,.form-group textarea{width:100%;padding:.65rem .85rem;border:1px solid #e2e8f0;border-radius:6px;font-size:.95rem;font-family:inherit}
      .form-group textarea{min-height:120px;resize:vertical}
      .btn{display:inline-block;padding:.45rem 1rem;border:none;border-radius:6px;font-size:.875rem;font-weight:500;cursor:pointer;font-family:inherit;background:#2563eb;color:#fff}
      .btn:hover{background:#1d4ed8}
      .btn-secondary{background:#e2e8f0;color:#475569}.btn-secondary:hover{background:#cbd5e1}
      .login-hint{font-size:.9rem;color:#64748b;margin-bottom:.75rem}
    </style>
</head>
<body>
    <div class="wrap">
        <div class="page-header">
            <a href="<%= ctx %>/" class="back-link">← 首页</a>
            <h1>交流论坛</h1>
            <span></span>
        </div>

        <c:if test="${not empty forumNotice}">
            <p class="msg-ok"><c:out value="${forumNotice}"/></p>
        </c:if>
        <c:if test="${not empty forumError}">
            <p class="msg-err"><c:out value="${forumError}"/></p>
        </c:if>

        <div class="section">
            <h2>主题列表</h2>
            <c:choose>
                <c:when test="${empty forumThreads}">
                    <p class="empty-hint">当前还没有主题，登录后下方可发布第一条帖文。</p>
                </c:when>
                <c:otherwise>
                    <ul class="thread-list">
                        <c:forEach items="${forumThreads}" var="t">
                            <c:url var="threadUrl" value="/forum">
                                <c:param name="threadId" value="${t.id}"/>
                            </c:url>
                            <li class="thread-item">
                                <a class="title" href="${threadUrl}"><c:out value="${t.title}"/></a>
                                <div class="thread-meta">
                                    <c:choose>
                                        <c:when test="${t.authorRole eq 'ta'}"><span class="badge badge-ta">应聘者</span></c:when>
                                        <c:when test="${t.authorRole eq 'mo'}"><span class="badge badge-mo">课程组织者</span></c:when>
                                        <c:when test="${t.authorRole eq 'admin'}"><span class="badge badge-admin">管理员</span></c:when>
                                        <c:otherwise><span class="badge">用户</span></c:otherwise>
                                    </c:choose>
                                    <c:out value="${t.authorName}"/> · 最后回复 <c:out value="${t.lastReplyAtText}"/> · 回复 ${t.replyCount}
                                </div>
                            </li>
                        </c:forEach>
                    </ul>
                </c:otherwise>
            </c:choose>
        </div>

        <div class="section">
            <h2>发布新主题</h2>
            <c:choose>
                <c:when test="${empty forumAuthor}">
                    <p class="login-hint">登录后即可发帖。请选择入口：<a href="<%= ctx %>/ta/auth">应聘者</a>、<a href="<%= ctx %>/mo/auth">课程组织者</a> 或 <a href="<%= ctx %>/admin/auth">管理员</a>。</p>
                </c:when>
                <c:otherwise>
                    <form method="post" action="<%= ctx %>/forum">
                        <input type="hidden" name="action" value="newThread">
                        <div class="form-group">
                            <label for="title">标题</label>
                            <input id="title" name="title" type="text" maxlength="200" required placeholder="简要概括讨论主题">
                        </div>
                        <div class="form-group">
                            <label for="body">正文</label>
                            <textarea id="body" name="body" required placeholder="分享经验、提问或讨论…"></textarea>
                        </div>
                        <button type="submit" class="btn">发布主题</button>
                    </form>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</body>
</html>
