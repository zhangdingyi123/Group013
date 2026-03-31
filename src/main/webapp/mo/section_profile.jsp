<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.bupt.ta.model.Job" %>
<%@ page import="com.bupt.ta.model.ModuleOrganiser" %>
<%@ page import="java.util.List" %>
<%
    ModuleOrganiser mo = (ModuleOrganiser) request.getAttribute("mo");
    if (mo == null) {
        String c = request.getContextPath();
        if (session.getAttribute("moUser") != null) {
            response.sendRedirect(c + "/mo/profile");
        } else {
            response.sendRedirect(c + "/mo/auth");
        }
        return;
    }
    @SuppressWarnings("unchecked")
    List<Job> myJobs = (List<Job>) request.getAttribute("myJobs");
    if (myJobs == null) myJobs = java.util.Collections.emptyList();
    int jobCount = myJobs.size();
    int openCount = 0;
    for (Job j : myJobs) {
        if (j != null && Job.STATUS_OPEN.equals(j.getStatus())) openCount++;
    }
    String ctx = request.getContextPath();
    String dispName = mo.getName() != null && !mo.getName().trim().isEmpty()
            ? mo.getName().trim() : "组织者";
    String avLetter = dispName.substring(0, 1);
    String emailDisp = mo.getEmail() != null && !mo.getEmail().isEmpty() ? mo.getEmail() : "—";
    String deptDisp = mo.getDepartment() != null && !mo.getDepartment().trim().isEmpty()
            ? mo.getDepartment().trim() : "未填写";
%>
<div class="profile-center-wrap">
    <div class="profile-hero" role="region" aria-label="账户概览">
        <div class="profile-avatar" aria-hidden="true"><%= avLetter %></div>
        <div class="profile-hero-main">
            <p class="profile-title"><%= dispName %></p>
            <p class="profile-meta">
                <span><%= emailDisp %></span>
                <span class="sep">·</span>
                <span><%= deptDisp %></span>
            </p>
        </div>
    </div>

    <div class="profile-stats" aria-label="数据概览">
        <div class="profile-stat">
            <div class="profile-stat-value"><%= jobCount %></div>
            <div class="profile-stat-label">已发布岗位</div>
        </div>
        <div class="profile-stat">
            <div class="profile-stat-value"><%= openCount %></div>
            <div class="profile-stat-label">招聘中</div>
        </div>
    </div>

    <div class="section profile-edit">
        <h2>编辑资料</h2>
        <p class="section-desc">更新显示名称与院系/课程组。登录邮箱仅用于身份识别，不可在此修改。</p>
        <form method="post" action="<%= ctx %>/mo/profile">
            <input type="hidden" name="action" value="updateProfile">
            <div class="form-group">
                <label for="mo-pf-name">姓名</label>
                <input id="mo-pf-name" type="text" name="name" required value="<%= mo.getName() != null ? mo.getName() : "" %>">
            </div>
            <div class="form-group">
                <label for="mo-pf-email">登录邮箱</label>
                <input id="mo-pf-email" type="email" class="field-ro" value="<%= mo.getEmail() != null ? mo.getEmail() : "" %>" readonly aria-readonly="true">
            </div>
            <div class="form-group">
                <label for="mo-pf-dept">院系 / 课程组</label>
                <input id="mo-pf-dept" type="text" name="department" placeholder="例如：计算机学院" value="<%= mo.getDepartment() != null ? mo.getDepartment() : "" %>">
            </div>
            <button type="submit" class="btn btn-primary">保存资料</button>
        </form>
    </div>
</div>
