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
<section id="pc-edit" class="section profile-edit pc-card">
    <div class="pc-card-hd">
        <h2>账号资料</h2>
        <span class="pc-muted">基本信息将展示给招聘方，与主流招聘站「我的简历-基本信息」类似</span>
    </div>
    <div class="pc-card-bd">
        <form method="post" action="<%= ctx %>/ta/profile">
            <input type="hidden" name="action" value="updateProfile">
            <div class="form-group">
                <label for="pf-name">姓名</label>
                <input id="pf-name" type="text" name="name" required autocomplete="name" value="<%= applicant.getName() != null ? applicant.getName() : "" %>">
            </div>
            <div class="form-group">
                <label for="pf-sid">学号</label>
                <input id="pf-sid" type="text" name="studentId" autocomplete="off" value="<%= applicant.getStudentId() != null ? applicant.getStudentId() : "" %>">
            </div>
            <div class="form-group">
                <label for="pf-skills">技能标签（逗号分隔）</label>
                <input id="pf-skills" type="text" name="skills" placeholder="例如：Java, Python, 监考" value="<%= skillsJoined %>">
            </div>
            <button type="submit" class="btn btn-primary">保存</button>
        </form>
    </div>
</section>
