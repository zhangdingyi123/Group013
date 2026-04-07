<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%
    if (request.getAttribute("applicant") == null) {
        String c = request.getContextPath();
        if (session.getAttribute("taUser") != null) {
            response.sendRedirect(c + "/ta/dashboard");
        } else {
            response.sendRedirect(c + "/ta/auth");
        }
        return;
    }
    String ctx = request.getContextPath();
    String dmPostAction = (String) request.getAttribute("taDmPostAction");
    if (dmPostAction == null) {
        dmPostAction = ctx + "/ta/dashboard";
    }
%>
<div id="pc-messages" class="section dm-section">
    <h2>私信招聘者</h2>
    <p class="section-desc">在开放岗位中可联系发布者，或向已申请岗位的招聘者发送消息；对方可在工作台「私信」中回复。</p>
    <c:if test="${not empty dmNotice}">
        <p class="msg-ok"><c:out value="${dmNotice}"/></p>
    </c:if>

    <c:choose>
        <c:when test="${empty taDmThreads}">
            <p class="empty-hint">暂无可联系的招聘者。请先浏览「开放岗位」或提交岗位申请。</p>
        </c:when>
        <c:otherwise>
            <div class="dm-layout">
                <div class="dm-thread-list" role="navigation" aria-label="会话列表">
                    <c:forEach items="${taDmThreads}" var="row">
                        <c:choose>
                        <c:when test="${taDmProfileMode}">
                        <c:url var="threadUrl" value="/ta/profile">
                            <c:param name="withMo" value="${row.moId}"/>
                        </c:url>
                        </c:when>
                        <c:otherwise>
                        <c:url var="threadUrl" value="/ta/dashboard">
                            <c:param name="tab" value="messages"/>
                            <c:param name="withMo" value="${row.moId}"/>
                        </c:url>
                        </c:otherwise>
                        </c:choose>
                        <c:set var="isActive" value="${taDmWithMo eq row.moId}"/>
                        <a href="${threadUrl}" class="dm-thread-item ${isActive ? 'active' : ''}">
                            <span class="dm-thread-name"><c:out value="${row.moName}"/><c:if test="${row.unreadCount > 0}"><span class="dm-unread-badge" title="未读">${row.unreadCount > 99 ? '99+' : row.unreadCount}</span></c:if></span>
                            <span class="dm-thread-preview">
                                <c:choose>
                                    <c:when test="${not empty row.lastPreview}"><c:out value="${row.lastPreview}"/></c:when>
                                    <c:otherwise>（尚无消息）</c:otherwise>
                                </c:choose>
                            </span>
                            <span class="dm-thread-time"><c:out value="${row.lastAtText}"/></span>
                        </a>
                    </c:forEach>
                </div>
                <div class="dm-pane">
                    <c:if test="${not empty taDmMo}">
                        <p class="dm-peer-title">与 <strong><c:out value="${taDmMo.name}"/></strong> 的对话</p>
                    </c:if>
                    <div class="dm-bubble-list" aria-live="polite">
                        <c:choose>
                            <c:when test="${empty taDmConversation}">
                                <p class="empty-hint" style="padding:.5rem 0">尚无消息，在下方输入第一条私信。</p>
                            </c:when>
                            <c:otherwise>
                                <c:forEach items="${taDmConversation}" var="m">
                                    <c:set var="mine" value="${m.senderRole eq 'ta'}"/>
                                    <div class="dm-bubble ${mine ? 'mine' : 'theirs'}">
                                        <div class="dm-meta">${mine ? '我' : '招聘者'} · <c:out value="${m.sentAtText}"/></div>
                                        <div class="dm-body"><c:out value="${m.body}"/></div>
                                    </div>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <form method="post" action="<%= dmPostAction %>" class="dm-compose">
                        <input type="hidden" name="action" value="sendDm">
                        <input type="hidden" name="moId" value="<c:out value='${taDmWithMo}'/>">
                        <c:if test="${not empty taDmPrefillJobId}">
                            <input type="hidden" name="jobId" value="<c:out value='${taDmPrefillJobId}'/>">
                        </c:if>
                        <div class="form-group">
                            <label for="dm-body">发送消息</label>
                            <textarea id="dm-body" name="body" rows="4" placeholder="输入私信内容" required></textarea>
                        </div>
                        <button type="submit" class="btn btn-primary">发送</button>
                    </form>
                </div>
            </div>
            <style>
              .dm-layout{display:grid;grid-template-columns:minmax(0,220px) minmax(0,1fr);gap:1rem;align-items:start}
              @media (max-width:720px){.dm-layout{grid-template-columns:1fr}}
              .dm-thread-list{border:1px solid #e2e8f0;border-radius:8px;overflow:hidden;background:#fafafa;max-height:420px;overflow-y:auto}
              .dm-thread-item{display:block;padding:.65rem .85rem;border-bottom:1px solid #e2e8f0;text-decoration:none;color:#334155;font-size:.875rem}
              .dm-thread-item:last-child{border-bottom:none}
              .dm-thread-item:hover{background:#f1f5f9}
              .dm-thread-item.active{background:#dbeafe;color:#1e3a8a}
              .dm-thread-name{display:block;font-weight:600;margin-bottom:.2rem}
              .dm-thread-preview{display:block;color:#64748b;font-size:.8rem;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
              .dm-thread-time{display:block;font-size:.75rem;color:#94a3b8;margin-top:.25rem}
              .dm-pane{border:1px solid #e2e8f0;border-radius:8px;padding:1rem;background:#fff}
              .dm-peer-title{margin:0 0 .75rem;font-size:.95rem;color:#475569}
              .dm-bubble-list{max-height:320px;overflow-y:auto;margin-bottom:1rem;padding-right:.25rem}
              .dm-bubble{margin-bottom:.75rem;max-width:92%}
              .dm-bubble.mine{margin-left:auto;text-align:right}
              .dm-bubble .dm-meta{font-size:.75rem;color:#94a3b8;margin-bottom:.25rem}
              .dm-bubble .dm-body{display:inline-block;text-align:left;padding:.55rem .85rem;border-radius:10px;font-size:.9rem;line-height:1.45;white-space:pre-wrap;word-break:break-word}
              .dm-bubble.mine .dm-body{background:#2563eb;color:#fff}
              .dm-bubble.theirs .dm-body{background:#f1f5f9;color:#1e293b}
              .dm-compose .form-group{margin-bottom:.75rem}
              .dm-unread-badge{display:inline-flex;align-items:center;justify-content:center;min-width:1.1rem;margin-left:.4rem;padding:0 .3rem;font-size:.65rem;font-weight:800;line-height:1.2;border-radius:999px;background:#dc2626;color:#fff;vertical-align:middle}
            </style>
        </c:otherwise>
    </c:choose>
</div>
