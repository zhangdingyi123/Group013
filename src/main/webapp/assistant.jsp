<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
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
    Boolean taIn = (Boolean) request.getAttribute("assistantTaLoggedIn");
    Boolean savedTxt = (Boolean) request.getAttribute("assistantSavedResumeTxt");
    boolean loggedInTa = taIn != null && taIn;
    boolean canUseSavedResume = loggedInTa && savedTxt != null && savedTxt;
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>智能小助手 · 助教招聘系统</title>
    <link rel="stylesheet" href="<%= ctx %>/css/style.css?v=3">
    <link rel="stylesheet" href="<%= ctx %>/css/assistant.css?v=7">
</head>
<body class="assistant-page">
    <div class="assistant-page-bg" aria-hidden="true"></div>
    <div class="dashboard assistant-dashboard">
        <header class="page-header assistant-header">
            <a href="<%= ctx %>/" class="back-link assistant-back">← 首页</a>
            <div class="assistant-header-center">
                <span class="assistant-header-badge" aria-hidden="true">AI</span>
                <h1>智能小助手</h1>
                <p class="assistant-header-tagline">招聘问答 · 简历辅助</p>
            </div>
            <span class="assistant-page-header-spacer" aria-hidden="true"></span>
        </header>

        <div class="assistant-intro">
            <p class="assistant-lead">切换下方模型（Kimi / 通义 / OpenAI），可附带简历上下文，协助润色表述与结构调整。</p>
            <nav class="assistant-tags-bar" aria-label="能力标签">
                <span class="assistant-tags-bar-item">岗位与申请</span>
                <span class="assistant-tags-bar-item">简历润色</span>
                <span class="assistant-tags-bar-item">站内说明</span>
            </nav>
        </div>

        <section class="section assistant-section assistant-resume-card" aria-labelledby="resume-card-title">
            <div class="assistant-resume-head">
                <span class="assistant-card-icon" aria-hidden="true">📄</span>
                <div class="assistant-resume-head-text">
                    <span id="resume-card-title" class="assistant-resume-title">简历辅助</span>
                    <span class="assistant-resume-hint">可选；开启后对话会带上简历上下文</span>
                </div>
            </div>
            <div class="assistant-resume-modes">
                <label class="assistant-radio"><input type="radio" name="resumeMode" value="none" checked> 不使用</label>
                <label class="assistant-radio"><input type="radio" name="resumeMode" value="paste"> 粘贴或上传文本</label>
                <label class="assistant-radio <%= canUseSavedResume ? "" : "assistant-radio--disabled" %>">
                    <input type="radio" name="resumeMode" value="saved" <%= canUseSavedResume ? "" : "disabled" %>> 使用站内已保存简历</label>
            </div>
            <% if (!loggedInTa) { %>
            <p class="assistant-resume-login-hint">登录应聘者账号后，可选择「站内已保存简历」（支持 .txt / .pdf / .doc / .docx）。</p>
            <% } else if (!canUseSavedResume) { %>
            <p class="assistant-resume-login-hint">当前未上传简历，请在 <a href="<%= ctx %>/ta/dashboard?tab=resume">工作台 · 简历</a> 上传或保存一份简历。</p>
            <% } %>
            <div id="resumePastePanel" class="assistant-resume-paste" hidden>
                <label class="assistant-label" for="resumePaste">简历正文（可粘贴，或选择本地文件由服务端解析）</label>
                <textarea id="resumePaste" rows="6" placeholder="粘贴简历文字，或下方选择 .txt / .pdf / .doc / .docx…"></textarea>
                <div class="assistant-resume-file">
                    <input type="file" id="resumeFile" accept=".txt,.pdf,.doc,.docx,application/pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document">
                    <span id="resumeExtractStatus" class="assistant-resume-file-status" aria-live="polite"></span>
                </div>
                <p class="assistant-resume-file-hint">纯文本 .txt 在浏览器直接读取；PDF / Word 将上传到服务器抽取正文（扫描版 PDF 可能无法识别文字）。</p>
            </div>
            <p id="resumeSavedBadge" class="assistant-resume-saved" hidden>将使用你在工作台保存的简历文件，由服务端抽取正文作为上下文。</p>
        </section>

        <div class="section assistant-section assistant-toolbar">
            <div class="assistant-toolbar-row">
                <label class="assistant-label" for="providerSel"><span class="assistant-label-icon" aria-hidden="true">◇</span> 模型</label>
                <div class="assistant-select-wrap">
                    <select id="providerSel" name="provider" class="assistant-select">
                        <option value="kimi" <%= "kimi".equals(defProv) ? "selected" : "" %> <%= disableKimiOption ? "disabled" : "" %>>Kimi K2.5</option>
                        <option value="qwen" <%= "qwen".equals(defProv) ? "selected" : "" %> <%= disableQwenOption ? "disabled" : "" %>>通义千问</option>
                        <option value="openai" <%= "openai".equals(defProv) ? "selected" : "" %> <%= disableOpenaiOption ? "disabled" : "" %>>OpenAI</option>
                    </select>
                </div>
            </div>
            <% if (noAssistantKeys) { %>
            <div class="assistant-warn" role="alert">
                <span class="assistant-warn-icon" aria-hidden="true">!</span>
                <span>服务端未检测到 API 密钥，暂无法发送对话。配好密钥后重启应用。推荐环境变量 <code>KIMI_API_KEY</code>、<code>QWEN_API_KEY</code>、<code>OPENAI_API_KEY</code>，或写入 <code>assistant.properties</code>。也可设 <code>ASSISTANT_PROPERTIES_PATH</code> 或 <code>-Dassistant.properties.path=...</code>。</span>
            </div>
            <% } %>
        </div>

        <section class="section assistant-section assistant-chat-section" aria-label="对话">
            <div class="assistant-chat-head">
                <div class="assistant-chat-head-left">
                    <span class="assistant-chat-head-dot" aria-hidden="true"></span>
                    <h2 class="assistant-chat-head-title">对话</h2>
                </div>
                <span class="assistant-chat-head-hint">内容由大模型生成，请自行核实重要信息</span>
            </div>
            <div id="chatLog" class="assistant-chat" aria-live="polite">
                <div id="chatEmpty" class="assistant-chat-empty">
                    <div class="assistant-chat-empty-ring" aria-hidden="true">
                        <span class="assistant-chat-empty-icon">💬</span>
                    </div>
                    <p class="assistant-chat-empty-title">开始提问</p>
                    <p class="assistant-chat-empty-desc">例如：如何申请助教岗位？简历项目经历怎么写更醒目？</p>
                </div>
            </div>
        </section>

        <form id="chatForm" class="section assistant-section assistant-form assistant-composer" autocomplete="off">
            <label class="assistant-label assistant-composer-label" for="userInput">输入消息</label>
            <div class="assistant-composer-inner">
                <textarea id="userInput" rows="3" placeholder="输入问题，例如：如何申请助教岗位？" <%= noAssistantKeys ? "disabled" : "" %>></textarea>
                <div class="assistant-actions">
                    <button type="submit" class="btn btn-primary assistant-btn-send" <%= noAssistantKeys ? "disabled" : "" %>>
                        <span class="assistant-btn-send-text">发送</span>
                    </button>
                    <button type="button" id="btnClear" class="btn btn-secondary">清空对话</button>
                </div>
            </div>
        </form>
    </div>

    <script>
        (function () {
            var ctx = '<%= ctx %>';
            var apiUrl = ctx + '/api/assistant/chat';
            var extractUrl = ctx + '/api/assistant/extract-resume';
            var messages = [];

            var logEl = document.getElementById('chatLog');
            var chatEmpty = document.getElementById('chatEmpty');
            var form = document.getElementById('chatForm');
            var input = document.getElementById('userInput');
            var providerSel = document.getElementById('providerSel');
            var resumePaste = document.getElementById('resumePaste');
            var resumePastePanel = document.getElementById('resumePastePanel');
            var resumeSavedBadge = document.getElementById('resumeSavedBadge');
            var resumeFile = document.getElementById('resumeFile');
            var resumeExtractStatus = document.getElementById('resumeExtractStatus');
            var canUseSaved = <%= canUseSavedResume ? "true" : "false" %>;

            function getResumeMode() {
                var r = document.querySelector('input[name="resumeMode"]:checked');
                return r ? r.value : 'none';
            }

            function syncResumePanels() {
                var m = getResumeMode();
                resumePastePanel.hidden = (m !== 'paste');
                resumeSavedBadge.hidden = (m !== 'saved');
            }

            document.querySelectorAll('input[name="resumeMode"]').forEach(function (el) {
                el.addEventListener('change', syncResumePanels);
            });
            syncResumePanels();

            function mapExtractError(code) {
                if (code === 'unsupported format') return '不支持的格式，请使用 .txt / .pdf / .doc / .docx。';
                if (code === 'no text extracted') return '未能从文件中识别出文字（常见于扫描版 PDF）。';
                if (code === 'extract failed') return '解析失败，请换用 .txt 或检查文件是否损坏。';
                if (code === 'empty file') return '文件为空。';
                return code || '上传解析失败';
            }

            resumeFile.addEventListener('change', function () {
                var f = resumeFile.files && resumeFile.files[0];
                if (!f) return;
                var name = f.name || '';
                var lower = name.toLowerCase();
                if (!/\.(txt|pdf|doc|docx)$/i.test(lower)) {
                    alert('请选择 .txt、.pdf、.doc 或 .docx 文件');
                    resumeFile.value = '';
                    return;
                }
                if (resumeExtractStatus) resumeExtractStatus.textContent = '';
                if (/\.txt$/i.test(lower)) {
                    var reader = new FileReader();
                    reader.onload = function () {
                        resumePaste.value = (reader.result || '').toString();
                    };
                    reader.onerror = function () {
                        alert('读取文件失败');
                    };
                    reader.readAsText(f, 'UTF-8');
                    return;
                }
                if (resumeExtractStatus) resumeExtractStatus.textContent = '正在解析…';
                var fd = new FormData();
                fd.append('file', f, name);
                fetch(extractUrl, {
                    method: 'POST',
                    body: fd,
                    credentials: 'same-origin'
                }).then(function (r) {
                    return r.json().then(function (data) {
                        return { ok: r.ok, data: data };
                    });
                }).then(function (res) {
                    if (res.data && res.data.ok && res.data.text) {
                        resumePaste.value = res.data.text;
                        if (resumeExtractStatus) resumeExtractStatus.textContent = '已载入正文';
                    } else {
                        var err = (res.data && res.data.error) ? res.data.error : '请求失败';
                        alert(mapExtractError(err));
                        if (resumeExtractStatus) resumeExtractStatus.textContent = '';
                    }
                }).catch(function () {
                    alert('网络异常，解析失败');
                    if (resumeExtractStatus) resumeExtractStatus.textContent = '';
                });
            });

            function mapChatError(msg) {
                if (msg === 'login required for saved resume') return '使用站内简历请先登录应聘者账号。';
                if (msg === 'no saved resume') return '尚未保存简历，请先到工作台上传或保存简历。';
                if (msg === 'saved resume format not supported') return '当前简历格式不支持解析。';
                if (msg === 'saved resume is empty or unreadable') return '简历内容为空或无法抽取正文（如扫描版 PDF）。';
                if (msg === 'cannot extract resume text') return '无法从已保存简历中抽取正文，请在工作台换用可编辑文本或 .txt。';
                if (msg === 'resume text too long') return '简历正文过长，请删减后再试（上限约 1 万字）。';
                return msg;
            }

            function hideChatEmpty() {
                if (chatEmpty) chatEmpty.hidden = true;
            }

            function showChatEmpty() {
                if (chatEmpty) chatEmpty.hidden = false;
            }

            function appendBubble(role, text) {
                hideChatEmpty();
                var div = document.createElement('div');
                div.className = 'assistant-msg assistant-msg--' + role;
                var label = role === 'user' ? '你' : '小助手';
                var av = role === 'user' ? '我' : '助';
                div.innerHTML = '<span class="assistant-msg-avatar" aria-hidden="true">' + av + '</span>'
                    + '<div class="assistant-msg-col"><span class="assistant-msg-label">' + label + '</span><div class="assistant-msg-body"></div></div>';
                div.querySelector('.assistant-msg-body').textContent = text;
                logEl.appendChild(div);
                logEl.scrollTop = logEl.scrollHeight;
            }

            function appendError(text) {
                hideChatEmpty();
                var div = document.createElement('div');
                div.className = 'assistant-msg assistant-msg--error-wrap';
                div.innerHTML = '<span class="assistant-msg-avatar assistant-msg-avatar--err" aria-hidden="true">!</span>'
                    + '<div class="assistant-msg-col"><div class="assistant-msg--error">' + escapeHtml(text) + '</div></div>';
                logEl.appendChild(div);
                logEl.scrollTop = logEl.scrollHeight;
            }

            function escapeHtml(s) {
                var d = document.createElement('div');
                d.textContent = s;
                return d.innerHTML;
            }

            form.addEventListener('submit', function (e) {
                e.preventDefault();
                var text = (input.value || '').trim();
                if (!text) return;
                var mode = getResumeMode();
                var resumeText = '';
                var useSavedResume = false;
                if (mode === 'paste') {
                    resumeText = (resumePaste.value || '').trim();
                    if (!resumeText) {
                        alert('请先粘贴简历正文或选择文件完成解析，或改选「不使用」。');
                        return;
                    }
                } else if (mode === 'saved') {
                    if (!canUseSaved) {
                        alert('当前无法使用站内简历，请检查是否已登录并在工作台保存 .txt / .pdf / .doc / .docx 简历。');
                        return;
                    }
                    useSavedResume = true;
                }

                appendBubble('user', text);
                messages.push({ role: 'user', content: text });
                input.value = '';

                var payload = {
                    provider: providerSel.value,
                    messages: messages
                };
                if (resumeText) payload.resumeText = resumeText;
                if (useSavedResume) payload.useSavedResume = true;

                var btn = form.querySelector('button[type="submit"]');
                btn.disabled = true;
                btn.classList.add('assistant-btn-send--loading');
                btn.setAttribute('aria-busy', 'true');
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
                        appendBubble('assistant', res.data.reply);
                        messages.push({ role: 'assistant', content: res.data.reply });
                    } else {
                        var raw = (res.data && res.data.error) ? res.data.error : ('请求失败 (' + res.status + ')');
                        appendError(mapChatError(raw));
                    }
                }).catch(function () {
                    appendError('网络异常，请稍后重试。');
                }).finally(function () {
                    btn.disabled = false;
                    btn.classList.remove('assistant-btn-send--loading');
                    btn.removeAttribute('aria-busy');
                });
            });

            document.getElementById('btnClear').addEventListener('click', function () {
                messages = [];
                logEl.innerHTML = '';
                var emptyEl = document.createElement('div');
                emptyEl.id = 'chatEmpty';
                emptyEl.className = 'assistant-chat-empty';
                emptyEl.innerHTML = '<div class="assistant-chat-empty-ring" aria-hidden="true">'
                    + '<span class="assistant-chat-empty-icon">💬</span></div>'
                    + '<p class="assistant-chat-empty-title">开始提问</p>'
                    + '<p class="assistant-chat-empty-desc">例如：如何申请助教岗位？简历项目经历怎么写更醒目？</p>';
                logEl.appendChild(emptyEl);
                chatEmpty = emptyEl;
                resumePaste.value = '';
                if (resumeFile) resumeFile.value = '';
                if (resumeExtractStatus) resumeExtractStatus.textContent = '';
                var noneRadio = document.querySelector('input[name="resumeMode"][value="none"]');
                if (noneRadio) noneRadio.checked = true;
                syncResumePanels();
            });
        })();
    </script>
</body>
</html>
