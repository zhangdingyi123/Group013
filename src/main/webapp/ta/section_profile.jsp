<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.bupt.ta.model.Applicant" %>
<%@ page import="java.util.List" %>
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
    Applicant applicant = (Applicant) request.getAttribute("applicant");
    String skillsJoined = (String) request.getAttribute("applicantSkillsJoined");
    if (skillsJoined == null) skillsJoined = "";
    String ctx = request.getContextPath();

    String dispName = applicant.getName() != null && !applicant.getName().trim().isEmpty()
            ? applicant.getName().trim() : "用户";
    String avLetter = dispName.substring(0, 1);
    String emailDisp = applicant.getEmail() != null && !applicant.getEmail().isEmpty()
            ? applicant.getEmail() : "—";
    String sidDisp = applicant.getStudentId() != null && !applicant.getStudentId().isEmpty()
            ? applicant.getStudentId() : "未填写";
    String rp = applicant.getResumePath();
    boolean hasResume = rp != null && !rp.trim().isEmpty();
    String resumeLabel = hasResume ? "已上传" : "未上传";
    boolean hasSkills = skillsJoined != null && !skillsJoined.trim().isEmpty();
    boolean nameOk = applicant.getName() != null && !applicant.getName().trim().isEmpty();
    boolean sidOk = applicant.getStudentId() != null && !applicant.getStudentId().trim().isEmpty();
    int pct = 0;
    if (nameOk) pct += 25;
    if (sidOk) pct += 25;
    if (hasSkills) pct += 25;
    if (hasResume) pct += 25;
    List<String> skillList = applicant.getSkills();
%>
<div id="pc-overview" class="pc-profile-stack">
    <section class="pc-card pc-hero-card" aria-label="账户概览">
        <div class="pc-hero-strip"></div>
        <div class="pc-hero-inner">
            <div class="pc-avatar-xl" aria-hidden="true"><%= avLetter %></div>
            <div class="pc-hero-info">
                <h2 class="pc-name"><%= dispName %></h2>
                <p class="pc-subline"><span class="pc-email"><%= emailDisp %></span><span class="pc-dot">·</span><span>学号 <%= sidDisp %></span></p>
                <% if (skillList != null && !skillList.isEmpty()) { %>
                <div class="pc-tags">
                    <% for (String s : skillList) {
                        if (s == null || s.trim().isEmpty()) continue;
                    %><span class="pc-tag"><%= s.trim() %></span><% } %>
                </div>
                <% } %>
                <div class="pc-progress-block">
                    <div class="pc-progress-meta">
                        <span>资料完整度</span>
                        <strong><%= pct %>%</strong>
                    </div>
                    <div class="pc-progress-track" role="progressbar" aria-valuenow="<%= pct %>" aria-valuemin="0" aria-valuemax="100">
                        <div class="pc-progress-fill" style="width:<%= pct %>%"></div>
                    </div>
                    <p class="pc-progress-tip">完善资料有助于岗位匹配；简历与投递请在「工作台」操作。</p>
                </div>
            </div>
        </div>
    </section>

    <section id="pc-quick" class="pc-card" aria-label="常用功能">
        <div class="pc-card-hd">
            <h2>常用功能</h2>
            <span class="pc-muted">与招聘平台类似的快捷入口，跳转至工作台对应模块</span>
        </div>
        <div class="pc-card-bd pc-quick-wrap">
            <a href="<%= ctx %>/ta/dashboard?tab=jobs" class="pc-tile">
                <span class="pc-tile-ic" aria-hidden="true">🔍</span>
                <span class="pc-tile-t">找岗位</span>
                <span class="pc-tile-d">浏览开放中的助教岗位</span>
            </a>
            <a href="<%= ctx %>/ta/dashboard?tab=applications" class="pc-tile">
                <span class="pc-tile-ic" aria-hidden="true">📨</span>
                <span class="pc-tile-t">我的申请</span>
                <span class="pc-tile-d">查看投递与审核状态</span>
            </a>
            <a href="<%= ctx %>/ta/dashboard?tab=resume" class="pc-tile">
                <span class="pc-tile-ic" aria-hidden="true">📄</span>
                <span class="pc-tile-t">我的简历</span>
                <span class="pc-tile-d">当前：<%= resumeLabel %></span>
            </a>
            <a href="<%= ctx %>/ta/dashboard?tab=messages" class="pc-tile">
                <span class="pc-tile-ic" aria-hidden="true">💬</span>
                <span class="pc-tile-t">私信</span>
                <span class="pc-tile-d">与招聘者沟通（已投递或好友）</span>
            </a>
        </div>
    </section>
</div>
<style>
  .pc-profile-stack{display:flex;flex-direction:column;gap:1.25rem}
  .pc-hero-card{overflow:hidden;padding:0}
  .pc-hero-strip{height:4px;background:linear-gradient(90deg,#1d4ed8,#3b82f6,#60a5fa)}
  .pc-hero-inner{display:flex;gap:1.25rem;padding:1.35rem 1.35rem 1.5rem;flex-wrap:wrap;align-items:flex-start}
  .pc-avatar-xl{width:88px;height:88px;border-radius:50%;background:linear-gradient(145deg,#3b82f6,#1d4ed8);color:#fff;font-size:2rem;font-weight:700;display:flex;align-items:center;justify-content:center;flex-shrink:0;box-shadow:0 8px 24px rgba(37,99,235,.28)}
  .pc-hero-info{flex:1;min-width:0}
  .pc-name{margin:0 0 .35rem;font-size:1.35rem;font-weight:600;letter-spacing:-.02em;color:#1f2329}
  .pc-subline{margin:0 0 .65rem;font-size:.88rem;color:#646a73}
  .pc-dot{opacity:.45;margin:0 .35rem}
  .pc-tags{display:flex;flex-wrap:wrap;gap:.4rem;margin-bottom:.85rem}
  .pc-tag{font-size:.78rem;padding:.2rem .55rem;background:#f0f5ff;color:#1d4ed8;border-radius:999px;font-weight:500}
  .pc-progress-block{margin-top:.15rem}
  .pc-progress-meta{display:flex;justify-content:space-between;align-items:center;font-size:.82rem;color:#646a73;margin-bottom:.35rem}
  .pc-progress-meta strong{color:#1d4ed8;font-size:.95rem}
  .pc-progress-track{height:8px;background:#eef0f3;border-radius:999px;overflow:hidden}
  .pc-progress-fill{height:100%;background:linear-gradient(90deg,#2563eb,#60a5fa);border-radius:999px;transition:width .35s ease}
  .pc-progress-tip{margin:.5rem 0 0;font-size:.78rem;color:#8f959e;line-height:1.45}
  .pc-quick-wrap{display:grid;grid-template-columns:repeat(auto-fill,minmax(200px,1fr));gap:1rem}
  .pc-tile{display:flex;flex-direction:column;align-items:flex-start;gap:.25rem;padding:1rem 1.1rem;border:1px solid #e8eaed;border-radius:10px;text-decoration:none;color:inherit;transition:border-color .15s,box-shadow .15s,transform .12s;background:#fafbfc}
  .pc-tile:hover{border-color:#bfdbfe;box-shadow:0 4px 14px rgba(37,99,235,.1);transform:translateY(-2px);background:#fff}
  .pc-tile-ic{font-size:1.35rem;line-height:1;margin-bottom:.15rem}
  .pc-tile-t{font-size:.95rem;font-weight:600;color:#1f2329}
  .pc-tile-d{font-size:.8rem;color:#8f959e;line-height:1.4}
</style>
