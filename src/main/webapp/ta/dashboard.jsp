<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.bupt.ta.model.Applicant" %>
<%@ page import="com.bupt.ta.model.Job" %>
<%@ page import="com.bupt.ta.web.TADashboardServlet" %>
<%@ page import="java.util.List" %>
<%
    Applicant applicant = (Applicant) request.getAttribute("applicant");
    if (applicant == null) return;
    @SuppressWarnings("unchecked")
    List<Job> openJobs = (List<Job>) request.getAttribute("openJobs");
    @SuppressWarnings("unchecked")
    List<TADashboardServlet.ApplicationWithJob> myApplications = (List<TADashboardServlet.ApplicationWithJob>) request.getAttribute("myApplications");
    if (openJobs == null) openJobs = java.util.Collections.emptyList();
    if (myApplications == null) myApplications = java.util.Collections.emptyList();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>我的申请 - 助教招聘系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=3">
    <style>
      *{box-sizing:border-box} body{margin:0;font-family:"PingFang SC","Microsoft YaHei",sans-serif;background:#f8fafc;color:#1e293b;min-height:100vh;line-height:1.6}
      .dashboard{max-width:960px;margin:0 auto;padding:1.5rem}
      .page-header{display:flex;align-items:center;justify-content:space-between;flex-wrap:wrap;gap:.75rem;margin-bottom:1.5rem;padding-bottom:1rem;border-bottom:1px solid #e2e8f0}
      .page-header h1{margin:0;font-size:1.4rem;font-weight:600;color:#1e293b}
      .back-link{padding:.45rem .85rem;color:#2563eb;text-decoration:none;font-size:.9rem;border-radius:6px}
      .back-link:hover{background:#dbeafe}
      .logout{font-size:.9rem;color:#64748b;text-decoration:none;padding:.45rem .85rem;border-radius:6px}
      .logout:hover{color:#dc2626;background:#fef2f2}
      .section{background:#fff;border-radius:10px;padding:1.35rem;margin-bottom:1.25rem;box-shadow:0 1px 3px rgba(0,0,0,.06);border:1px solid #e2e8f0;border-left:4px solid #2563eb}
      .section h2{font-size:1.05rem;font-weight:600;margin:0 0 1rem;color:#1e293b}
      .section p{margin:0 0 .75rem;color:#64748b;font-size:.9rem}
      .form-group{margin-bottom:1.1rem}
      .form-group label{display:block;font-size:.9rem;font-weight:500;margin-bottom:.4rem;color:#1e293b}
      .form-group input,.form-group textarea{width:100%;padding:.65rem .85rem;border:1px solid #e2e8f0;border-radius:6px;font-size:.95rem;font-family:inherit}
      .form-group input:focus,.form-group textarea:focus{outline:none;border-color:#2563eb;box-shadow:0 0 0 3px #dbeafe}
      .form-group textarea{min-height:100px;resize:vertical}
      .btn{display:inline-block;padding:.4rem .85rem;border:none;border-radius:6px;font-size:.875rem;font-weight:500;cursor:pointer;font-family:inherit;text-decoration:none}
      .btn-primary{background:#2563eb;color:#fff}.btn-primary:hover{background:#1d4ed8}
      .btn-secondary{background:#e2e8f0;color:#475569}.btn-secondary:hover{background:#cbd5e1}
      .table-wrap{overflow-x:auto;border-radius:6px;border:1px solid #e2e8f0;margin-top:.5rem}
      table{width:100%;border-collapse:collapse;font-size:.9rem}
      th,td{padding:.7rem .9rem;text-align:left;border-bottom:1px solid #e2e8f0}
      tr:last-child td{border-bottom:none}
      th{background:#f1f5f9;color:#1e293b;font-weight:600;font-size:.85rem}
      tbody tr:hover{background:#f8fafc}
      td form{display:inline-flex;align-items:center;gap:.4rem;flex-wrap:wrap}
      td form input[type="text"]{padding:.4rem .6rem;border:1px solid #e2e8f0;border-radius:6px;font-size:.85rem;width:120px}
      .badge{display:inline-block;padding:.25rem .6rem;border-radius:999px;font-size:.8rem;font-weight:500}
      .badge-pending{background:#fef3c7;color:#92400e}.badge-accepted{background:#d1fae5;color:#065f46}.badge-rejected{background:#fee2e2;color:#991b1b}
      .badge-cancelled{background:#f1f5f9;color:#475569}
      .empty-hint{color:#64748b;font-size:.9rem;padding:1rem 0}
      .section-hint{font-size:.85rem;color:#64748b;margin-bottom:1rem}
      .resume-current{font-size:.9rem;margin-bottom:1rem}.resume-current a{text-decoration:none}
      .form-group input[type="file"]{padding:.4rem 0}
      .section-tip{border-left-color:#d97706}
      .skill-gaps-list{margin:.5rem 0 0 1.2rem;padding:0;color:#92400e}
      .skill-gaps-list li{margin:.35rem 0}
    </style>
</head>
<body>
    <div class="dashboard">
        <div class="page-header">
            <a href="${pageContext.request.contextPath}/" class="back-link">← 首页</a>
            <h1>你好，<%= applicant.getName() %></h1>
            <a href="${pageContext.request.contextPath}/ta/auth?logout=1" class="logout">退出登录</a>
        </div>

        <div class="section">
            <h2>个人档案</h2>
            <form method="post" action="${pageContext.request.contextPath}/ta/dashboard">
                <input type="hidden" name="action" value="updateProfile">
                <div class="form-group">
                    <label>姓名</label>
                    <input type="text" name="name" value="<%= applicant.getName() != null ? applicant.getName() : "" %>">
                </div>
                <div class="form-group">
                    <label>学号</label>
                    <input type="text" name="studentId" value="<%= applicant.getStudentId() != null ? applicant.getStudentId() : "" %>">
                </div>
                <div class="form-group">
                    <label>技能（逗号分隔，如：Java, Python, 监考）</label>
                    <input type="text" name="skills" value="<%= applicant.getSkills() != null ? String.join(", ", applicant.getSkills()) : "" %>">
                </div>
                <button type="submit" class="btn btn-primary btn-small">保存</button>
            </form>
        </div>

        <div class="section">
            <h2>上传简历</h2>
            <p class="section-hint">支持纯文本（.txt）、PDF（.pdf）、Word（.doc / .docx），可粘贴文本或上传文件，单文件不超过 5MB。</p>
            <% if (request.getAttribute("resumeFilename") != null) { %>
            <p class="resume-current">
                当前简历：<strong><%= request.getAttribute("resumeFilename") %></strong>
                <a href="${pageContext.request.contextPath}/ta/resume" class="btn btn-secondary btn-small" style="margin-left:0.5rem;">下载</a>
            </p>
            <% } %>
            <form method="post" action="${pageContext.request.contextPath}/ta/dashboard" enctype="multipart/form-data">
                <input type="hidden" name="action" value="resume">
                <div class="form-group">
                    <label>上传文件（.txt / .pdf / .doc / .docx）</label>
                    <input type="file" name="resumeFile" accept=".txt,.pdf,.doc,.docx">
                </div>
                <div class="form-group">
                    <label>或粘贴纯文本（保存为 .txt）</label>
                    <textarea name="resumeContent" placeholder="粘贴或输入简历内容，将保存为 .txt"><%
                        Boolean resumeIsText = (Boolean) request.getAttribute("resumeIsText");
                        if (Boolean.TRUE.equals(resumeIsText)) {
                            String resumeContent = (String) request.getAttribute("resumeContent");
                            if (resumeContent != null) out.print(resumeContent);
                        }
                    %></textarea>
                </div>
                <button type="submit" class="btn btn-primary btn-small">保存简历</button>
            </form>
        </div>

        <%
            java.util.List<String> resumeSkillGaps = (java.util.List<String>) request.getAttribute("resumeSkillGaps");
            if (resumeSkillGaps != null && !resumeSkillGaps.isEmpty()) {
        %>
        <div class="section section-tip">
            <h2>技能短板提示</h2>
            <p class="section-hint">根据您填写的个人技能与简历内容，对比当前开放岗位需求，以下技能在岗位中较常要求但您尚未体现，建议在个人档案或简历中补充。</p>
            <ul class="skill-gaps-list">
                <% for (String skill : resumeSkillGaps) { %>
                <li><%= skill %></li>
                <% } %>
            </ul>
            <p class="section-hint">完善后有助于提高与岗位的匹配度，获得更多录用机会。</p>
        </div>
        <% } %>

        <div class="section">
            <h2>可申请岗位</h2>
            <% if (openJobs.isEmpty()) { %>
            <p class="empty-hint">暂无开放岗位。</p>
            <% } else { %>
            <div class="table-wrap">
            <table>
                <thead>
                    <tr><th>岗位名称</th><th>类型</th><th>所需技能</th><th>操作</th></tr>
                </thead>
                <tbody>
                <% for (Job j : openJobs) { %>
                    <tr>
                        <td><%= j.getTitle() %></td>
                        <td><%= j.getType() != null ? j.getType() : "-" %></td>
                        <td><%= j.getRequiredSkills() != null ? String.join(", ", j.getRequiredSkills()) : "-" %></td>
                        <td>
                            <form method="post" action="${pageContext.request.contextPath}/ta/dashboard" style="display:inline;">
                                <input type="hidden" name="action" value="apply">
                                <input type="hidden" name="jobId" value="<%= j.getId() %>">
                                <input type="text" name="note" placeholder="备注(可选)" size="12">
                                <button type="submit" class="btn btn-primary btn-small">申请</button>
                            </form>
                        </td>
                    </tr>
                <% } %>
                </tbody>
            </table>
            </div>
            <% } %>
        </div>

        <div class="section">
            <h2>我的申请状态</h2>
            <% if (myApplications.isEmpty()) { %>
            <p class="empty-hint">暂无申请记录。</p>
            <% } else { %>
            <div class="table-wrap">
            <table>
                <thead>
                    <tr><th>岗位</th><th>状态</th><th>申请时间</th><th>操作</th></tr>
                </thead>
                <tbody>
                <% for (TADashboardServlet.ApplicationWithJob awj : myApplications) {
                    String status = awj.application.getStatus();
                    String badgeClass = "pending";
                    if ("accepted".equals(status)) badgeClass = "accepted";
                    else if ("rejected".equals(status)) badgeClass = "rejected";
                    else if ("cancelled".equals(status)) badgeClass = "cancelled";
                    String jobTitle = awj.job != null ? awj.job.getTitle() : awj.application.getJobId();
                %>
                    <tr>
                        <td><%= jobTitle %></td>
                        <td><span class="badge badge-<%= badgeClass %>"><%= "pending".equals(status) ? "待审核" : "accepted".equals(status) ? "已录用" : "rejected".equals(status) ? "已拒绝" : "已取消" %></span></td>
                        <td><%= new java.util.Date(awj.application.getAppliedAt()) %></td>
                        <td>
                            <% if ("pending".equals(status)) { %>
                            <form method="post" action="${pageContext.request.contextPath}/ta/dashboard" style="display:inline;">
                                <input type="hidden" name="action" value="cancelApplication">
                                <input type="hidden" name="applicationId" value="<%= awj.application.getId() %>">
                                <button type="submit" class="btn btn-secondary btn-small">取消申请</button>
                            </form>
                            <% } else { %>
                            —
                            <% } %>
                        </td>
                    </tr>
                <% } %>
                </tbody>
            </table>
            </div>
            <% } %>
        </div>
    </div>
</body>
</html>
