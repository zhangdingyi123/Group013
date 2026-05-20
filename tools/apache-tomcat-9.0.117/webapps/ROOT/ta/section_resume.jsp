<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.bupt.ta.util.I18n" %>
<%@ page import="com.bupt.ta.model.Applicant" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.LinkedHashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.bupt.ta.model.Job" %>
<%@ page import="com.google.gson.Gson" %>
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
    Map<String, String> resumeI18nJs = new LinkedHashMap<String, String>();
    resumeI18nJs.put("errRequestFailed", I18n.msg(request, "asst.js.err.request.failed"));
    resumeI18nJs.put("errQuota", I18n.msg(request, "asst.js.err.quota"));
    resumeI18nJs.put("errLogin", I18n.msg(request, "asst.js.err.login"));
    resumeI18nJs.put("errNoSaved", I18n.msg(request, "asst.js.err.noSaved"));
    resumeI18nJs.put("networkChat", I18n.msg(request, "asst.js.network.chat"));
    resumeI18nJs.put("roleUser", I18n.msg(request, "asst.js.role.user"));
    resumeI18nJs.put("roleAssistant", I18n.msg(request, "asst.js.role.assistant"));
    String resumeI18nJson = new Gson().toJson(resumeI18nJs);
    if (resumeSkillGaps == null) resumeSkillGaps = java.util.Collections.emptyList();
    @SuppressWarnings("unchecked")
    List<String> resumeStrengths = (List<String>) request.getAttribute("resumeStrengths");
    if (resumeStrengths == null) resumeStrengths = java.util.Collections.emptyList();
    String ctx = request.getContextPath();
    Boolean kimiOk = (Boolean) request.getAttribute("assistantKimiConfigured");
    Boolean qwenOk = (Boolean) request.getAttribute("assistantQwenConfigured");
    Boolean openaiOk = (Boolean) request.getAttribute("assistantOpenaiConfigured");
    String defProv = (String) request.getAttribute("assistantDefaultProvider");
    if (defProv == null) defProv = "kimi";
    boolean hasKimi = kimiOk != null && kimiOk;
    boolean hasQwen = qwenOk != null && qwenOk;
    boolean hasOpenai = openaiOk != null && openaiOk;
    boolean noAssistantKeys = !hasKimi && !hasQwen && !hasOpenai;
    boolean disableKimiOption = !hasKimi && (hasQwen || hasOpenai);
    boolean disableQwenOption = !hasQwen && (hasKimi || hasOpenai);
    boolean disableOpenaiOption = !hasOpenai && (hasKimi || hasQwen);
    Boolean canAiResume = (Boolean) request.getAttribute("assistantSavedResumeTxt");
    boolean canUseAiResume = canAiResume != null && canAiResume;
    @SuppressWarnings("unchecked")
    java.util.List<com.bupt.ta.web.TADashboardServlet.ResumeVersionRow> resumeVersions = (java.util.List<com.bupt.ta.web.TADashboardServlet.ResumeVersionRow>) request.getAttribute("resumeVersions");
    if (resumeVersions == null) resumeVersions = java.util.Collections.emptyList();
    @SuppressWarnings("unchecked")
    java.util.List<Job> recommendedJobs = (java.util.List<Job>) request.getAttribute("recommendedJobs");
    if (recommendedJobs == null) recommendedJobs = java.util.Collections.emptyList();
%>
<div class="section">
    <h2><%= I18n.msg(request, "resume.title") %></h2>
    <% if (applicant.getResumePath() != null && !applicant.getResumePath().isEmpty()) { %>
    <p class="section-desc"><%= I18n.msg(request, "resume.currentFile") %><strong><%= resumeFilename != null ? resumeFilename : applicant.getResumePath() %></strong></p>
    <% } else { %>
    <p class="section-desc"><%= I18n.msg(request, "resume.desc.upload") %></p>
    <% } %>

    <h3 style="font-size:.95rem;margin:1rem 0 .5rem;color:#475569"><%= I18n.msg(request, "resume.h3.upload") %></h3>
    <form method="post" action="<%= ctx %>/ta/dashboard/upload-resume" enctype="multipart/form-data">
        <div class="form-group">
            <label><%= I18n.msg(request, "resume.label.choose") %></label>
            <input type="file" name="resumeFile" accept=".txt,.pdf,.doc,.docx">
        </div>
        <button type="submit" class="btn btn-primary"><%= I18n.msg(request, "common.upload") %></button>
    </form>

    <% if (Boolean.TRUE.equals(resumeIsText)) { %>
    <h3 style="font-size:.95rem;margin:1.25rem 0 .5rem;color:#475569"><%= I18n.msg(request, "resume.editText") %></h3>
    <form method="post" action="<%= ctx %>/ta/dashboard">
        <input type="hidden" name="action" value="resume">
        <div class="form-group">
            <label><%= I18n.msg(request, "resume.label.content") %></label>
            <textarea name="resumeContent" rows="8"><%= resumeContent %></textarea>
        </div>
        <button type="submit" class="btn btn-primary"><%= I18n.msg(request, "resume.saveText") %></button>
    </form>
    <% } else if (applicant.getResumePath() == null || applicant.getResumePath().isEmpty()) { %>
    <h3 style="font-size:.95rem;margin:1.25rem 0 .5rem;color:#475569"><%= I18n.msg(request, "resume.orPaste") %></h3>
    <form method="post" action="<%= ctx %>/ta/dashboard">
        <input type="hidden" name="action" value="resume">
        <div class="form-group">
            <label><%= I18n.msg(request, "resume.label.body") %></label>
            <textarea name="resumeContent" rows="8" placeholder="<%= I18n.msg(request, "resume.paste.ph") %>"></textarea>
        </div>
        <button type="submit" class="btn btn-secondary"><%= I18n.msg(request, "resume.saveAsText") %></button>
    </form>
    <% } %>

    <div class="resume-version-panel">
        <h3 style="font-size:.95rem;margin:1.25rem 0 .5rem;color:#475569"><%= I18n.msg(request, "resume.version.title") %></h3>
        <p class="section-desc"><%= I18n.msg(request, "resume.version.desc") %></p>
        <% if (resumeVersions.isEmpty()) { %>
            <p class="empty-hint"><%= I18n.msg(request, "resume.version.noHistory") %></p>
        <% } else { %>
            <ul class="version-list">
                <% for (com.bupt.ta.web.TADashboardServlet.ResumeVersionRow version : resumeVersions) {
                    boolean isCurrent = version.getPath().equals(applicant.getResumePath());
                %>
                <li>
                    <span class="version-name"><%= version.getName() %></span>
                    <span class="version-meta"><%= version.getUpdatedAtText() %><%= isCurrent ? " · " + I18n.msg(request, "resume.version.current") : "" %></span>
                    <% if (!isCurrent) { %>
                    <form method="post" action="<%= ctx %>/ta/dashboard" class="version-form">
                        <input type="hidden" name="action" value="restoreResumeVersion">
                        <input type="hidden" name="versionPath" value="<%= version.getPath() %>">
                        <button type="submit" class="btn btn-secondary btn-small"><%= I18n.msg(request, "resume.version.restore") %></button>
                    </form>
                    <% } %>
                </li>
                <% } %>
            </ul>
        <% } %>
    </div>

    <% if (!recommendedJobs.isEmpty()) { %>
    <div class="recommendation-panel">
        <h3 style="font-size:.95rem;margin:1.25rem 0 .5rem;color:#475569"><%= I18n.msg(request, "resume.recommended.title") %></h3>
        <p class="section-desc"><%= I18n.msg(request, "resume.recommended.desc") %></p>
        <div class="recommendation-list">
            <% for (Job rec : recommendedJobs) {
                Integer recScore = (Integer) request.getAttribute("jobMatchScores") != null ? ((java.util.Map<String,Integer>)request.getAttribute("jobMatchScores")).get(rec.getId()) : null;
            %>
            <a class="recommendation-item" href="<%= ctx %>/ta/dashboard?tab=jobs&jobQ=<%= java.net.URLEncoder.encode(rec.getTitle() != null ? rec.getTitle() : "", java.nio.charset.StandardCharsets.UTF_8) %>">
                <strong><%= rec.getTitle() != null ? rec.getTitle() : I18n.msg(request, "jobs.unnamed") %></strong>
                <span><%= I18n.msg(request, "jobs.match", recScore != null ? I18n.msg(request, "jobs.score.points", recScore) : I18n.msg(request, "common.dash")) %></span>
            </a>
            <% } %>
        </div>
    </div>
    <% } %>

    <% if (!resumeStrengths.isEmpty() || !resumeSkillGaps.isEmpty()) { %>
    <div style="margin-top:1.25rem">
        <% if (!resumeStrengths.isEmpty()) { %>
        <p style="margin:0 0 .35rem;font-size:.9rem;font-weight:600;color:#065f46"><%= I18n.msg(request, "resume.strengths") %></p>
        <ul class="hint-list">
            <% for (String s : resumeStrengths) { %><li><%= s %></li><% } %>
        </ul>
        <% } %>
        <% if (!resumeSkillGaps.isEmpty()) { %>
        <p style="margin:1rem 0 .35rem;font-size:.9rem;font-weight:600;color:#92400e"><%= I18n.msg(request, "resume.gaps") %></p>
        <ul class="hint-list">
            <% for (String s : resumeSkillGaps) { %><li><%= s %></li><% } %>
        </ul>
        <% } %>
    </div>
    <% } %>
</div>

<div class="section resume-ai-section">
    <h2><%= I18n.msg(request, "resume.ai.title") %></h2>
    <p class="section-desc"><%= I18n.msg(request, "resume.ai.desc") %></p>
    <% if (noAssistantKeys) { %>
    <p class="resume-ai-warn"><%= I18n.msg(request, "resume.ai.warn.keys") %></p>
    <% } else if (!canUseAiResume) { %>
    <p class="resume-ai-warn"><%= I18n.msg(request, "resume.ai.warn.noresume") %></p>
    <p class="resume-ai-hint"><%= I18n.msg(request, "resume.ai.hint", ctx + "/assistant") %></p>
    <% } else { %>
    <div class="resume-ai-toolbar">
        <div>
            <label for="resumeAiProvider"><%= I18n.msg(request, "resume.ai.model") %></label>
            <select id="resumeAiProvider" name="resumeAiProvider">
                <option value="kimi" <%= "kimi".equals(defProv) ? "selected" : "" %> <%= disableKimiOption ? "disabled" : "" %>>Kimi K2.5</option>
                <option value="qwen" <%= "qwen".equals(defProv) ? "selected" : "" %> <%= disableQwenOption ? "disabled" : "" %>>Qwen</option>
                <option value="openai" <%= "openai".equals(defProv) ? "selected" : "" %> <%= disableOpenaiOption ? "disabled" : "" %>>OpenAI</option>
            </select>
        </div>
    </div>
    <div id="resumeAiChat" class="resume-ai-chat" aria-live="polite"></div>
    <div class="form-group">
        <label for="resumeAiInput"><%= I18n.msg(request, "resume.ai.ask") %></label>
        <textarea id="resumeAiInput" rows="3" placeholder="<%= I18n.msg(request, "resume.ai.ask.ph") %>"></textarea>
    </div>
    <button type="button" id="resumeAiSend" class="btn btn-primary"><%= I18n.msg(request, "resume.ai.send") %></button>
    <button type="button" id="resumeAiClear" class="btn btn-secondary"><%= I18n.msg(request, "resume.ai.clear") %></button>
    <p class="resume-ai-hint"><%= I18n.msg(request, "resume.ai.security") %></p>
    <script>
    (function () {
        var RESUME_I18N = <%= resumeI18nJson %>;
        var apiUrl = '<%= ctx %>/api/assistant/chat';
        var messages = [];
        var logEl = document.getElementById('resumeAiChat');
        var input = document.getElementById('resumeAiInput');
        var providerSel = document.getElementById('resumeAiProvider');
        var btn = document.getElementById('resumeAiSend');
        var btnClear = document.getElementById('resumeAiClear');
	        function mapChatError(msg, data) {
	            if (!msg) return RESUME_I18N.errRequestFailed;
	            if (data && data.code === 'ASSISTANT_QUOTA_EXCEEDED') {
	                return RESUME_I18N.errQuota;
	            }
	            if (msg.indexOf('HTTP 429') === 0 || /quota|balance|余额|欠费/i.test(msg)) {
	                return RESUME_I18N.errQuota;
	            }
	            if (msg === 'login required for saved resume') return RESUME_I18N.errLogin;
	            if (msg === 'no saved resume') return RESUME_I18N.errNoSaved;
	            return msg;
	        }
        function append(role, text) {
            var div = document.createElement('div');
            div.className = 'resume-ai-msg resume-ai-msg--' + (role === 'user' ? 'user' : 'asst');
            var lab = document.createElement('span');
            lab.className = 'resume-ai-msg-label';
            lab.textContent = role === 'user' ? RESUME_I18N.roleUser : RESUME_I18N.roleAssistant;
            var body = document.createElement('div');
            body.textContent = text;
            div.appendChild(lab);
            div.appendChild(body);
            logEl.appendChild(div);
            logEl.scrollTop = logEl.scrollHeight;
        }
        function appendErr(text) {
            var div = document.createElement('div');
            div.className = 'resume-ai-msg resume-ai-msg--err';
            div.textContent = text;
            logEl.appendChild(div);
            logEl.scrollTop = logEl.scrollHeight;
        }
        btn.addEventListener('click', function () {
            var text = (input.value || '').trim();
            if (!text) return;
            append('user', text);
            messages.push({ role: 'user', content: text });
            input.value = '';
            btn.disabled = true;
            var payload = {
                provider: providerSel.value,
                messages: messages,
                useSavedResume: true
            };
            fetch(apiUrl, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json;charset=UTF-8' },
                credentials: 'same-origin',
                body: JSON.stringify(payload)
            }).then(function (r) {
                return r.json().then(function (data) {
                    return { ok: r.ok, status: r.status, data: data };
                });
	            }).then(function (res) {
	                if (res.data && res.data.reply) {
	                    append('assistant', res.data.reply);
	                    messages.push({ role: 'assistant', content: res.data.reply });
	                } else {
	                    var raw = (res.data && res.data.error) ? res.data.error : (RESUME_I18N.errRequestFailed + ' (' + res.status + ')');
	                    appendErr(mapChatError(raw, res.data));
	                }
	            }).catch(function () {
	                appendErr(RESUME_I18N.networkChat);
	            }).finally(function () {
                btn.disabled = false;
            });
        });
        btnClear.addEventListener('click', function () {
            messages = [];
            logEl.innerHTML = '';
            input.value = '';
        });
    })();
    </script>
    <style>
    .resume-version-panel { margin-top: 1.5rem; padding: 1rem 0 0; border-top: 1px solid #e5e7eb; }
    .resume-version-panel .version-list { list-style:none; padding:0; margin:0; }
    .resume-version-panel .version-list li { display:flex; flex-wrap:wrap; align-items:center; justify-content:space-between; gap:0.65rem; padding:.8rem 0; border-bottom:1px solid #f1f5f9; }
    .resume-version-panel .version-name { font-weight:600; }
    .resume-version-panel .version-meta { color:#6b7280; font-size:.88rem; }
    .resume-version-panel .version-form { margin:0; }
    .recommendation-panel { margin-top: 1.5rem; padding: 1rem 0 0; border-top: 1px solid #e5e7eb; }
    .recommendation-list { display:grid; gap:.75rem; }
    .recommendation-item { display:block; padding:1rem; border:1px solid #e2e8f0; border-radius:.9rem; text-decoration:none; color:inherit; background:#fff; transition: transform .15s ease, border-color .15s ease; }
    .recommendation-item:hover { transform: translateY(-1px); border-color:#60a5fa; }
    .recommendation-item strong { display:block; margin-bottom:.35rem; font-size:.96rem; }
    .recommendation-item span { color:#475569; font-size:.9rem; }
    </style>
    <% } %>
</div>
