<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="com.bupt.ta.model.Job" %>
<%@ page import="java.util.List" %>
<%@ page import="com.bupt.ta.util.I18n" %>
<%
    Job job = (Job) request.getAttribute("moEditJob");
    if (job == null) {
        return;
    }
    String ctx = request.getContextPath();
    String skillsJoin = "";
    List<String> rs = job.getRequiredSkills();
    if (rs != null && !rs.isEmpty()) {
        skillsJoin = String.join(", ", rs);
    }
    request.setAttribute("moEditSkillsJoin", skillsJoin);
    String jt = job.getType() != null ? job.getType() : "course_ta";
    request.setAttribute("moEditJobType", jt);
%>
<div class="section">
    <p class="section-desc" style="margin-top:0"><a href="<%= ctx %>/mo/dashboard?tab=positions" class="back-link"><%= I18n.msg(request, "mo.edit.back") %></a></p>
    <h2><%= I18n.msg(request, "mo.edit.title") %></h2>
    <p class="section-desc"><%= I18n.msg(request, "mo.edit.lead") %> <%= I18n.msg(request, "mo.post.englishOnlyHint") %></p>
    <form method="post" action="<%= ctx %>/mo/dashboard">
        <input type="hidden" name="action" value="updateJob">
        <input type="hidden" name="jobId" value="<c:out value="${moEditJob.id}"/>">
        <div class="form-group">
            <label><%= I18n.msg(request, "mo.post.label.title") %></label>
            <input type="text" name="title" required value="<c:out value="${moEditJob.title}"/>">
        </div>
        <div class="form-group">
            <label><%= I18n.msg(request, "mo.post.label.desc") %></label>
            <textarea name="description" rows="6" placeholder="<%= I18n.msg(request, "mo.post.ph.desc") %>"><c:out value="${moEditJob.description}"/></textarea>
        </div>
        <div class="form-group">
            <label><%= I18n.msg(request, "mo.post.label.type") %></label>
            <select name="type">
                <option value="course_ta" ${moEditJobType == 'course_ta' ? 'selected' : ''}><%= I18n.msg(request, "jobs.type.course_ta") %></option>
                <option value="invigilation" ${moEditJobType == 'invigilation' ? 'selected' : ''}><%= I18n.msg(request, "jobs.type.invigilation") %></option>
                <option value="activity" ${moEditJobType == 'activity' ? 'selected' : ''}><%= I18n.msg(request, "jobs.type.activity") %></option>
            </select>
        </div>
        <div class="form-group">
            <label><%= I18n.msg(request, "mo.post.label.skills") %></label>
            <input type="text" name="requiredSkills" placeholder="<%= I18n.msg(request, "mo.post.ph.skills") %>" value="<c:out value="${moEditSkillsJoin}"/>">
        </div>
        <button type="submit" class="btn btn-primary"><%= I18n.msg(request, "mo.edit.submit") %></button>
    </form>
</div>
