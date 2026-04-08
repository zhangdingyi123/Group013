<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.bupt.ta.model.Job" %>
<%@ page import="com.bupt.ta.model.Application" %>
<%@ page import="com.bupt.ta.service.MatchHelper" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.List" %>
<%
    Job job = (Job) request.getAttribute("job");
    String pageError = (String) request.getAttribute("error");
    @SuppressWarnings("unchecked")
    List<MatchHelper.ApplicantMatch> applicantsForJob = (List<MatchHelper.ApplicantMatch>) request.getAttribute("applicantsForJob");
    @SuppressWarnings("unchecked")
    List<Application> applicationsForJob = (List<Application>) request.getAttribute("applicationsForJob");
    if (applicantsForJob == null) applicantsForJob = java.util.Collections.emptyList();
    if (applicationsForJob == null) applicationsForJob = java.util.Collections.emptyList();
    String pageTitle = job != null ? ("筛选应聘者 - " + job.getTitle()) : "筛选应聘者";
    String filterAttr = (String) request.getAttribute("filter");
    if (filterAttr == null) filterAttr = "all";
    String qAttr = (String) request.getAttribute("q");
    if (qAttr == null) qAttr = "";
    String qAttrEsc = qAttr.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;");
    String sortAttr = (String) request.getAttribute("sort");
    if (sortAttr == null) sortAttr = "match_desc";
    Integer minScoreAttr = (Integer) request.getAttribute("minScore");
    if (minScoreAttr == null) minScoreAttr = 0;
    Integer totalApplicantsForJob = (Integer) request.getAttribute("totalApplicantsForJob");
    if (totalApplicantsForJob == null) totalApplicantsForJob = 0;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= pageTitle %> - 助教招聘系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=4">
</head>
<body class="mo-job-page">
    <div class="page">
        <div class="page-header">
            <a href="${pageContext.request.contextPath}/mo/dashboard?tab=positions" class="back-link">← 返回我的岗位</a>
            <h1><%= job != null ? ("筛选应聘者：" + job.getTitle()) : "筛选应聘者" %></h1>
        </div>
        <% if (request.getAttribute("moNotice") != null) { %>
        <p class="msg-ok"><%= request.getAttribute("moNotice") %></p>
        <% } %>

        <% if (job == null) { %>
        <div class="section">
            <p class="empty-hint" style="margin:0;"><%= pageError != null ? pageError : "岗位不存在或无权访问。" %></p>
            <p style="margin-top:1rem;"><a href="${pageContext.request.contextPath}/mo/dashboard?tab=positions" class="back-link">返回我的岗位</a></p>
        </div>
        <% } else { %>
        <div class="section">
            <h2>系统推荐（按匹配度与负荷均衡排序）</h2>
            <p class="section-desc">匹配分：岗位技能匹配度；技能短板：岗位需要但应聘者未体现的技能。可将<strong>待处理</strong>申请标记为<strong>待面试</strong>并填写时间与地点或线上链接；应聘者确认后您仍可录用或拒绝。<strong>录用任意一名应聘者后，该岗位将自动关闭</strong>，且关闭后不可再进入本筛选页。</p>
            <form class="filter-bar" method="get" action="${pageContext.request.contextPath}/mo/job-applicants">
                <input type="hidden" name="jobId" value="<%= job.getId() %>">
                <div>
                    <label for="filter-status">申请状态</label>
                    <select id="filter-status" name="filter">
                        <option value="all" <%= "all".equals(filterAttr) ? "selected" : "" %>>全部</option>
                        <option value="pending" <%= "pending".equals(filterAttr) ? "selected" : "" %>>待处理</option>
                        <option value="interview" <%= "interview".equals(filterAttr) ? "selected" : "" %>>待面试</option>
                        <option value="accepted" <%= "accepted".equals(filterAttr) ? "selected" : "" %>>已录用</option>
                        <option value="rejected" <%= "rejected".equals(filterAttr) ? "selected" : "" %>>已拒绝</option>
                        <option value="cancelled" <%= "cancelled".equals(filterAttr) ? "selected" : "" %>>已撤销</option>
                    </select>
                </div>
                <div>
                    <label for="filter-minScore">最低匹配分</label>
                    <select id="filter-minScore" name="minScore">
                        <option value="0" <%= minScoreAttr == 0 ? "selected" : "" %>>不限</option>
                        <option value="60" <%= minScoreAttr == 60 ? "selected" : "" %>>≥ 60</option>
                        <option value="70" <%= minScoreAttr == 70 ? "selected" : "" %>>≥ 70</option>
                        <option value="80" <%= minScoreAttr == 80 ? "selected" : "" %>>≥ 80</option>
                        <option value="90" <%= minScoreAttr == 90 ? "selected" : "" %>>≥ 90</option>
                    </select>
                </div>
                <div>
                    <label for="filter-sort">排序方式</label>
                    <select id="filter-sort" name="sort">
                        <option value="match_desc" <%= "match_desc".equals(sortAttr) ? "selected" : "" %>>匹配分 · 高→低</option>
                        <option value="match_asc" <%= "match_asc".equals(sortAttr) ? "selected" : "" %>>匹配分 · 低→高</option>
                        <option value="time_desc" <%= "time_desc".equals(sortAttr) ? "selected" : "" %>>申请时间 · 最新优先</option>
                        <option value="time_asc" <%= "time_asc".equals(sortAttr) ? "selected" : "" %>>申请时间 · 最早优先</option>
                        <option value="name_asc" <%= "name_asc".equals(sortAttr) ? "selected" : "" %>>姓名 A→Z</option>
                    </select>
                </div>
                <div>
                    <label for="filter-q">关键词</label>
                    <input type="text" id="filter-q" name="q" value="<%= qAttrEsc %>" placeholder="姓名、邮箱或学号" autocomplete="off">
                </div>
                <button type="submit" class="btn btn-secondary btn-small">应用筛选</button>
            </form>
            <p class="filter-meta">共 <strong><%= totalApplicantsForJob %></strong> 人投递本岗位；当前列表 <strong><%= applicantsForJob.size() %></strong> 人（随状态、匹配分与关键词变化）。</p>
            <% if (applicantsForJob.isEmpty()) { %>
            <p class="empty-hint"><%= totalApplicantsForJob > 0 ? "无符合当前筛选条件的应聘者，请调整筛选条件。" : "该岗位暂无申请人。" %></p>
            <% } else { %>
            <div class="table-wrap">
            <table>
                <thead>
                    <tr><th>姓名</th><th>学号</th><th>邮箱</th><th>电话</th><th>匹配分</th><th>技能短板</th><th>申请时间</th><th>操作</th></tr>
                </thead>
                <tbody>
                <% for (MatchHelper.ApplicantMatch m : applicantsForJob) {
                    String appId = "";
                    String status = "pending";
                    long appliedAt = 0L;
                    Application appRow = null;
                    for (Application app : applicationsForJob) {
                        if (app.getApplicantId().equals(m.applicant.getId())) {
                            appId = app.getId();
                            appRow = app;
                            status = app.getStatus() != null ? app.getStatus() : "pending";
                            appliedAt = app.getAppliedAt();
                            break;
                        }
                    }
                    String dispName = m.applicant.getName() != null ? m.applicant.getName() : "";
                    String jsName = dispName.replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"").replace("\r", " ").replace("\n", " ");
                    String sidDisp = m.applicant.getStudentId() != null && !m.applicant.getStudentId().isEmpty()
                            ? m.applicant.getStudentId() : "—";
                    String phoneDisp = m.applicant.getPhone() != null && !m.applicant.getPhone().trim().isEmpty()
                            ? m.applicant.getPhone().trim() : "—";
                %>
                    <tr>
                        <td><%= m.applicant.getName() %></td>
                        <td><%= sidDisp %></td>
                        <td><%= m.applicant.getEmail() %></td>
                        <td><%= phoneDisp %></td>
                        <td><span class="score"><%= m.score %> 分</span></td>
                        <td class="gaps"><%= m.gaps != null && !m.gaps.isEmpty() ? String.join(", ", m.gaps) : "无" %></td>
                        <td><%= appliedAt > 0 ? sdf.format(new Date(appliedAt)) : "—" %></td>
                        <td>
                            <% if ("pending".equals(status) && appId != null && !appId.isEmpty()) { %>
                            <form method="post" action="${pageContext.request.contextPath}/mo/job-applicants" style="display:inline;" onsubmit="return confirm('确定录用「<%= jsName %>」吗？\n录用后该岗位将自动关闭，且无法再筛选其他应聘者。');">
                                <input type="hidden" name="action" value="applicationStatus">
                                <input type="hidden" name="jobId" value="<%= job.getId() %>">
                                <input type="hidden" name="applicationId" value="<%= appId %>">
                                <input type="hidden" name="status" value="accepted">
                                <input type="hidden" name="filter" value="<%= filterAttr %>">
                                <input type="hidden" name="q" value="<%= qAttrEsc %>">
                                <input type="hidden" name="sort" value="<%= sortAttr %>">
                                <input type="hidden" name="minScore" value="<%= minScoreAttr %>">
                                <button type="submit" class="btn btn-primary btn-small">录用</button>
                            </form>
                            <form method="post" action="${pageContext.request.contextPath}/mo/job-applicants" style="display:inline;" onsubmit="return confirm('确定拒绝录用「<%= jsName %>」吗？');">
                                <input type="hidden" name="action" value="applicationStatus">
                                <input type="hidden" name="jobId" value="<%= job.getId() %>">
                                <input type="hidden" name="applicationId" value="<%= appId %>">
                                <input type="hidden" name="status" value="rejected">
                                <input type="hidden" name="filter" value="<%= filterAttr %>">
                                <input type="hidden" name="q" value="<%= qAttrEsc %>">
                                <input type="hidden" name="sort" value="<%= sortAttr %>">
                                <input type="hidden" name="minScore" value="<%= minScoreAttr %>">
                                <button type="submit" class="btn btn-secondary btn-small">拒绝</button>
                            </form>
                            <details class="interview-box">
                                <summary>安排面试/试讲</summary>
                                <form class="schedule-form" method="post" action="${pageContext.request.contextPath}/mo/job-applicants">
                                    <input type="hidden" name="action" value="scheduleInterview">
                                    <input type="hidden" name="jobId" value="<%= job.getId() %>">
                                    <input type="hidden" name="applicationId" value="<%= appId %>">
                                    <input type="hidden" name="filter" value="<%= filterAttr %>">
                                    <input type="hidden" name="q" value="<%= qAttrEsc %>">
                                    <input type="hidden" name="sort" value="<%= sortAttr %>">
                                    <input type="hidden" name="minScore" value="<%= minScoreAttr %>">
                                    <label for="iv-at-<%= appId %>">时间</label>
                                    <input id="iv-at-<%= appId %>" type="datetime-local" name="interviewAt" required>
                                    <label for="iv-d-<%= appId %>">地点或线上链接</label>
                                    <textarea id="iv-d-<%= appId %>" name="interviewDetail" rows="2" placeholder="例如：教三 201，或腾讯会议链接" required></textarea>
                                    <button type="submit" class="btn btn-primary btn-small">标记为待面试</button>
                                </form>
                            </details>
                            <% } else if ("pending".equals(status)) { %>
                            <span class="empty-hint" style="padding:0;font-size:.85rem;">无有效申请记录</span>
                            <% } else if ("interview".equals(status) && appId != null && !appId.isEmpty()) {
                                String det = appRow != null && appRow.getInterviewDetail() != null ? appRow.getInterviewDetail() : "";
                                String detEsc = det.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
                                long ivAt = appRow != null ? appRow.getInterviewAt() : 0L;
                                String ivTa = appRow != null ? appRow.getInterviewTaStatus() : Application.TA_IV_PENDING;
                                String taBadge;
                                String taBadgeClass;
                                if (Application.TA_IV_CONFIRMED.equals(ivTa)) { taBadge = "已确认"; taBadgeClass = "badge-accepted"; }
                                else if (Application.TA_IV_DECLINED.equals(ivTa)) { taBadge = "拒绝"; taBadgeClass = "badge-rejected"; }
                                else if (Application.TA_IV_RESCHEDULE.equals(ivTa)) { taBadge = "更换时间"; taBadgeClass = "badge-interview"; }
                                else { taBadge = "待确认"; taBadgeClass = "badge-pending"; }
                                boolean needReschedule = Application.TA_IV_DECLINED.equals(ivTa) || Application.TA_IV_RESCHEDULE.equals(ivTa);
                            %>
                            <div style="font-size:.8rem;color:#64748b;margin-bottom:.4rem;">
                                <% if (ivAt > 0) { %><strong><%= sdf.format(new Date(ivAt)) %></strong><br><% } %>
                                <span style="word-break:break-all;"><%= detEsc.isEmpty() ? "—" : detEsc %></span><br>
                                <span class="badge <%= taBadgeClass %>" style="margin-top:.25rem;"><%= taBadge %></span>
                            </div>
                            <form method="post" action="${pageContext.request.contextPath}/mo/job-applicants" style="display:inline;" onsubmit="return confirm('确定录用「<%= jsName %>」吗？\n录用后该岗位将自动关闭。');">
                                <input type="hidden" name="action" value="applicationStatus">
                                <input type="hidden" name="jobId" value="<%= job.getId() %>">
                                <input type="hidden" name="applicationId" value="<%= appId %>">
                                <input type="hidden" name="status" value="accepted">
                                <input type="hidden" name="filter" value="<%= filterAttr %>">
                                <input type="hidden" name="q" value="<%= qAttrEsc %>">
                                <input type="hidden" name="sort" value="<%= sortAttr %>">
                                <input type="hidden" name="minScore" value="<%= minScoreAttr %>">
                                <button type="submit" class="btn btn-primary btn-small">录用</button>
                            </form>
                            <form method="post" action="${pageContext.request.contextPath}/mo/job-applicants" style="display:inline;" onsubmit="return confirm('确定拒绝录用「<%= jsName %>」吗？');">
                                <input type="hidden" name="action" value="applicationStatus">
                                <input type="hidden" name="jobId" value="<%= job.getId() %>">
                                <input type="hidden" name="applicationId" value="<%= appId %>">
                                <input type="hidden" name="status" value="rejected">
                                <input type="hidden" name="filter" value="<%= filterAttr %>">
                                <input type="hidden" name="q" value="<%= qAttrEsc %>">
                                <input type="hidden" name="sort" value="<%= sortAttr %>">
                                <input type="hidden" name="minScore" value="<%= minScoreAttr %>">
                                <button type="submit" class="btn btn-secondary btn-small">拒绝</button>
                            </form>
                            <% if (needReschedule) { %>
                            <details class="interview-box" style="margin-top:.5rem;">
                                <summary>重新安排面试/试讲</summary>
                                <form class="schedule-form" method="post" action="${pageContext.request.contextPath}/mo/job-applicants">
                                    <input type="hidden" name="action" value="scheduleInterview">
                                    <input type="hidden" name="jobId" value="<%= job.getId() %>">
                                    <input type="hidden" name="applicationId" value="<%= appId %>">
                                    <input type="hidden" name="filter" value="<%= filterAttr %>">
                                    <input type="hidden" name="q" value="<%= qAttrEsc %>">
                                    <input type="hidden" name="sort" value="<%= sortAttr %>">
                                    <input type="hidden" name="minScore" value="<%= minScoreAttr %>">
                                    <label for="iv-at-re-<%= appId %>">新时间</label>
                                    <input id="iv-at-re-<%= appId %>" type="datetime-local" name="interviewAt" required>
                                    <label for="iv-d-re-<%= appId %>">地点或线上链接</label>
                                    <textarea id="iv-d-re-<%= appId %>" name="interviewDetail" rows="2" placeholder="更新后的教室或会议链接" required></textarea>
                                    <button type="submit" class="btn btn-primary btn-small">保存新安排</button>
                                </form>
                            </details>
                            <% } %>
                            <% } else if ("cancelled".equals(status)) { %>
                            <span class="badge badge-cancelled">已撤销</span>
                            <% } else { %>
                            <span class="badge badge-<%= "accepted".equals(status) ? "accepted" : "rejected" %>"><%= "accepted".equals(status) ? "已录用" : "已拒绝" %></span>
                            <% } %>
                        </td>
                    </tr>
                <% } %>
                </tbody>
            </table>
            </div>
            <% } %>
        </div>
        <% } %>
    </div>
    <script src="${pageContext.request.contextPath}/js/ui.js?v=1" defer></script>
</body>
</html>
