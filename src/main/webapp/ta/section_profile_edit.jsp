<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.bupt.ta.model.Applicant" %>
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
%>
<div class="section profile-edit">
    <h2>编辑资料</h2>
    <p class="section-desc">维护姓名、学号与技能标签，用于岗位匹配与组织者查看。</p>
    <form method="post" action="<%= ctx %>/ta/profile">
        <input type="hidden" name="action" value="updateProfile">
        <div class="form-group">
            <label for="pf-name">姓名</label>
            <input id="pf-name" type="text" name="name" required value="<%= applicant.getName() != null ? applicant.getName() : "" %>">
        </div>
        <div class="form-group">
            <label for="pf-sid">学号</label>
            <input id="pf-sid" type="text" name="studentId" value="<%= applicant.getStudentId() != null ? applicant.getStudentId() : "" %>">
        </div>
        <div class="form-group">
            <label for="pf-skills">技能（逗号分隔）</label>
            <input id="pf-skills" type="text" name="skills" placeholder="Java, Python, 监考" value="<%= skillsJoined %>">
        </div>
        <button type="submit" class="btn btn-primary">保存资料</button>
    </form>
</div>
