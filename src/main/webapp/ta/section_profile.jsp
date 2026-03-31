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

    List<?> myApps = (List<?>) request.getAttribute("myApplications");
    int appCount = myApps != null ? myApps.size() : 0;
    int skCount = applicant.getSkills() != null ? applicant.getSkills().size() : 0;
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
%>
<div class="profile-center-wrap">
    <div class="profile-hero" role="region" aria-label="账户概览">
        <div class="profile-avatar" aria-hidden="true"><%= avLetter %></div>
        <div class="profile-hero-main">
            <p class="profile-title"><%= dispName %></p>
            <p class="profile-meta">
                <span><%= emailDisp %></span>
                <span class="sep">·</span>
                <span>学号 <%= sidDisp %></span>
            </p>
        </div>
    </div>

    <div class="profile-stats" aria-label="数据概览">
        <div class="profile-stat">
            <div class="profile-stat-value"><%= appCount %></div>
            <div class="profile-stat-label">我的申请</div>
        </div>
        <div class="profile-stat">
            <div class="profile-stat-value"><%= skCount %></div>
            <div class="profile-stat-label">技能标签</div>
        </div>
        <div class="profile-stat">
            <div class="profile-stat-value" style="font-size:1.15rem;padding-top:.2rem"><%= resumeLabel %></div>
            <div class="profile-stat-label">简历状态</div>
        </div>
    </div>

</div>
