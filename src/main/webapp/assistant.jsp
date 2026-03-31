<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    Boolean kimiOk = (Boolean) request.getAttribute("assistantKimiConfigured");
    Boolean qwenOk = (Boolean) request.getAttribute("assistantQwenConfigured");
    String defProv = (String) request.getAttribute("assistantDefaultProvider");
    if (defProv == null) defProv = "kimi";
    boolean hasKimi = kimiOk != null && kimiOk;
    boolean hasQwen = qwenOk != null && qwenOk;
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>智能小助手 · 助教招聘系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=3">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/assistant.css?v=1">
</head>
<body class="assistant-page">
    <header class="assistant-header">
        <a href="${pageContext.request.contextPath}/" class="assistant-back">← 返回首页</a>
        <h1>智能小助手</h1>
        <p class="assistant-sub">模型可在下方切换：Kimi（月之暗面 K2.5）与通义千问（阿里云 DashScope）</p>
    </header>

    <div class="assistant-panel">
        <div class="assistant-toolbar">
            <label class="assistant-label" for="providerSel">模型</label>
            <select id="providerSel" name="provider" <%= (!hasKimi && !hasQwen) ? "disabled" : "" %>>
                <option value="kimi" <%= "kimi".equals(defProv) ? "selected" : "" %> <%= !hasKimi ? "disabled" : "" %>>Kimi K2.5</option>
                <option value="qwen" <%= "qwen".equals(defProv) ? "selected" : "" %> <%= !hasQwen ? "disabled" : "" %>>通义千问</option>
            </select>
            <% if (!hasKimi && !hasQwen) { %>
            <span class="assistant-warn">服务端未配置 API 密钥，请在部署环境设置 KIMI_API_KEY 或 QWEN_API_KEY（或 DASHSCOPE_API_KEY）。</span>
            <% } %>
        </div>

        <div id="chatLog" class="assistant-chat" aria-live="polite"></div>

        <form id="chatForm" class="assistant-form" autocomplete="off">
            <textarea id="userInput" rows="3" placeholder="输入问题，例如：如何申请助教岗位？" <%= (!hasKimi && !hasQwen) ? "disabled" : "" %>></textarea>
            <div class="assistant-actions">
                <button type="submit" class="btn-primary" <%= (!hasKimi && !hasQwen) ? "disabled" : "" %>>发送</button>
                <button type="button" id="btnClear" class="btn-secondary">清空对话</button>
            </div>
        </form>
    </div>

    <script>
        (function () {
            var ctx = '<%= request.getContextPath() %>';
            var apiUrl = ctx + '/api/assistant/chat';
            var messages = [];

            var logEl = document.getElementById('chatLog');
            var form = document.getElementById('chatForm');
            var input = document.getElementById('userInput');
            var providerSel = document.getElementById('providerSel');

            function appendBubble(role, text) {
                var div = document.createElement('div');
                div.className = 'assistant-msg assistant-msg--' + role;
                var label = role === 'user' ? '你' : '小助手';
                div.innerHTML = '<span class="assistant-msg-label">' + label + '</span><div class="assistant-msg-body"></div>';
                div.querySelector('.assistant-msg-body').textContent = text;
                logEl.appendChild(div);
                logEl.scrollTop = logEl.scrollHeight;
            }

            function appendError(text) {
                var div = document.createElement('div');
                div.className = 'assistant-msg assistant-msg--error';
                div.textContent = text;
                logEl.appendChild(div);
                logEl.scrollTop = logEl.scrollHeight;
            }

            form.addEventListener('submit', function (e) {
                e.preventDefault();
                var text = (input.value || '').trim();
                if (!text) return;
                if (providerSel.disabled) return;

                appendBubble('user', text);
                messages.push({ role: 'user', content: text });
                input.value = '';

                var payload = {
                    provider: providerSel.value,
                    messages: messages
                };

                var btn = form.querySelector('button[type="submit"]');
                btn.disabled = true;
                fetch(apiUrl, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json;charset=UTF-8' },
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
                        var err = (res.data && res.data.error) ? res.data.error : ('请求失败 (' + res.status + ')');
                        appendError(err);
                    }
                }).catch(function () {
                    appendError('网络异常，请稍后重试。');
                }).finally(function () {
                    btn.disabled = false;
                });
            });

            document.getElementById('btnClear').addEventListener('click', function () {
                messages = [];
                logEl.innerHTML = '';
            });
        })();
    </script>
</body>
</html>
