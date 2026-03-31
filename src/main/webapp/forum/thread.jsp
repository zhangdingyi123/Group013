<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%
    String ctx = request.getContextPath();
    if (request.getAttribute("forumThread") == null) {
        response.sendRedirect(ctx + "/forum");
        return;
    }
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${forumThread.title}"/> - 交流论坛</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css?v=3">
    <style>
      *{box-sizing:border-box} body{margin:0;font-family:"PingFang SC","Microsoft YaHei",sans-serif;background:#f8fafc;color:#1e293b;min-height:100vh;line-height:1.6}
      .wrap{max-width:920px;margin:0 auto;padding:1.5rem}
      .page-header{display:flex;flex-wrap:wrap;align-items:flex-start;justify-content:space-between;gap:1rem;margin-bottom:1.25rem;padding-bottom:1rem;border-bottom:1px solid #e2e8f0}
      .page-header h1{margin:0;font-size:1.35rem;font-weight:600;flex:1;min-width:0}
      .back-link{color:#2563eb;text-decoration:none;font-size:.9rem;border-radius:6px;padding:.4rem .75rem;white-space:nowrap}
      .back-link:hover{background:#dbeafe}
      .post{background:#fff;border-radius:10px;padding:1.35rem;margin-bottom:1rem;box-shadow:0 1px 3px rgba(0,0,0,.06);border:1px solid #e2e8f0;border-left:4px solid #2563eb}
      .post-meta{font-size:.85rem;color:#64748b;margin-bottom:.75rem}
      .post-body{white-space:pre-wrap;word-break:break-word;font-size:.95rem;color:#334155}
      .reply{border:1px solid #e2e8f0;border-radius:8px;padding:1rem;margin-bottom:.75rem;background:#fafafa}
      .reply-meta{font-size:.8rem;color:#64748b;margin-bottom:.5rem}
      .reply-body{white-space:pre-wrap;word-break:break-word;font-size:.9rem;color:#334155}
      .badge{display:inline-block;padding:.15rem .5rem;border-radius:999px;font-size:.75rem;font-weight:500;margin-right:.35rem}
      .badge-ta{background:#dbeafe;color:#1e40af}
      .badge-mo{background:#fef3c7;color:#92400e}
      .badge-admin{background:#f3e8ff;color:#6b21a8}
      .section{background:#fff;border-radius:10px;padding:1.35rem;margin-bottom:1.25rem;box-shadow:0 1px 3px rgba(0,0,0,.06);border:1px solid #e2e8f0}
      .section h2{font-size:1.05rem;font-weight:600;margin:0 0 1rem;color:#1e293b}
      .form-group{margin-bottom:1rem}
      .form-group label{display:block;font-size:.9rem;font-weight:500;margin-bottom:.4rem;color:#1e293b}
      .form-group textarea{width:100%;padding:.65rem .85rem;border:1px solid #e2e8f0;border-radius:6px;font-size:.95rem;font-family:inherit;min-height:100px;resize:vertical}
      .btn{display:inline-block;padding:.45rem 1rem;border:none;border-radius:6px;font-size:.875rem;font-weight:500;cursor:pointer;font-family:inherit;background:#2563eb;color:#fff}
      .btn:hover{background:#1d4ed8}
      .login-hint{font-size:.9rem;color:#64748b}
      h2.replies-title{font-size:1rem;margin:0 0 .75rem;color:#475569}
    </style>
</head>
<body>
    <div class="wrap">
        <div class="page-header">
            <a href="<%= ctx %>/forum" class="back-link">← 论坛列表</a>
            <h1><c:out value="${forumThread.title}"/></h1>
        </div>

        <div class="post">
            <div class="post-meta">
                <c:choose>
                    <c:when test="${forumThread.authorRole eq 'ta'}"><span class="badge badge-ta">应聘者</span></c:when>
                    <c:when test="${forumThread.authorRole eq 'mo'}"><span class="badge badge-mo">课程组织者</span></c:when>
                    <c:when test="${forumThread.authorRole eq 'admin'}"><span class="badge badge-admin">管理员</span></c:when>
                    <c:otherwise><span class="badge">用户</span></c:otherwise>
                </c:choose>
                <c:out value="${forumThread.authorName}"/> · <c:out value="${forumThread.createdAtText}"/>
            </div>
            <div class="post-body"><c:out value="${forumThread.body}"/></div>
        </div>

        <h2 class="replies-title">回复 (${empty forumReplies ? 0 : fn:length(forumReplies)})</h2>
        <c:forEach items="${forumReplies}" var="r">
            <div class="reply">
                <div class="reply-meta">
                    <c:choose>
                        <c:when test="${r.authorRole eq 'ta'}"><span class="badge badge-ta">应聘者</span></c:when>
                        <c:when test="${r.authorRole eq 'mo'}"><span class="badge badge-mo">课程组织者</span></c:when>
                        <c:when test="${r.authorRole eq 'admin'}"><span class="badge badge-admin">管理员</span></c:when>
                        <c:otherwise><span class="badge">用户</span></c:otherwise>
                    </c:choose>
                    <c:out value="${r.authorName}"/> · <c:out value="${r.createdAtText}"/>
                </div>
                <div class="reply-body"><c:out value="${r.body}"/></div>
            </div>
        </c:forEach>

        <div class="section">
            <h2>发表回复</h2>
            <c:choose>
                <c:when test="${empty forumAuthor}">
                    <p class="login-hint">请 <a href="<%= ctx %>/ta/auth">应聘者登录</a>、<a href="<%= ctx %>/mo/auth">课程组织者登录</a> 或 <a href="<%= ctx %>/admin/auth">管理员登录</a> 后回复。</p>
                </c:when>
                <c:otherwise>
                    <form method="post" action="<%= ctx %>/forum">
                        <input type="hidden" name="action" value="newReply">
                        <input type="hidden" name="threadId" value="<c:out value='${forumThread.id}'/>">
                        <div class="form-group">
                            <label for="body">内容</label>
                            <textarea id="body" name="body" required placeholder="写下你的看法或补充…"></textarea>
                        </div>
                        <button type="submit" class="btn">发表回复</button>
                    </form>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</body>
</html>
