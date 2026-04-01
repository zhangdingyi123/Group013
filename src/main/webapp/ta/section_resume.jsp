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

<div class="section resume-ai-section">
    <h2>AI 修改简历</h2>
    <p class="section-desc">根据当前<strong>已保存的简历文件</strong>向智能小助手提问，可请其润色表述、调整结构或提出改进建议（不会自动覆盖你的文件，满意后可自行粘贴到上方文本框保存）。</p>
    <% if (noAssistantKeys) { %>
    <p class="resume-ai-warn">服务端未配置大模型 API 密钥，无法使用本功能。请在 <code>assistant.properties</code> 或环境变量 <code>KIMI_API_KEY</code> / <code>QWEN_API_KEY</code> / <code>OPENAI_API_KEY</code> 中配置后重启应用。</p>
    <% } else if (!canUseAiResume) { %>
    <p class="resume-ai-warn">请先在上方上传或保存一份简历（支持 .txt / .pdf / .doc / .docx），保存成功后再使用 AI 辅助。</p>
    <p class="resume-ai-hint">也可前往 <a href="<%= ctx %>/assistant">智能小助手</a> 页面，粘贴简历正文进行对话。</p>
    <% } else { %>
    <div class="resume-ai-toolbar">
        <div>
            <label for="resumeAiProvider">模型</label>
            <select id="resumeAiProvider" name="resumeAiProvider">
                <option value="kimi" <%= "kimi".equals(defProv) ? "selected" : "" %> <%= disableKimiOption ? "disabled" : "" %>>Kimi K2.5</option>
                <option value="qwen" <%= "qwen".equals(defProv) ? "selected" : "" %> <%= disableQwenOption ? "disabled" : "" %>>通义千问</option>
                <option value="openai" <%= "openai".equals(defProv) ? "selected" : "" %> <%= disableOpenaiOption ? "disabled" : "" %>>OpenAI</option>
            </select>
        </div>
    </div>
    <div id="resumeAiChat" class="resume-ai-chat" aria-live="polite"></div>
    <div class="form-group">
        <label for="resumeAiInput">你想怎么改？</label>
        <textarea id="resumeAiInput" rows="3" placeholder="例如：把项目经历写得更突出；帮我压缩到一页纸的要点；语气更正式一些…"></textarea>
    </div>
    <button type="button" id="resumeAiSend" class="btn btn-primary">发送给 AI</button>
    <button type="button" id="resumeAiClear" class="btn btn-secondary">清空对话</button>
    <p class="resume-ai-hint">对话会附带你的站内简历正文；请勿在提问中提交敏感密码等信息。</p>
    <script>
    (function () {
        var apiUrl = '<%= ctx %>/api/assistant/chat';
        var messages = [];
        var logEl = document.getElementById('resumeAiChat');
        var input = document.getElementById('resumeAiInput');
        var providerSel = document.getElementById('resumeAiProvider');
        var btn = document.getElementById('resumeAiSend');
        var btnClear = document.getElementById('resumeAiClear');
        function mapChatError(msg) {
            if (!msg) return '请求失败';
            if (msg.indexOf('HTTP 429') === 0 || /quota|balance|余额|欠费/i.test(msg)) {
                return '模型服务繁忙或账户额度不足，请稍后重试、更换模型或检查 API 配额。';
            }
            if (msg === 'login required for saved resume') return '请先登录。';
            if (msg === 'no saved resume') return '未找到已保存简历。';
            return msg;
        }
        function append(role, text) {
            var div = document.createElement('div');
            div.className = 'resume-ai-msg resume-ai-msg--' + (role === 'user' ? 'user' : 'asst');
            var lab = document.createElement('span');
            lab.className = 'resume-ai-msg-label';
            lab.textContent = role === 'user' ? '你' : '小助手';
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
                    var raw = (res.data && res.data.error) ? res.data.error : ('请求失败 (' + res.status + ')');
                    appendErr(mapChatError(raw));
                }
            }).catch(function () {
                appendErr('网络异常，请稍后重试。');
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
    <% } %>
</div>
