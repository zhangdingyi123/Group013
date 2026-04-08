<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.bupt.ta.util.I18n" %>
<%@ page import="com.bupt.ta.web.AdminServlet" %>
<%@ page import="java.util.List" %>
<%
    @SuppressWarnings("unchecked")
    List<AdminServlet.WorkloadEntry> workload = (List<AdminServlet.WorkloadEntry>) request.getAttribute("workload");
    Integer totalAssignments = (Integer) request.getAttribute("totalAssignments");
    Integer taCount = (Integer) request.getAttribute("taCount");
    if (workload == null) workload = java.util.Collections.emptyList();
    if (totalAssignments == null) totalAssignments = 0;
    if (taCount == null) taCount = 0;
    @SuppressWarnings("unchecked")
    java.util.List<AdminServlet.AcceptedAssignment> acceptedDetails = (java.util.List<AdminServlet.AcceptedAssignment>) request.getAttribute("acceptedDetails");
    if (acceptedDetails == null) acceptedDetails = java.util.Collections.emptyList();
%>
<!DOCTYPE html>
<html lang="<%= I18n.htmlLangAttr(request) %>">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= I18n.msg(request, "admin.wl.title") %></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=4">
    <style>
      *{box-sizing:border-box}
      body.admin-workload-page{
        margin:0;
        font-family:"PingFang SC","Microsoft YaHei",sans-serif;
        color:#1e293b;
        min-height:100vh;
        line-height:1.6;
        background:linear-gradient(165deg,#eef2ff 0%,#e0f2fe 38%,#f1f5f9 72%,#f8fafc 100%);
        position:relative;
      }
      body.admin-workload-page::before{
        content:"";
        position:fixed;
        inset:0;
        background:
          radial-gradient(ellipse 90% 55% at 50% -15%,rgba(99,102,241,.14),transparent 55%),
          radial-gradient(ellipse 70% 45% at 100% 0,rgba(59,130,246,.10),transparent 50%),
          radial-gradient(ellipse 50% 35% at 0 100%,rgba(14,165,233,.08),transparent 45%);
        pointer-events:none;
        z-index:0;
      }
      .dashboard.admin-workload{
        position:relative;
        z-index:1;
        max-width:920px;
        margin:0 auto;
        padding:clamp(1.25rem,4vw,2.25rem) 1.25rem 2.5rem;
      }
      .panel{
        background:rgba(255,255,255,.78);
        backdrop-filter:saturate(140%) blur(14px);
        -webkit-backdrop-filter:saturate(140%) blur(14px);
        border-radius:16px;
        border:1px solid rgba(255,255,255,.9);
        box-shadow:0 4px 24px rgba(15,23,42,.07),0 1px 3px rgba(15,23,42,.04);
        padding:1.5rem 1.35rem 1.65rem;
      }
      @supports not (backdrop-filter:blur(1px)){
        .panel{background:#fff}
      }
      .page-header{
        display:grid;
        grid-template-columns:minmax(0,1fr) auto minmax(0,1fr);
        align-items:center;
        gap:.75rem;
        margin-bottom:1.15rem;
        padding-bottom:1.1rem;
        border-bottom:1px solid rgba(226,232,240,.95);
      }
      .page-header .back-link{justify-self:start}
      .page-header .logout{justify-self:end}
      .page-header h1{
        margin:0;
        text-align:center;
        font-size:clamp(1.2rem,2.8vw,1.45rem);
        font-weight:700;
        letter-spacing:-.02em;
        color:#0f172a;
        background:linear-gradient(120deg,#1e3a8a,#2563eb,#0d9488);
        -webkit-background-clip:text;
        background-clip:text;
        -webkit-text-fill-color:transparent;
      }
      @media (max-width:520px){
        .page-header{grid-template-columns:1fr 1fr}
        .page-header h1{grid-column:1/-1;grid-row:1;margin-bottom:.25rem}
        .page-header .back-link{grid-column:1;grid-row:2}
        .page-header .logout{grid-column:2;grid-row:2}
      }
      .back-link,.logout{
        font-size:.9rem;
        text-decoration:none;
        padding:.5rem .95rem;
        border-radius:10px;
        transition:background .2s,color .2s,box-shadow .2s;
      }
      .back-link{color:#2563eb;background:rgba(219,234,254,.65);border:1px solid rgba(191,219,254,.8)}
      .back-link:hover{background:#dbeafe;box-shadow:0 2px 8px rgba(37,99,235,.12)}
      .logout{color:#64748b;background:rgba(248,250,252,.8);border:1px solid #e2e8f0}
      .logout:hover{color:#dc2626;background:#fef2f2;border-color:#fecaca}
      .intro{color:#475569;font-size:.92rem;margin:0 0 1rem}
      .summary{
        position:relative;
        padding:.85rem 1.1rem .85rem 1.25rem;
        background:linear-gradient(135deg,rgba(219,234,254,.95),rgba(224,242,254,.9));
        color:#1d4ed8;
        border-radius:12px;
        margin-bottom:1.25rem;
        font-weight:600;
        border:1px solid rgba(191,219,254,.85);
        box-shadow:0 1px 0 rgba(255,255,255,.8) inset;
      }
      .summary::before{
        content:"";
        position:absolute;
        left:0;top:50%;
        transform:translateY(-50%);
        width:4px;height:60%;
        border-radius:4px;
        background:linear-gradient(180deg,#2563eb,#0d9488);
      }
      .error{color:#dc2626;font-size:.9rem;margin-bottom:1rem;padding:.65rem .95rem;background:#fef2f2;border-radius:10px;border:1px solid #fecaca}
      .notice{color:#0f766e;font-size:.9rem;margin-bottom:1rem;padding:.65rem .95rem;background:#ecfdf5;border-radius:10px;border:1px solid #99f6e4}
      .workload-tabs{margin-top:.25rem}
      .wtab-input{position:absolute;opacity:0;width:0;height:0;pointer-events:none}
      .tab-bar{display:flex;gap:.35rem;margin:0 0 0;border-bottom:2px solid #e2e8f0;padding:0}
      .tab-bar label{
        flex:1;min-width:0;text-align:center;padding:.65rem 1rem;font-size:.9rem;font-weight:500;color:#64748b;
        cursor:pointer;border-radius:10px 10px 0 0;border:1px solid transparent;border-bottom:none;margin-bottom:-2px;
        transition:background .15s,color .15s,border-color .15s;
      }
      .tab-bar label:hover{color:#334155;background:rgba(241,245,249,.6)}
      #wtab-summary:checked ~ .tab-bar label[for="wtab-summary"],
      #wtab-detail:checked ~ .tab-bar label[for="wtab-detail"]{
        color:#1d4ed8;font-weight:600;background:rgba(255,255,255,.95);
        border-color:#e2e8f0;border-bottom-color:#fff;box-shadow:0 -2px 12px rgba(37,99,235,.08);
      }
      .tab-panel{display:none;padding-top:1rem}
      #wtab-summary:checked ~ .tab-panel.panel-summary{display:block}
      #wtab-detail:checked ~ .tab-panel.panel-detail{display:block}
      .tab-panel .panel-intro{color:#64748b;font-size:.88rem;margin:0 0 .85rem;line-height:1.55}
      .btn-danger{display:inline-block;padding:.4rem .75rem;border:none;border-radius:8px;font-size:.82rem;font-weight:500;cursor:pointer;font-family:inherit;background:#fee2e2;color:#991b1b}
      .btn-danger:hover{background:#fecaca}
      .empty-hint{color:#64748b;font-size:.9rem;padding:1.25rem 0;text-align:center}
      .table-wrap{
        overflow-x:auto;
        border-radius:12px;
        border:1px solid #e2e8f0;
        margin-top:.35rem;
        background:#fff;
        box-shadow:0 1px 2px rgba(15,23,42,.04);
      }
      table{width:100%;border-collapse:collapse;font-size:.9rem}
      th,td{padding:.75rem 1rem;text-align:left;border-bottom:1px solid #f1f5f9}
      tr:last-child td{border-bottom:none}
      th{
        background:linear-gradient(180deg,#f8fafc,#f1f5f9);
        color:#334155;
        font-weight:600;
        font-size:.82rem;
        letter-spacing:.02em;
      }
      tbody tr{transition:background .15s ease}
      tbody tr:hover{background:linear-gradient(90deg,rgba(239,246,255,.9),rgba(248,250,252,.95))}
    </style>
</head>
<body class="admin-workload-page">
    <div class="dashboard admin-workload">
      <div class="panel">
        <div class="page-header">
            <a href="${pageContext.request.contextPath}/" class="back-link"><%= I18n.msg(request, "header.backHome") %></a>
            <h1><%= I18n.msg(request, "admin.wl.h1") %></h1>
            <a href="${pageContext.request.contextPath}/admin/auth?logout=1" class="logout"><%= I18n.msg(request, "common.logout") %></a>
        </div>
        <div style="text-align:right;margin-bottom:0.65rem;"><jsp:include page="/WEB-INF/jsp/lang_switch.jsp"/></div>
        <p class="intro"><%= I18n.msg(request, "admin.wl.intro") %></p>
        <p class="summary"><%= I18n.msg(request, "admin.wl.summary", taCount, totalAssignments) %></p>
        <% if (request.getAttribute("error") != null) { %>
        <p class="error"><%= request.getAttribute("error") %></p>
        <% } %>
        <% if (request.getAttribute("notice") != null) { %>
        <p class="notice"><%= request.getAttribute("notice") %></p>
        <% } %>
        <% if (workload.isEmpty()) { %>
        <p class="empty-hint"><%= I18n.msg(request, "admin.wl.empty") %></p>
        <% } else { %>
        <div class="workload-tabs">
            <input type="radio" name="wtab" id="wtab-summary" class="wtab-input" checked>
            <input type="radio" name="wtab" id="wtab-detail" class="wtab-input">
            <div class="tab-bar" role="tablist" aria-label="<%= I18n.msg(request, "admin.wl.tab.aria") %>">
                <label for="wtab-summary" role="tab"><%= I18n.msg(request, "admin.wl.tab.summary") %></label>
                <label for="wtab-detail" role="tab"><%= I18n.msg(request, "admin.wl.tab.detail") %></label>
            </div>
            <div class="tab-panel panel-summary" id="panel-summary" role="tabpanel">
                <p class="panel-intro"><%= I18n.msg(request, "admin.wl.panel.summary.intro") %></p>
                <div class="table-wrap">
                <table>
                    <thead>
                        <tr><th><%= I18n.msg(request, "admin.wl.th.name") %></th><th><%= I18n.msg(request, "admin.wl.th.email") %></th><th><%= I18n.msg(request, "admin.wl.th.count") %></th></tr>
                    </thead>
                    <tbody>
                    <% for (AdminServlet.WorkloadEntry e : workload) { %>
                        <tr>
                            <td><%= e.name %></td>
                            <td><%= e.email %></td>
                            <td><strong><%= e.count %></strong></td>
                        </tr>
                    <% } %>
                    </tbody>
                </table>
                </div>
            </div>
            <div class="tab-panel panel-detail" id="panel-detail" role="tabpanel">
                <p class="panel-intro"><%= I18n.msg(request, "admin.wl.panel.detail.intro") %></p>
                <% if (acceptedDetails.isEmpty()) { %>
                <p class="empty-hint"><%= I18n.msg(request, "admin.wl.detail.empty") %></p>
                <% } else { %>
                <div class="table-wrap">
                <table>
                    <thead>
                        <tr><th><%= I18n.msg(request, "admin.wl.th.ta") %></th><th><%= I18n.msg(request, "admin.wl.th.email") %></th><th><%= I18n.msg(request, "admin.wl.th.job") %></th><th><%= I18n.msg(request, "common.ops") %></th></tr>
                    </thead>
                    <tbody>
                    <% for (AdminServlet.AcceptedAssignment row : acceptedDetails) {
                        String dispName = row.applicantName != null ? row.applicantName : "";
                        String jobDisp = row.jobTitle != null ? row.jobTitle : "";
                        String cfmRaw = I18n.msg(request, "admin.wl.cancel.confirm", dispName, jobDisp);
                        String jsCfm = cfmRaw.replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"").replace("\r", " ").replace("\n", "\\n");
                    %>
                        <tr>
                            <td><%= row.applicantName %></td>
                            <td><%= row.applicantEmail %></td>
                            <td><%= row.jobTitle %></td>
                            <td>
                                <form method="post" action="${pageContext.request.contextPath}/admin/workload" style="display:inline;margin:0"
                                      onsubmit="return confirm('<%= jsCfm %>');">
                                    <input type="hidden" name="action" value="cancelApplication">
                                    <input type="hidden" name="applicationId" value="<%= row.applicationId %>">
                                    <button type="submit" class="btn-danger"><%= I18n.msg(request, "admin.wl.cancel") %></button>
                                </form>
                            </td>
                        </tr>
                    <% } %>
                    </tbody>
                </table>
                </div>
                <% } %>
            </div>
        </div>
        <% } %>
      </div>
    </div>
</body>
</html>
