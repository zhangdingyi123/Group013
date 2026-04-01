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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=3">
    <style>
      *{box-sizing:border-box} body{margin:0;font-family:"PingFang SC","Microsoft YaHei",sans-serif;background:#f8fafc;color:#1e293b;min-height:100vh;line-height:1.6}
      .page{max-width:960px;margin:0 auto;padding:1.5rem}
      .page-header{display:flex;align-items:center;justify-content:space-between;flex-wrap:wrap;gap:.75rem;margin-bottom:1.5rem;padding-bottom:1rem;border-bottom:1px solid #e2e8f0}
      .page-header h1{margin:0;font-size:1.4rem;font-weight:600;color:#1e293b}
      .back-link{padding:.45rem .85rem;color:#2563eb;text-decoration:none;font-size:.9rem;border-radius:6px}
      .back-link:hover{background:#dbeafe}
      .section{background:#fff;border-radius:10px;padding:1.35rem;margin-bottom:1.25rem;box-shadow:0 1px 3px rgba(0,0,0,.06);border:1px solid #e2e8f0;border-left:4px solid #2563eb}
      .section h2{font-size:1.05rem;font-weight:600;margin:0 0 1rem;color:#1e293b}
      .section-desc{margin:0 0 .75rem;color:#64748b;font-size:.9rem}
      .btn{display:inline-block;padding:.4rem .85rem;border:none;border-radius:6px;font-size:.875rem;font-weight:500;cursor:pointer;font-family:inherit;text-decoration:none}
      .btn-primary{background:#2563eb;color:#fff}.btn-primary:hover{background:#1d4ed8}
      .btn-secondary{background:#e2e8f0;color:#475569}.btn-secondary:hover{background:#cbd5e1}
      .btn-small{font-size:.8rem;padding:.35rem .65rem}
      .table-wrap{overflow-x:auto;border-radius:6px;border:1px solid #e2e8f0;margin-top:.5rem}
      table{width:100%;border-collapse:collapse;font-size:.9rem}
      th,td{padding:.7rem .9rem;text-align:left;border-bottom:1px solid #e2e8f0}
      tr:last-child td{border-bottom:none}
      th{background:#f1f5f9;color:#1e293b;font-weight:600;font-size:.85rem}
      tbody tr:hover{background:#f8fafc}
      .badge{display:inline-block;padding:.25rem .6rem;border-radius:999px;font-size:.8rem;font-weight:500}
      .badge-pending{background:#fef3c7;color:#92400e}.badge-accepted{background:#d1fae5;color:#065f46}.badge-rejected{background:#fee2e2;color:#991b1b}
      .score{font-weight:600;color:#2563eb}.gaps{font-size:.85rem;color:#64748b}
      .empty-hint{color:#64748b;font-size:.9rem;padding:1rem 0}
      .filter-bar{display:flex;flex-wrap:wrap;gap:.65rem;align-items:flex-end;margin-bottom:.75rem;padding:.85rem 1rem;background:#f8fafc;border-radius:8px;border:1px solid #e2e8f0}
      .filter-bar label{font-size:.8rem;font-weight:500;color:#475569;display:block;margin-bottom:.25rem}
      .filter-bar select,.filter-bar input[type=text]{padding:.45rem .65rem;border:1px solid #e2e8f0;border-radius:6px;font-size:.875rem;font-family:inherit;min-width:140px}
      .filter-bar input[type=text]{min-width:200px}
      .filter-bar .btn{margin-bottom:0}
      .filter-meta{margin:0 0 1rem;font-size:.85rem;color:#64748b}
      .filter-meta strong{color:#334155}
      .badge-cancelled{background:#f1f5f9;color:#475569}
    </style>
</head>
<body>
    <div class="page">
        <div class="page-header">
            <a href="${pageContext.request.contextPath}/mo/dashboard?tab=positions" class="back-link">← 返回我的岗位</a>
            <h1><%= job != null ? ("筛选应聘者：" + job.getTitle()) : "筛选应聘者" %></h1>
        </div>

        <% if (job == null) { %>
        <div class="section">
            <p class="empty-hint" style="margin:0;"><%= pageError != null ? pageError : "岗位不存在或无权访问。" %></p>
            <p style="margin-top:1rem;"><a href="${pageContext.request.contextPath}/mo/dashboard?tab=positions" class="back-link">返回我的岗位</a></p>
        </div>
        <% } else { %>
        <div class="section">
            <h2>系统推荐（按匹配度与负荷均衡排序）</h2>
            <p class="section-desc">匹配分：岗位技能匹配度；技能短板：岗位需要但应聘者未体现的技能。可根据此列表进行录用或拒绝。<strong>录用任意一名待处理应聘者后，该岗位将自动关闭</strong>，且关闭后不可再进入本筛选页。</p>
            <form class="filter-bar" method="get" action="${pageContext.request.contextPath}/mo/job-applicants">
                <input type="hidden" name="jobId" value="<%= job.getId() %>">
                <div>
                    <label for="filter-status">申请状态</label>
                    <select id="filter-status" name="filter">
                        <option value="all" <%= "all".equals(filterAttr) ? "selected" : "" %>>全部</option>
                        <option value="pending" <%= "pending".equals(filterAttr) ? "selected" : "" %>>待处理</option>
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
                    <tr><th>姓名</th><th>学号</th><th>邮箱</th><th>匹配分</th><th>技能短板</th><th>申请时间</th><th>操作</th></tr>
                </thead>
                <tbody>
                <% for (MatchHelper.ApplicantMatch m : applicantsForJob) {
                    String appId = "";
                    String status = "pending";
                    long appliedAt = 0L;
                    for (Application app : applicationsForJob) {
                        if (app.getApplicantId().equals(m.applicant.getId())) {
                            appId = app.getId();
                            status = app.getStatus() != null ? app.getStatus() : "pending";
                            appliedAt = app.getAppliedAt();
                            break;
                        }
                    }
                    String dispName = m.applicant.getName() != null ? m.applicant.getName() : "";
                    String jsName = dispName.replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"").replace("\r", " ").replace("\n", " ");
                    String sidDisp = m.applicant.getStudentId() != null && !m.applicant.getStudentId().isEmpty()
                            ? m.applicant.getStudentId() : "—";
                %>
                    <tr>
                        <td><%= m.applicant.getName() %></td>
                        <td><%= sidDisp %></td>
                        <td><%= m.applicant.getEmail() %></td>
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
                            <% } else if ("pending".equals(status)) { %>
                            <span class="empty-hint" style="padding:0;font-size:.85rem;">无有效申请记录</span>
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
</body>
</html>
