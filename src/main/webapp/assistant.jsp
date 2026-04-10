<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.bupt.ta.util.I18n" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="java.util.LinkedHashMap" %>
<%@ page import="java.util.Map" %>
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
    Map<String, String> asstI18nJs = new LinkedHashMap<String, String>();
    asstI18nJs.put("chatEmptyTitle", I18n.msg(request, "asst.chat.empty.title"));
    asstI18nJs.put("chatEmptyDesc", I18n.msg(request, "asst.chat.empty.desc"));
    asstI18nJs.put("roleUser", I18n.msg(request, "asst.js.role.user"));
    asstI18nJs.put("roleAssistant", I18n.msg(request, "asst.js.role.assistant"));
    asstI18nJs.put("avUser", I18n.msg(request, "asst.js.avatar.user"));
    asstI18nJs.put("avAssistant", I18n.msg(request, "asst.js.avatar.assistant"));
    asstI18nJs.put("extractUnsupported", I18n.msg(request, "asst.js.extract.unsupported"));
    asstI18nJs.put("extractNoText", I18n.msg(request, "asst.js.extract.noText"));
    asstI18nJs.put("extractFailed", I18n.msg(request, "asst.js.extract.failed"));
    asstI18nJs.put("extractEmpty", I18n.msg(request, "asst.js.extract.empty"));
    asstI18nJs.put("extractFallback", I18n.msg(request, "asst.js.extract.fallback"));
    asstI18nJs.put("fileBadExt", I18n.msg(request, "asst.js.file.badExt"));
    asstI18nJs.put("fileReadFail", I18n.msg(request, "asst.js.file.readFail"));
    asstI18nJs.put("statusParsing", I18n.msg(request, "asst.js.status.parsing"));
    asstI18nJs.put("statusLoaded", I18n.msg(request, "asst.js.status.loaded"));
    asstI18nJs.put("errRequest", I18n.msg(request, "asst.js.err.request"));
    asstI18nJs.put("networkExtract", I18n.msg(request, "asst.js.network.extract"));
    asstI18nJs.put("chatLoginSaved", I18n.msg(request, "asst.js.chat.loginSaved"));
    asstI18nJs.put("chatNoSaved", I18n.msg(request, "asst.js.chat.noSaved"));
    asstI18nJs.put("chatFmtNs", I18n.msg(request, "asst.js.chat.fmtNs"));
    asstI18nJs.put("chatEmptyUnread", I18n.msg(request, "asst.js.chat.emptyUnread"));
    asstI18nJs.put("chatCannotExtract", I18n.msg(request, "asst.js.chat.cannotExtract"));
    asstI18nJs.put("chatTooLong", I18n.msg(request, "asst.js.chat.tooLong"));
    asstI18nJs.put("alertNeedPaste", I18n.msg(request, "asst.js.alert.needPaste"));
    asstI18nJs.put("alertSavedBlock", I18n.msg(request, "asst.js.alert.savedBlock"));
    asstI18nJs.put("errHttp", I18n.msg(request, "asst.js.err.http"));
    asstI18nJs.put("networkChat", I18n.msg(request, "asst.js.network.chat"));
    String asstI18nJson = new Gson().toJson(asstI18nJs);
%>
<!DOCTYPE html>
<html lang="<%= I18n.htmlLangAttr(request) %>">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= I18n.msg(request, "asst.title") %></title>
    <link rel="stylesheet" href="<%= ctx %>/css/style.css?v=4">
    <link rel="stylesheet" href="<%= ctx %>/css/assistant.css?v=11">
</head>
<body class="dashboard-app assistant-page">
    <div class="assistant-page-bg" aria-hidden="true"></div>
    <div class="dashboard assistant-dashboard">
        <header class="page-header has-lang assistant-header">
            <jsp:include page="/WEB-INF/jsp/lang_switch.jsp"/>
            <a href="<%= ctx %>/" class="back-link assistant-back"><%= I18n.msg(request, "header.backHome") %></a>
            <div class="assistant-header-center">
                <span class="assistant-header-badge" aria-hidden="true">AI</span>
                <h1><%= I18n.msg(request, "asst.h1") %></h1>
                <p class="assistant-header-tagline"><%= I18n.msg(request, "asst.tagline") %></p>
            </div>
            <a href="<%= ctx %>/forum" class="back-link assistant-back assistant-header-forum"><%= I18n.msg(request, "home.card.forum") %></a>
        </header>

        <div class="assistant-intro">
            <p class="assistant-lead"><%= I18n.msg(request, "asst.lead") %></p>
            <nav class="assistant-tags-bar" aria-label="<%= I18n.msg(request, "asst.tags.aria") %>">
                <span class="assistant-tags-bar-item assistant-tags-bar-item--active"><%= I18n.msg(request, "asst.tag.apps") %></span>
                <span class="assistant-tags-bar-item"><%= I18n.msg(request, "asst.tag.resume") %></span>
                <span class="assistant-tags-bar-item"><%= I18n.msg(request, "asst.tag.help") %></span>
            </nav>
        </div>

        <div class="assistant-config-grid">
        <section class="section assistant-section assistant-resume-card" aria-labelledby="resume-card-title">
            <div class="assistant-resume-head">
                <span class="assistant-card-icon" aria-hidden="true">📄</span>
                <div class="assistant-resume-head-text">
                    <span id="resume-card-title" class="assistant-resume-title"><%= I18n.msg(request, "asst.resume.card") %></span>
                    <span class="assistant-resume-hint"><%= I18n.msg(request, "asst.resume.hint") %></span>
                </div>
            </div>
            <div class="assistant-resume-modes">
                <label class="assistant-radio"><input type="radio" name="resumeMode" value="none" checked> <%= I18n.msg(request, "asst.resume.none") %></label>
                <label class="assistant-radio"><input type="radio" name="resumeMode" value="paste"> <%= I18n.msg(request, "asst.resume.paste") %></label>
                <label class="assistant-radio <%= canUseSavedResume ? "" : "assistant-radio--disabled" %>">
                    <input type="radio" name="resumeMode" value="saved" <%= canUseSavedResume ? "" : "disabled" %>> <%= I18n.msg(request, "asst.resume.saved") %></label>
            </div>
            <% if (!loggedInTa) { %>
            <p class="assistant-resume-login-hint"><%= I18n.msg(request, "asst.resume.loginHint") %></p>
            <% } else if (!canUseSavedResume) { %>
            <p class="assistant-resume-login-hint"><%= I18n.msg(request, "asst.resume.noFile", ctx + "/ta/dashboard?tab=resume") %></p>
            <% } %>
            <div id="resumePastePanel" class="assistant-resume-paste" hidden>
                <label class="assistant-label" for="resumePaste"><%= I18n.msg(request, "asst.resume.pasteLabel") %></label>
                <textarea id="resumePaste" rows="6" placeholder="<%= I18n.msg(request, "asst.resume.paste.ph") %>"></textarea>
                <div class="assistant-resume-file">
                    <input type="file" id="resumeFile" accept=".txt,.pdf,.doc,.docx,application/pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document">
                    <span id="resumeExtractStatus" class="assistant-resume-file-status" aria-live="polite"></span>
                </div>
                <p class="assistant-resume-file-hint"><%= I18n.msg(request, "asst.resume.fileHint") %></p>
            </div>
            <p id="resumeSavedBadge" class="assistant-resume-saved" hidden><%= I18n.msg(request, "asst.resume.savedBadge") %></p>
        </section>

        <div class="section assistant-section assistant-toolbar">
            <div class="assistant-toolbar-inner">
            <div class="assistant-toolbar-row">
                <label class="assistant-label" for="providerSel"><span class="assistant-label-icon" aria-hidden="true">◇</span> <%= I18n.msg(request, "asst.model") %></label>
                <div class="assistant-select-wrap">
                    <select id="providerSel" name="provider" class="assistant-select">
                        <option value="kimi" <%= "kimi".equals(defProv) ? "selected" : "" %> <%= disableKimiOption ? "disabled" : "" %>><%= I18n.msg(request, "asst.option.kimi") %></option>
                        <option value="qwen" <%= "qwen".equals(defProv) ? "selected" : "" %> <%= disableQwenOption ? "disabled" : "" %>><%= I18n.msg(request, "asst.option.qwen") %></option>
                        <option value="openai" <%= "openai".equals(defProv) ? "selected" : "" %> <%= disableOpenaiOption ? "disabled" : "" %>><%= I18n.msg(request, "asst.option.openai") %></option>
                    </select>
                </div>
            </div>
            <% if (noAssistantKeys) { %>
            <div class="assistant-warn" role="alert">
                <span class="assistant-warn-icon" aria-hidden="true">!</span>
                <div class="assistant-warn-body">
                    <p class="assistant-warn-summary"><%= I18n.msg(request, "asst.warn.keys") %></p>
                    <details class="assistant-warn-details">
                        <summary class="assistant-warn-details-summary"><%= I18n.msg(request, "asst.warn.keys.toggle") %></summary>
                        <div class="assistant-warn-details-inner"><%= I18n.msg(request, "asst.warn.keys.details") %></div>
                    </details>
                </div>
            </div>
            <% } %>
            </div>
        </div>
        </div>

        <div class="assistant-conversation assistant-conversation-card">
        <section class="section assistant-section assistant-chat-section" aria-label="<%= I18n.msg(request, "asst.chat.sectionAria") %>">
            <div class="assistant-chat-head">
                <div class="assistant-chat-head-left">
                    <span class="assistant-chat-head-dot" aria-hidden="true"></span>
                    <h2 class="assistant-chat-head-title"><%= I18n.msg(request, "asst.chat.title") %></h2>
                </div>
                <span class="assistant-chat-head-hint"><%= I18n.msg(request, "asst.chat.disclaimer") %></span>
            </div>
            <div id="chatLog" class="assistant-chat" aria-live="polite">
                <div id="chatEmpty" class="assistant-chat-empty">
                    <div class="assistant-chat-empty-ring" aria-hidden="true">
                        <span class="assistant-chat-empty-icon">💬</span>
                    </div>
                    <p class="assistant-chat-empty-title"><%= I18n.msg(request, "asst.chat.empty.title") %></p>
                    <p class="assistant-chat-empty-desc"><%= I18n.msg(request, "asst.chat.empty.desc") %></p>
                </div>
            </div>
        </section>

        <form id="chatForm" class="section assistant-section assistant-form assistant-composer" autocomplete="off">
            <label class="assistant-label assistant-composer-label" for="userInput"><%= I18n.msg(request, "asst.input.label") %></label>
            <div class="assistant-composer-inner">
                <textarea id="userInput" rows="3" placeholder="<%= I18n.msg(request, "asst.input.ph") %>" <%= noAssistantKeys ? "disabled" : "" %>></textarea>
                <div class="assistant-actions">
                    <button type="submit" class="btn btn-primary assistant-btn-send" <%= noAssistantKeys ? "disabled" : "" %>>
                        <span class="assistant-btn-send-text"><%= I18n.msg(request, "asst.send") %></span>
                    </button>
                    <button type="button" id="btnClear" class="btn btn-secondary"><%= I18n.msg(request, "asst.clear") %></button>
                </div>
            </div>
        </form>
        </div>
    </div>

    <script>
        (function () {
            var ASST_I18N = <%= asstI18nJson %>;
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
                if (code === 'unsupported format') return ASST_I18N.extractUnsupported;
                if (code === 'no text extracted') return ASST_I18N.extractNoText;
                if (code === 'extract failed') return ASST_I18N.extractFailed;
                if (code === 'empty file') return ASST_I18N.extractEmpty;
                return code || ASST_I18N.extractFallback;
            }

            resumeFile.addEventListener('change', function () {
                var f = resumeFile.files && resumeFile.files[0];
                if (!f) return;
                var name = f.name || '';
                var lower = name.toLowerCase();
                if (!/\.(txt|pdf|doc|docx)$/i.test(lower)) {
                    alert(ASST_I18N.fileBadExt);
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
                        alert(ASST_I18N.fileReadFail);
                    };
                    reader.readAsText(f, 'UTF-8');
                    return;
                }
                if (resumeExtractStatus) resumeExtractStatus.textContent = ASST_I18N.statusParsing;
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
                        if (resumeExtractStatus) resumeExtractStatus.textContent = ASST_I18N.statusLoaded;
                    } else {
                        var err = (res.data && res.data.error) ? res.data.error : ASST_I18N.errRequest;
                        alert(mapExtractError(err));
                        if (resumeExtractStatus) resumeExtractStatus.textContent = '';
                    }
                }).catch(function () {
                    alert(ASST_I18N.networkExtract);
                    if (resumeExtractStatus) resumeExtractStatus.textContent = '';
                });
            });

            function mapChatError(msg) {
                if (msg === 'login required for saved resume') return ASST_I18N.chatLoginSaved;
                if (msg === 'no saved resume') return ASST_I18N.chatNoSaved;
                if (msg === 'saved resume format not supported') return ASST_I18N.chatFmtNs;
                if (msg === 'saved resume is empty or unreadable') return ASST_I18N.chatEmptyUnread;
                if (msg === 'cannot extract resume text') return ASST_I18N.chatCannotExtract;
                if (msg === 'resume text too long') return ASST_I18N.chatTooLong;
                return msg;
            }

            function formatHttpErr(status) {
                return (ASST_I18N.errHttp || '').replace(/\{0\}/g, String(status));
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
                var label = role === 'user' ? ASST_I18N.roleUser : ASST_I18N.roleAssistant;
                var av = role === 'user' ? ASST_I18N.avUser : ASST_I18N.avAssistant;
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
                        alert(ASST_I18N.alertNeedPaste);
                        return;
                    }
                } else if (mode === 'saved') {
                    if (!canUseSaved) {
                        alert(ASST_I18N.alertSavedBlock);
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
                        var raw = (res.data && res.data.error) ? res.data.error : formatHttpErr(res.status);
                        appendError(mapChatError(raw));
                    }
                }).catch(function () {
                    appendError(ASST_I18N.networkChat);
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
                    + '<p class="assistant-chat-empty-title"></p>'
                    + '<p class="assistant-chat-empty-desc"></p>';
                var tEl = emptyEl.querySelector('.assistant-chat-empty-title');
                var dEl = emptyEl.querySelector('.assistant-chat-empty-desc');
                if (tEl) tEl.textContent = ASST_I18N.chatEmptyTitle;
                if (dEl) dEl.textContent = ASST_I18N.chatEmptyDesc;
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
