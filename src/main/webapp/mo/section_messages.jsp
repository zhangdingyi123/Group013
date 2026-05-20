<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.bupt.ta.util.I18n, com.bupt.ta.model.Applicant" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    if (request.getAttribute("mo") == null) {
        String c = request.getContextPath();
        if (session.getAttribute("moUser") != null) {
            response.sendRedirect(c + "/mo/dashboard");
        } else {
            response.sendRedirect(c + "/mo/auth");
        }
        return;
    }
    String ctx = request.getContextPath();
    Applicant moDmApplicantObj = null;
    if (request.getAttribute("moDmApplicant") != null) {
        moDmApplicantObj = (Applicant) request.getAttribute("moDmApplicant");
    }
%>
<div class="section dm-section">
    <h2><%= I18n.msg(request, "dm.mo.h2") %></h2>
    <p class="section-desc"><%= I18n.msg(request, "dm.mo.desc") %></p>
    <c:if test="${not empty moDmNotice}">
        <p class="msg-ok"><c:out value="${moDmNotice}"/></p>
    </c:if>

    <c:if test="${not empty moFriendRequestsPending}">
        <div class="friend-requests-panel" role="region" aria-label="<%= I18n.msg(request, "dm.mo.friend.pending.title") %>">
            <h3 class="friend-requests-title"><%= I18n.msg(request, "dm.mo.friend.pending.title") %></h3>
            <p class="friend-requests-desc"><%= I18n.msg(request, "dm.mo.friend.pending.desc") %></p>
            <ul class="friend-requests-list">
                <c:forEach items="${moFriendRequestsPending}" var="fr">
                    <li class="friend-request-item">
                        <span class="friend-request-name"><c:out value="${fr.applicantName}"/></span>
                        <form method="post" action="<%= ctx %>/mo/dashboard" class="friend-request-form">
                            <input type="hidden" name="action" value="acceptFriendRequest">
                            <input type="hidden" name="requestId" value="<c:out value='${fr.requestId}'/>">
                            <input type="hidden" name="applicantId" value="<c:out value='${fr.applicantId}'/>">
                            <button type="submit" class="btn btn-primary btn-sm"><%= I18n.msg(request, "dm.mo.friend.accept") %></button>
                        </form>
                    </li>
                </c:forEach>
            </ul>
        </div>
    </c:if>

    <c:choose>
        <c:when test="${empty moDmThreads}">
            <p class="empty-hint"><%= I18n.msg(request, "dm.mo.empty") %></p>
        </c:when>
        <c:otherwise>
            <div class="dm-layout">
                <div class="dm-thread-list" role="navigation" aria-label="<%= I18n.msg(request, "dm.ta.threadAria") %>">
                    <c:forEach items="${moDmThreads}" var="row">
                        <c:url var="threadUrl" value="/mo/dashboard">
                            <c:param name="tab" value="messages"/>
                            <c:param name="withApplicant" value="${row.applicantId}"/>
                        </c:url>
                        <c:set var="isActive" value="${moDmWithApplicant eq row.applicantId}"/>
                        <a href="${threadUrl}" class="dm-thread-item ${isActive ? 'active' : ''}">
                            <span class="dm-thread-name"><c:out value="${row.applicantName}"/><c:if test="${row.unreadCount > 0}"><span class="dm-unread-badge" title="<%= I18n.msg(request, "dm.unread") %>">${row.unreadCount > 99 ? '99+' : row.unreadCount}</span></c:if></span>
                            <span class="dm-thread-preview">
                                <c:choose>
                                    <c:when test="${not empty row.lastPreview}"><c:out value="${row.lastPreview}"/></c:when>
                                    <c:otherwise><%= I18n.msg(request, "dm.ta.noMsg") %></c:otherwise>
                                </c:choose>
                            </span>
                            <span class="dm-thread-time"><c:out value="${row.lastAtText}"/></span>
                        </a>
                    </c:forEach>
                </div>
                <div class="dm-pane">
                    <c:if test="${not empty moDmApplicant}">
                        <p class="dm-peer-title"><%= I18n.msg(request, "dm.ta.conv", moDmApplicantObj.getName()) %></p>
                    </c:if>
                    <c:if test="${moDmIsFriend}">
                        <p class="friend-status-hint"><%= I18n.msg(request, "dm.mo.friend.isFriend") %></p>
                    </c:if>
                    <c:if test="${moDmCanRequestFriendApplicant}">
                        <form method="post" action="<%= ctx %>/mo/dashboard" class="friend-request-action">
                            <input type="hidden" name="action" value="requestFriendApplicant">
                            <input type="hidden" name="applicantId" value="<c:out value='${moDmWithApplicant}'/>">
                            <button type="submit" class="btn btn-secondary btn-sm"><%= I18n.msg(request, "dm.mo.friend.request") %></button>
                        </form>
                    </c:if>
                    <div class="dm-bubble-list" aria-live="polite">
                        <c:choose>
                            <c:when test="${empty moDmConversation}">
                                <p class="empty-hint" style="padding:.5rem 0"><%= I18n.msg(request, "dm.mo.first") %></p>
                            </c:when>
                            <c:otherwise>
                                <c:forEach items="${moDmConversation}" var="m">
                                    <c:set var="mine" value="${m.senderRole eq 'mo'}"/>
                                    <div class="dm-bubble ${mine ? 'mine' : 'theirs'}">
                                        <div class="dm-meta">
                                            <c:choose>
                                                <c:when test="${mine}"><%= I18n.msg(request, "dm.ta.meta.me") %></c:when>
                                                <c:otherwise><%= I18n.msg(request, "dm.mo.meta.applicant") %></c:otherwise>
                                            </c:choose>
                                            · <c:out value="${m.sentAtText}"/>
                                        </div>
                                        <div class="dm-body"><c:out value="${m.body}"/></div>
                                    </div>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <form method="post" action="<%= ctx %>/mo/dashboard" class="dm-compose">
                        <input type="hidden" name="action" value="sendDm">
                        <input type="hidden" name="applicantId" value="<c:out value='${moDmWithApplicant}'/>">
                        <div class="form-group">
                            <label for="mo-dm-body"><%= I18n.msg(request, "dm.mo.label.reply") %></label>
                            <textarea id="mo-dm-body" name="body" rows="4" placeholder="<%= I18n.msg(request, "dm.mo.placeholder") %>" required></textarea>
                        </div>
                        <button type="submit" class="btn btn-primary"><%= I18n.msg(request, "common.send") %></button>
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
              .friend-requests-panel{margin-bottom:1rem;padding:1rem;border:1px solid #bfdbfe;border-radius:10px;background:#eff6ff}
              .friend-requests-title{margin:0 0 .35rem;font-size:1rem;color:#1e40af}
              .friend-requests-desc{margin:0 0 .65rem;font-size:.85rem;color:#475569}
              .friend-requests-list{list-style:none;margin:0;padding:0}
              .friend-request-item{display:flex;flex-wrap:wrap;align-items:center;gap:.5rem;padding:.5rem 0;border-top:1px solid #dbeafe}
              .friend-request-item:first-child{border-top:none;padding-top:0}
              .friend-request-name{font-weight:600;color:#1e293b}
              .friend-request-form{margin:0}
              .friend-status-hint{margin:0 0 .65rem;font-size:.85rem;color:#0f766e}
              .friend-request-action{margin:0 0 .75rem}
              .btn-sm{padding:.35rem .75rem;font-size:.85rem}
            </style>
        </c:otherwise>
    </c:choose>
</div>
