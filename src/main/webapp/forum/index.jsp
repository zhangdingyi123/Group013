<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
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
        margin-bottom: 1.5rem;
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
        font-size: clamp(1.2rem, 3.5vw, 1.45rem);
        font-weight: 700;
        letter-spacing: -0.02em;
        text-align: center;
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
        transition: background 0.2s, border-color 0.2s, box-shadow 0.2s;
        white-space: nowrap;
      }
      .back-link:hover {
        background: #eff6ff;
        border-color: #bfdbfe;
        box-shadow: 0 2px 8px rgba(37, 99, 235, 0.1);
      }
      .msg-ok {
        color: #065f46;
        background: linear-gradient(180deg, #d1fae5, #ecfdf5);
        padding: 0.65rem 1rem;
        border-radius: 10px;
        margin-bottom: 1rem;
        font-size: 0.9rem;
        border: 1px solid #a7f3d0;
      }
      .msg-err {
        color: #991b1b;
        background: linear-gradient(180deg, #fee2e2, #fef2f2);
        padding: 0.65rem 1rem;
        border-radius: 10px;
        margin-bottom: 1rem;
        font-size: 0.9rem;
        border: 1px solid #fecaca;
      }
      .section {
        background: #fff;
        border-radius: 14px;
        padding: 1.4rem 1.45rem 1.45rem;
        margin-bottom: 1.15rem;
        box-shadow: 0 2px 8px rgba(15, 23, 42, 0.05), 0 8px 28px rgba(15, 23, 42, 0.04);
        border: 1px solid rgba(226, 232, 240, 0.95);
        border-left: 4px solid #2563eb;
      }
      .section h2 {
        font-size: 1.05rem;
        font-weight: 600;
        margin: 0 0 1rem;
        color: #0f172a;
      }
      .thread-list { list-style: none; margin: 0; padding: 0; }
      .thread-item {
        border-bottom: 1px solid #f1f5f9;
        padding: 1rem 0.35rem;
        margin: 0 -0.35rem;
        border-radius: 10px;
        transition: background 0.15s ease;
      }
      .thread-item:hover { background: #f8fafc; }
      .thread-item:last-child { border-bottom: none; }
      .thread-item a.title {
        color: #0f172a;
        font-weight: 600;
        text-decoration: none;
        font-size: 1rem;
        line-height: 1.45;
      }
      .thread-item a.title:hover { color: #2563eb; }
      .thread-meta { font-size: 0.8rem; color: #64748b; margin-top: 0.45rem; }
      .badge {
        display: inline-block;
        padding: 0.18rem 0.55rem;
        border-radius: 999px;
        font-size: 0.72rem;
        font-weight: 600;
        color: #475569;
        background: #e2e8f0;
        margin-right: 0.35rem;
      }
      .badge-ta { background: #dbeafe; color: #1e40af; }
      .badge-mo { background: #fef3c7; color: #92400e; }
      .badge-admin { background: #f3e8ff; color: #6b21a8; }
      .empty-hint { color: #64748b; font-size: 0.9rem; padding: 1rem 0; }
      .form-group { margin-bottom: 1rem; }
      .form-group label {
        display: block;
        font-size: 0.9rem;
        font-weight: 500;
        margin-bottom: 0.4rem;
        color: #1e293b;
      }
      .form-group input,
      .form-group textarea {
        width: 100%;
        padding: 0.65rem 0.9rem;
        border: 1px solid #e2e8f0;
        border-radius: 10px;
        font-size: 0.95rem;
        font-family: inherit;
        transition: border-color 0.2s, box-shadow 0.2s;
      }
      .form-group input:focus,
      .form-group textarea:focus {
        outline: none;
        border-color: #2563eb;
        box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15);
      }
      .form-group textarea { min-height: 120px; resize: vertical; }
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
        transition: background 0.2s, box-shadow 0.2s;
      }
      .btn:hover { background: #1d4ed8; box-shadow: 0 4px 14px rgba(37, 99, 235, 0.3); }
      .btn-secondary { background: #e2e8f0; color: #475569; box-shadow: none; }
      .btn-secondary:hover { background: #cbd5e1; }
      .login-hint { font-size: 0.9rem; color: #64748b; margin-bottom: 0.75rem; line-height: 1.55; }
      .login-hint a { color: #2563eb; font-weight: 500; }
      .forum-toolbar {
        display: flex;
        flex-wrap: wrap;
        align-items: flex-end;
        gap: 0.75rem 1rem;
        margin-bottom: 1rem;
        padding: 0.85rem 1rem;
        background: #f8fafc;
        border: 1px solid #e2e8f0;
        border-radius: 10px;
      }
      .forum-filter-form {
        display: flex;
        flex-wrap: wrap;
        align-items: flex-end;
        gap: 0.5rem 0.65rem;
        flex: 1;
        min-width: 0;
      }
      .forum-filter-form input[type="search"] {
        flex: 1;
        min-width: 140px;
        padding: 0.5rem 0.75rem;
        border: 1px solid #e2e8f0;
        border-radius: 8px;
        font-size: 0.9rem;
        font-family: inherit;
      }
      .forum-filter-form select {
        padding: 0.5rem 0.65rem;
        border: 1px solid #e2e8f0;
        border-radius: 8px;
        font-size: 0.88rem;
        font-family: inherit;
        background: #fff;
      }
      .forum-filter-form label[for="forum-sort"] {
        font-size: 0.8rem;
        font-weight: 500;
        color: #64748b;
        margin-right: 0.15rem;
      }
      .forum-meta-line {
        margin: 0;
        font-size: 0.82rem;
        color: #64748b;
        width: 100%;
      }
      .forum-pagination {
        display: flex;
        flex-wrap: wrap;
        align-items: center;
        gap: 0.5rem;
        margin-top: 1rem;
        padding-top: 0.75rem;
        border-top: 1px solid #f1f5f9;
        font-size: 0.88rem;
      }
      .forum-pagination a {
        color: #2563eb;
        text-decoration: none;
        font-weight: 500;
        padding: 0.35rem 0.65rem;
        border-radius: 6px;
        border: 1px solid #e2e8f0;
        background: #fff;
      }
      .forum-pagination a:hover { background: #eff6ff; border-color: #bfdbfe; }
      .forum-pagination .muted { color: #94a3b8; border: none; background: transparent; padding-left: 0; }
      .sr-only {
        position: absolute;
        width: 1px;
        height: 1px;
        padding: 0;
        margin: -1px;
        overflow: hidden;
        clip: rect(0, 0, 0, 0);
        white-space: nowrap;
        border: 0;
      }
      @media (max-width: 560px) {
        .forum-header {
          grid-template-columns: 1fr 1fr;
          text-align: center;
          padding: 1rem 0.9rem;
        }
        .forum-header h1 {
          order: -1;
          grid-column: 1 / -1;
          margin-bottom: 0.15rem;
        }
        .forum-header .back-link { justify-self: stretch; text-align: center; }
      }
    </style>
</head>
<body>
    <div class="wrap">
        <header class="forum-header">
            <a href="<%= ctx %>/" class="back-link">← 首页</a>
            <h1>交流论坛</h1>
            <a href="<%= ctx %>/assistant" class="back-link">智能小助手</a>
        </header>

        <c:if test="${not empty forumNotice}">
            <p class="msg-ok"><c:out value="${forumNotice}"/></p>
        </c:if>
        <c:if test="${not empty forumError}">
            <p class="msg-err"><c:out value="${forumError}"/></p>
        </c:if>

        <div class="section">
            <h2>主题列表</h2>
            <form class="forum-toolbar" method="get" action="<%= ctx %>/forum" role="search" aria-label="搜索与排序">
                <div class="forum-filter-form">
                    <label class="sr-only" for="forum-q">按标题搜索</label>
                    <input type="search" id="forum-q" name="q" value="<c:out value='${forumQuery}'/>" placeholder="搜索标题关键词…" maxlength="100" autocomplete="off">
                    <label for="forum-sort">排序</label>
                    <select id="forum-sort" name="sort" aria-label="排序方式">
                        <option value="" ${empty forumSort ? 'selected' : ''}>最新回复优先</option>
                        <option value="created" ${forumSort eq 'created' ? 'selected' : ''}>最新发帖优先</option>
                    </select>
                    <button type="submit" class="btn btn-secondary">应用</button>
                </div>
                <p class="forum-meta-line">共 <strong>${forumIndexTotal}</strong> 条主题 · 第 ${forumIndexPage} / ${forumIndexTotalPages} 页</p>
            </form>
            <c:choose>
                <c:when test="${empty forumThreads}">
                    <p class="empty-hint">
                        <c:choose>
                            <c:when test="${not empty forumQuery and fn:length(forumQuery) > 0}">没有标题包含「<c:out value="${forumQuery}"/>」的主题，可换关键词或<a href="<%= ctx %>/forum">清空搜索</a>。</c:when>
                            <c:otherwise>当前还没有主题，登录后下方可发布第一条帖文。</c:otherwise>
                        </c:choose>
                    </p>
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
                    <c:if test="${forumIndexTotalPages > 1}">
                        <nav class="forum-pagination" aria-label="分页">
                            <c:if test="${forumIndexPage > 1}">
                                <c:url var="forumPrev" value="/forum">
                                    <c:param name="q" value="${forumQuery}"/>
                                    <c:param name="sort" value="${forumSort}"/>
                                    <c:param name="page" value="${forumIndexPage - 1}"/>
                                </c:url>
                                <a href="${forumPrev}">上一页</a>
                            </c:if>
                            <span class="muted">第 ${forumIndexPage} / ${forumIndexTotalPages} 页</span>
                            <c:if test="${forumIndexPage < forumIndexTotalPages}">
                                <c:url var="forumNext" value="/forum">
                                    <c:param name="q" value="${forumQuery}"/>
                                    <c:param name="sort" value="${forumSort}"/>
                                    <c:param name="page" value="${forumIndexPage + 1}"/>
                                </c:url>
                                <a href="${forumNext}">下一页</a>
                            </c:if>
                        </nav>
                    </c:if>
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
