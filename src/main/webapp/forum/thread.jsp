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
      * { box-sizing: border-box; }
      body {
        margin: 0;
        font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
        background: linear-gradient(165deg, #eef6ff 0%, #e0f2fe 40%, #f8fafc 100%);
        color: #1e293b;
        min-height: 100vh;
        line-height: 1.6;
      }
      .wrap { max-width: 920px; margin: 0 auto; padding: 1.35rem 1.25rem 2rem; }
      .forum-header {
        display: grid;
        grid-template-columns: auto 1fr auto;
        align-items: center;
        gap: 0.75rem 1rem;
        margin-bottom: 1.25rem;
        padding: 1rem 1.15rem 1.1rem;
        background: rgba(255, 255, 255, 0.78);
        border: 1px solid rgba(226, 232, 240, 0.95);
        border-radius: 16px;
        box-shadow: 0 4px 24px rgba(15, 23, 42, 0.06);
        backdrop-filter: blur(10px);
        -webkit-backdrop-filter: blur(10px);
      }
      .forum-header h1 {
        margin: 0;
        font-size: clamp(1rem, 3vw, 1.25rem);
        font-weight: 700;
        line-height: 1.4;
        color: #0f172a;
      }
      .back-link {
        color: #2563eb;
        text-decoration: none;
        font-size: 0.88rem;
        font-weight: 500;
        border-radius: 999px;
        padding: 0.45rem 0.85rem;
        border: 1px solid rgba(226, 232, 240, 0.9);
        background: rgba(255, 255, 255, 0.85);
        white-space: nowrap;
      }
      .back-link:hover { background: #eff6ff; border-color: #bfdbfe; }
      .msg-ok {
        color: #065f46;
        background: linear-gradient(180deg, #d1fae5, #ecfdf5);
        padding: 0.65rem 1rem;
        border-radius: 10px;
        margin-bottom: 1rem;
        font-size: 0.9rem;
        border: 1px solid #a7f3d0;
      }
      .post {
        background: #fff;
        border-radius: 14px;
        padding: 1.4rem 1.45rem;
        margin-bottom: 1rem;
        box-shadow: 0 2px 8px rgba(15, 23, 42, 0.05);
        border: 1px solid rgba(226, 232, 240, 0.95);
        border-left: 4px solid #2563eb;
      }
      .post-meta { font-size: 0.85rem; color: #64748b; margin-bottom: 0.85rem; }
      .post-body { white-space: pre-wrap; word-break: break-word; font-size: 0.95rem; color: #334155; }
      .replies-title {
        font-size: 1rem;
        margin: 0 0 0.75rem;
        color: #475569;
        font-weight: 600;
      }
      .reply {
        border: 1px solid #e2e8f0;
        border-radius: 12px;
        padding: 1rem 1.1rem;
        margin-bottom: 0.65rem;
        background: #fafafa;
        transition: background 0.15s ease;
      }
      .reply:hover { background: #f8fafc; }
      .reply-meta { font-size: 0.8rem; color: #64748b; margin-bottom: 0.45rem; }
      .reply-body { white-space: pre-wrap; word-break: break-word; font-size: 0.9rem; color: #334155; }
      .badge {
        display: inline-block;
        padding: 0.18rem 0.55rem;
        border-radius: 999px;
        font-size: 0.72rem;
        font-weight: 600;
        margin-right: 0.35rem;
      }
      .badge-ta { background: #dbeafe; color: #1e40af; }
      .badge-mo { background: #fef3c7; color: #92400e; }
      .badge-admin { background: #f3e8ff; color: #6b21a8; }
      .section {
        background: #fff;
        border-radius: 14px;
        padding: 1.4rem 1.45rem;
        margin-bottom: 1.25rem;
        box-shadow: 0 2px 8px rgba(15, 23, 42, 0.05);
        border: 1px solid rgba(226, 232, 240, 0.95);
      }
      .section h2 { font-size: 1.05rem; font-weight: 600; margin: 0 0 1rem; color: #0f172a; }
      .form-group { margin-bottom: 1rem; }
      .form-group label { display: block; font-size: 0.9rem; font-weight: 500; margin-bottom: 0.4rem; color: #1e293b; }
      .form-group textarea {
        width: 100%;
        padding: 0.65rem 0.9rem;
        border: 1px solid #e2e8f0;
        border-radius: 10px;
        font-size: 0.95rem;
        font-family: inherit;
        min-height: 120px;
        resize: vertical;
      }
      .form-group textarea:focus {
        outline: none;
        border-color: #2563eb;
        box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15);
      }
      .btn {
        display: inline-block;
        padding: 0.5rem 1.15rem;
        border: none;
        border-radius: 10px;
        font-size: 0.9rem;
        font-weight: 600;
        cursor: pointer;
        font-family: inherit;
        background: #2563eb;
        color: #fff;
        box-shadow: 0 2px 8px rgba(37, 99, 235, 0.25);
      }
      .btn:hover { background: #1d4ed8; }
      .login-hint { font-size: 0.9rem; color: #64748b; line-height: 1.55; }
      .login-hint a { color: #2563eb; font-weight: 500; }
      @media (max-width: 560px) {
        .forum-header { grid-template-columns: 1fr 1fr; text-align: center; }
        .forum-header h1 { order: -1; grid-column: 1 / -1; }
      }
    </style>
</head>
<body>
    <div class="wrap">
        <header class="forum-header">
            <a href="<%= ctx %>/forum" class="back-link">← 论坛列表</a>
            <h1><c:out value="${forumThread.title}"/></h1>
            <a href="<%= ctx %>/assistant" class="back-link">小助手</a>
        </header>

        <c:if test="${not empty forumNotice}">
            <p class="msg-ok"><c:out value="${forumNotice}"/></p>
        </c:if>

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

        <div id="reply-form" class="section" tabindex="-1">
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
