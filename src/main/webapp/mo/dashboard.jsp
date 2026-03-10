<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.bupt.ta.model.Job" %>
<%@ page import="java.util.List" %>
<%
    @SuppressWarnings("unchecked")
    List<Job> myJobs = (List<Job>) request.getAttribute("myJobs");
    if (myJobs == null) myJobs = java.util.Collections.emptyList();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>课程组织者 - 助教招聘系统</title>
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
      .section p,.section-desc{margin:0 0 .75rem;color:#64748b;font-size:.9rem}
      .form-group{margin-bottom:1.1rem}
      .form-group label{display:block;font-size:.9rem;font-weight:500;margin-bottom:.4rem;color:#1e293b}
      .form-group input,.form-group textarea,.form-group select{width:100%;padding:.65rem .85rem;border:1px solid #e2e8f0;border-radius:6px;font-size:.95rem;font-family:inherit}
      .form-group input:focus,.form-group textarea:focus,.form-group select:focus{outline:none;border-color:#2563eb;box-shadow:0 0 0 3px #dbeafe}
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
      .badge{display:inline-block;padding:.25rem .6rem;border-radius:999px;font-size:.8rem;font-weight:500}
      .badge-pending{background:#fef3c7;color:#92400e}.badge-accepted{background:#d1fae5;color:#065f46}.badge-rejected{background:#fee2e2;color:#991b1b}
      .badge-open{background:#dbeafe;color:#1e40af}.badge-closed{background:#f1f5f9;color:#475569}
      .score{font-weight:600;color:#2563eb}.gaps{font-size:.85rem;color:#64748b}
      .empty-hint{color:#64748b;font-size:.9rem;padding:1rem 0}
    </style>
</head>
<body>
    <div class="dashboard">
        <div class="page-header">
            <a href="${pageContext.request.contextPath}/" class="back-link">← 首页</a>
            <h1>课程组织者工作台</h1>
            <a href="${pageContext.request.contextPath}/mo/auth?logout=1" class="logout">退出登录</a>
        </div>

        <div class="section">
            <h2>发布新岗位</h2>
            <form method="post" action="${pageContext.request.contextPath}/mo/dashboard">
                <input type="hidden" name="action" value="createJob">
                <div class="form-group">
                    <label>岗位名称</label>
                    <input type="text" name="title" required placeholder="例如：数据结构课程助教">
                </div>
                <div class="form-group">
                    <label>岗位描述</label>
                    <textarea name="description" placeholder="岗位职责与说明"></textarea>
                </div>
                <div class="form-group">
                    <label>类型</label>
                    <select name="type">
                        <option value="course_ta">课程助教</option>
                        <option value="invigilation">监考</option>
                        <option value="activity">活动支持</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>所需技能（逗号分隔）</label>
                    <input type="text" name="requiredSkills" placeholder="Java, Python, 监考">
                </div>
                <button type="submit" class="btn btn-primary">发布</button>
            </form>
        </div>

        <div class="section">
            <h2>我的岗位</h2>
            <% if (myJobs.isEmpty()) { %>
            <p class="empty-hint">暂无岗位。请在上方发布。</p>
            <% } else { %>
            <div class="table-wrap">
            <table>
                <thead>
                    <tr><th>岗位名称</th><th>类型</th><th>状态</th><th>操作</th></tr>
                </thead>
                <tbody>
                <% for (Job j : myJobs) {
                    String statusClass = "open".equals(j.getStatus()) ? "badge-open" : "badge-closed";
                %>
                    <tr>
                        <td><%= j.getTitle() %></td>
                        <td><%= j.getType() != null ? j.getType() : "-" %></td>
                        <td><span class="badge <%= statusClass %>"><%= j.getStatus() %></span></td>
                        <td>
                            <a href="${pageContext.request.contextPath}/mo/job-applicants?jobId=<%= j.getId() %>" class="btn btn-secondary btn-small">筛选应聘者</a>
                            <% if ("open".equals(j.getStatus())) { %>
                            <form method="post" action="${pageContext.request.contextPath}/mo/dashboard" style="display:inline;">
                                <input type="hidden" name="action" value="closeJob">
                                <input type="hidden" name="jobId" value="<%= j.getId() %>">
                                <button type="submit" class="btn btn-secondary btn-small">关闭岗位</button>
                            </form>
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
