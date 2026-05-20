<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.bupt.ta.util.I18n" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
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
    String applicationsPostUrl = (String) request.getAttribute("applicationsPostUrl");
    if (applicationsPostUrl == null) {
        applicationsPostUrl = request.getContextPath() + "/ta/dashboard";
    }
    request.setAttribute("applicationsPostUrl", applicationsPostUrl);
%>
<jsp:useBean id="tsDate" scope="page" class="java.util.Date"/>
<section id="pc-applications" class="section">
    <h2><%= I18n.msg(request, "apps.title") %></h2>
    <p class="section-desc"><%= I18n.msg(request, "apps.desc") %></p>
    <c:choose>
        <c:when test="${empty myApplications}">
            <p class="empty-hint"><%= I18n.msg(request, "apps.empty") %></p>
        </c:when>
        <c:otherwise>
    <div class="table-wrap">
    <table>
        <thead>
            <tr>
                <th><%= I18n.msg(request, "apps.th.job") %></th>
                <th><%= I18n.msg(request, "apps.th.status") %></th>
                <th><%= I18n.msg(request, "apps.th.iv") %></th>
                <th><%= I18n.msg(request, "apps.th.time") %></th>
                <th><%= I18n.msg(request, "apps.th.ops") %></th>
            </tr>
        </thead>
        <tbody>
        <c:forEach var="row" items="${myApplications}">
            <c:set var="app" value="${row.app}"/>
            <c:set var="job" value="${row.job}"/>
            <c:set var="st" value="${app.status}"/>
            <tr>
                <td>
                    <c:choose>
                        <c:when test="${not empty job.title}"><c:out value="${job.title}"/></c:when>
                        <c:otherwise><%= I18n.msg(request, "apps.job.deleted") %></c:otherwise>
                    </c:choose>
                </td>
                <td>
                    <c:choose>
                        <c:when test="${st eq 'pending'}"><span class="badge badge-pending"><%= I18n.msg(request, "apps.status.pending") %></span></c:when>
                        <c:when test="${st eq 'interview'}">
                            <span class="badge badge-interview"><%= I18n.msg(request, "apps.status.interview") %></span>
                            <c:choose>
                                <c:when test="${app.interviewTaStatus eq 'pending'}"><div style="font-size:.78rem;margin-top:.2rem;color:#64748b;"><%= I18n.msg(request, "apps.iv.ta.pending") %></div></c:when>
                                <c:when test="${app.interviewTaStatus eq 'confirmed'}"><div style="font-size:.78rem;margin-top:.2rem;color:#64748b;"><%= I18n.msg(request, "apps.iv.ta.confirmed") %></div></c:when>
                                <c:when test="${app.interviewTaStatus eq 'declined'}"><div style="font-size:.78rem;margin-top:.2rem;color:#64748b;"><%= I18n.msg(request, "apps.iv.ta.declined") %></div></c:when>
                                <c:when test="${app.interviewTaStatus eq 'reschedule'}"><div style="font-size:.78rem;margin-top:.2rem;color:#64748b;"><%= I18n.msg(request, "apps.iv.ta.reschedule") %></div></c:when>
                            </c:choose>
                        </c:when>
                        <c:when test="${st eq 'accepted'}"><span class="badge badge-accepted"><%= I18n.msg(request, "apps.status.accepted") %></span></c:when>
                        <c:when test="${st eq 'rejected'}"><span class="badge badge-rejected"><%= I18n.msg(request, "apps.status.rejected") %></span></c:when>
                        <c:when test="${st eq 'cancelled'}"><span class="badge badge-cancelled"><%= I18n.msg(request, "apps.status.cancelled") %></span></c:when>
                        <c:otherwise><span class="badge badge-pending"><c:out value="${st}"/></span></c:otherwise>
                    </c:choose>
                </td>
                <td>
                    <c:choose>
                        <c:when test="${st eq 'interview'}">
                            <c:if test="${app.interviewAt > 0}">
                            <div class="app-iv-note"><strong><%= I18n.msg(request, "apps.iv.time") %></strong>
                                <c:set target="${tsDate}" property="time" value="${app.interviewAt}"/>
                                <fmt:formatDate value="${tsDate}" pattern="yyyy-MM-dd HH:mm"/>
                            </div>
                            </c:if>
                            <div class="app-iv-note"><strong><%= I18n.msg(request, "apps.iv.place") %></strong>
                                <c:choose>
                                    <c:when test="${not empty app.interviewDetail}"><c:out value="${app.interviewDetail}"/></c:when>
                                    <c:otherwise><%= I18n.msg(request, "common.dash") %></c:otherwise>
                                </c:choose>
                            </div>
                        </c:when>
                        <c:otherwise><%= I18n.msg(request, "common.dash") %></c:otherwise>
                    </c:choose>
                </td>
                <td>
                    <c:set target="${tsDate}" property="time" value="${app.appliedAt}"/>
                    <fmt:formatDate value="${tsDate}" pattern="yyyy-MM-dd HH:mm"/>
                </td>
                <td>
                    <c:choose>
                        <c:when test="${st eq 'pending'}">
                            <form method="post" action="${applicationsPostUrl}" style="display:inline;">
                                <input type="hidden" name="action" value="cancelApplication">
                                <input type="hidden" name="applicationId" value="${app.id}">
                                <button type="submit" class="btn btn-secondary btn-small"><%= I18n.msg(request, "apps.cancel") %></button>
                            </form>
                        </c:when>
                        <c:when test="${st eq 'interview' and app.interviewTaStatus eq 'pending'}">
                            <div style="display:flex;flex-wrap:wrap;gap:.35rem;align-items:center;">
                            <form method="post" action="${applicationsPostUrl}" style="display:inline;">
                                <input type="hidden" name="action" value="confirmInterview">
                                <input type="hidden" name="applicationId" value="${app.id}">
                                <button type="submit" class="btn btn-primary btn-small"><%= I18n.msg(request, "apps.confirmAttend") %></button>
                            </form>
                            <form method="post" action="${applicationsPostUrl}" style="display:inline;" data-confirm="<%= I18n.msg(request, "apps.confirm.decline") %>">
                                <input type="hidden" name="action" value="declineInterview">
                                <input type="hidden" name="applicationId" value="${app.id}">
                                <button type="submit" class="btn btn-secondary btn-small"><%= I18n.msg(request, "apps.decline") %></button>
                            </form>
                            <form method="post" action="${applicationsPostUrl}" style="display:inline;" data-confirm="<%= I18n.msg(request, "apps.confirm.reschedule") %>">
                                <input type="hidden" name="action" value="requestRescheduleInterview">
                                <input type="hidden" name="applicationId" value="${app.id}">
                                <button type="submit" class="btn btn-secondary btn-small"><%= I18n.msg(request, "apps.reschedule") %></button>
                            </form>
                            </div>
                        </c:when>
                        <c:otherwise><%= I18n.msg(request, "common.dash") %></c:otherwise>
                    </c:choose>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
    </div>
        </c:otherwise>
    </c:choose>
</section>
<script>
document.querySelectorAll('#pc-applications form[data-confirm]').forEach(function (f) {
    f.addEventListener('submit', function (e) {
        if (!confirm(f.getAttribute('data-confirm'))) e.preventDefault();
    });
});
</script>
