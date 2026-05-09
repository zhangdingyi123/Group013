<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.bupt.ta.util.I18n" %>
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
        <h2><%= I18n.msg(request, "profile.edit.title") %></h2>
        <span class="pc-muted"><%= I18n.msg(request, "profile.edit.sub") %></span>
    </div>
    <div class="pc-card-bd">
        <form method="post" action="<%= ctx %>/ta/profile">
            <input type="hidden" name="action" value="updateProfile">
            <div class="form-group">
                <label for="pf-name"><%= I18n.msg(request, "profile.edit.name") %></label>
                <input id="pf-name" type="text" name="name" required autocomplete="name" value="<%= applicant.getName() != null ? applicant.getName() : "" %>">
            </div>
            <div class="form-group">
                <label for="pf-sid"><%= I18n.msg(request, "profile.edit.sid") %></label>
                <input id="pf-sid" type="text" name="studentId" autocomplete="off" value="<%= applicant.getStudentId() != null ? applicant.getStudentId() : "" %>">
            </div>
            <div class="form-group">
                <label for="pf-phone"><%= I18n.msg(request, "profile.edit.phone") %></label>
                <input id="pf-phone" type="tel" name="phone" inputmode="tel" autocomplete="tel" placeholder="<%= I18n.msg(request, "profile.edit.phone.ph") %>" value="<%= applicant.getPhone() != null ? applicant.getPhone() : "" %>">
            </div>
            <div class="form-group">
                <label for="pf-skills"><%= I18n.msg(request, "profile.edit.skills") %></label>
                <input id="pf-skills" type="text" name="skills" placeholder="<%= I18n.msg(request, "profile.edit.skills.ph") %>" value="<%= skillsJoined %>">
            </div>
            <button type="submit" class="btn btn-primary"><%= I18n.msg(request, "common.save") %></button>
        </form>
    </div>
</section>
