<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String ctx = request.getContextPath();
%>
<div class="section">
    <h2>发布新岗位</h2>
    <form method="post" action="<%= ctx %>/mo/dashboard">
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
