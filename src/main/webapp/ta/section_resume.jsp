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
    Boolean resumeIsText = (Boolean) request.getAttribute("resumeIsText");
    String resumeFilename = (String) request.getAttribute("resumeFilename");
    String resumeContent = (String) request.getAttribute("resumeContent");
    if (resumeContent == null) resumeContent = "";
    @SuppressWarnings("unchecked")
    List<String> resumeSkillGaps = (List<String>) request.getAttribute("resumeSkillGaps");
    if (resumeSkillGaps == null) resumeSkillGaps = java.util.Collections.emptyList();
    @SuppressWarnings("unchecked")
    List<String> resumeStrengths = (List<String>) request.getAttribute("resumeStrengths");
    if (resumeStrengths == null) resumeStrengths = java.util.Collections.emptyList();
    String ctx = request.getContextPath();
%>
<div class="section">
    <h2>简历</h2>
    <% if (applicant.getResumePath() != null && !applicant.getResumePath().isEmpty()) { %>
    <p class="section-desc">当前文件：<strong><%= resumeFilename != null ? resumeFilename : applicant.getResumePath() %></strong></p>
    <% } else { %>
    <p class="section-desc">上传文件（.txt / .pdf / .doc / .docx）或粘贴纯文本简历，便于岗位匹配提示。</p>
    <% } %>

    <h3 style="font-size:.95rem;margin:1rem 0 .5rem;color:#475569">上传或更新文件</h3>
    <form method="post" action="<%= ctx %>/ta/dashboard/upload-resume" enctype="multipart/form-data">
        <div class="form-group">
            <label>选择文件</label>
            <input type="file" name="resumeFile" accept=".txt,.pdf,.doc,.docx">
        </div>
        <button type="submit" class="btn btn-primary">上传</button>
    </form>

    <% if (Boolean.TRUE.equals(resumeIsText)) { %>
    <h3 style="font-size:.95rem;margin:1.25rem 0 .5rem;color:#475569">编辑纯文本简历</h3>
    <form method="post" action="<%= ctx %>/ta/dashboard">
        <input type="hidden" name="action" value="resume">
        <div class="form-group">
            <label>内容</label>
            <textarea name="resumeContent" rows="8"><%= resumeContent %></textarea>
        </div>
        <button type="submit" class="btn btn-primary">保存文本简历</button>
    </form>
    <% } else if (applicant.getResumePath() == null || applicant.getResumePath().isEmpty()) { %>
    <h3 style="font-size:.95rem;margin:1.25rem 0 .5rem;color:#475569">或粘贴纯文本</h3>
    <form method="post" action="<%= ctx %>/ta/dashboard">
        <input type="hidden" name="action" value="resume">
        <div class="form-group">
            <label>简历正文</label>
            <textarea name="resumeContent" rows="8" placeholder="粘贴简历文字…"></textarea>
        </div>
        <button type="submit" class="btn btn-secondary">保存为文本简历</button>
    </form>
    <% } %>

    <% if (!resumeStrengths.isEmpty() || !resumeSkillGaps.isEmpty()) { %>
    <div style="margin-top:1.25rem">
        <% if (!resumeStrengths.isEmpty()) { %>
        <p style="margin:0 0 .35rem;font-size:.9rem;font-weight:600;color:#065f46">与开放岗位匹配的优势</p>
        <ul class="hint-list">
            <% for (String s : resumeStrengths) { %><li><%= s %></li><% } %>
        </ul>
        <% } %>
        <% if (!resumeSkillGaps.isEmpty()) { %>
        <p style="margin:1rem 0 .35rem;font-size:.9rem;font-weight:600;color:#92400e">可加强的技能方向</p>
        <ul class="hint-list">
            <% for (String s : resumeSkillGaps) { %><li><%= s %></li><% } %>
        </ul>
        <% } %>
    </div>
    <% } %>
</div>
