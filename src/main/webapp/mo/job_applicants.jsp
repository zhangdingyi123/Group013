<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.bupt.ta.model.Job" %>
<%@ page import="com.bupt.ta.model.Application" %>
<%@ page import="com.bupt.ta.service.MatchHelper" %>
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
    </style>
</head>
<body>
    <div class="page">
        <div class="page-header">
            <a href="${pageContext.request.contextPath}/mo/dashboard" class="back-link">← 返回我的岗位</a>
            <h1><%= job != null ? ("筛选应聘者：" + job.getTitle()) : "筛选应聘者" %></h1>
        </div>

        <% if (job == null) { %>
        <div class="section">
            <p class="empty-hint" style="margin:0;"><%= pageError != null ? pageError : "岗位不存在或无权访问。" %></p>
            <p style="margin-top:1rem;"><a href="${pageContext.request.contextPath}/mo/dashboard" class="back-link">返回我的岗位</a></p>
        </div>
        <% } else { %>
        <div class="section">
            <h2>系统推荐（按匹配度与负荷均衡排序）</h2>
            <p class="section-desc">匹配分：岗位技能匹配度；技能短板：岗位需要但应聘者未体现的技能。可根据此列表进行录用或拒绝。</p>
            <% if (applicantsForJob.isEmpty()) { %>
            <p class="empty-hint">该岗位暂无申请人。</p>
            <% } else { %>
            <div class="table-wrap">
            <table>
                <thead>
                    <tr><th>姓名</th><th>邮箱</th><th>匹配分</th><th>技能短板</th><th>操作</th></tr>
                </thead>
                <tbody>
                <% for (MatchHelper.ApplicantMatch m : applicantsForJob) {
                    String appId = "";
                    String status = "pending";
                    for (Application app : applicationsForJob) {
                        if (app.getApplicantId().equals(m.applicant.getId())) {
                            appId = app.getId();
                            status = app.getStatus() != null ? app.getStatus() : "pending";
                            break;
                        }
                    }
                %>
                    <tr>
                        <td><%= m.applicant.getName() %></td>
                        <td><%= m.applicant.getEmail() %></td>
                        <td><span class="score"><%= m.score %> 分</span></td>
                        <td class="gaps"><%= m.gaps != null && !m.gaps.isEmpty() ? String.join(", ", m.gaps) : "无" %></td>
                        <td>
                            <% if ("pending".equals(status)) { %>
                            <form method="post" action="${pageContext.request.contextPath}/mo/job-applicants" style="display:inline;">
                                <input type="hidden" name="action" value="applicationStatus">
                                <input type="hidden" name="jobId" value="<%= job.getId() %>">
                                <input type="hidden" name="applicationId" value="<%= appId %>">
                                <input type="hidden" name="status" value="accepted">
                                <button type="submit" class="btn btn-primary btn-small">录用</button>
                            </form>
                            <form method="post" action="${pageContext.request.contextPath}/mo/job-applicants" style="display:inline;">
                                <input type="hidden" name="action" value="applicationStatus">
                                <input type="hidden" name="jobId" value="<%= job.getId() %>">
                                <input type="hidden" name="applicationId" value="<%= appId %>">
                                <input type="hidden" name="status" value="rejected">
                                <button type="submit" class="btn btn-secondary btn-small">拒绝</button>
                            </form>
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
