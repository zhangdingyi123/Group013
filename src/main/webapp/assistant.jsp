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
	    asstI18nJs.put("chatQuotaExceeded", I18n.msg(request, "asst.js.chat.quotaExceeded"));
	    asstI18nJs.put("alertNeedPaste", I18n.msg(request, "asst.js.alert.needPaste"));
	    asstI18nJs.put("alertSavedBlock", I18n.msg(request, "asst.js.alert.savedBlock"));
	    asstI18nJs.put("errHttp", I18n.msg(request, "asst.js.err.http"));
	    asstI18nJs.put("networkChat", I18n.msg(request, "asst.js.network.chat"));
    asstI18nJs.put("quotaTitle", I18n.msg(request, "asst.quota.title"));
    asstI18nJs.put("quotaStatusLoading", I18n.msg(request, "asst.quota.status.loading"));
    asstI18nJs.put("quotaFreeUsage", I18n.msg(request, "asst.quota.freeUsage"));
    asstI18nJs.put("quotaPaidCredits", I18n.msg(request, "asst.quota.paidCredits"));
    asstI18nJs.put("quotaTopupTitle", I18n.msg(request, "asst.quota.topup.title"));
    asstI18nJs.put("quotaTopupHint", I18n.msg(request, "asst.quota.topup.hint"));
    asstI18nJs.put("quotaTopupCodeLabel", I18n.msg(request, "asst.quota.topup.codeLabel"));
    asstI18nJs.put("quotaTopupCreditsLabel", I18n.msg(request, "asst.quota.topup.creditsLabel"));
    asstI18nJs.put("quotaTopupButton", I18n.msg(request, "asst.quota.topup.button"));
    asstI18nJs.put("quotaTopupLoginHint", I18n.msg(request, "asst.quota.topup.loginHint"));
    asstI18nJs.put("quotaTopupDisabled", I18n.msg(request, "asst.quota.topup.disabled"));
    asstI18nJs.put("quotaTopupSuccess", I18n.msg(request, "asst.quota.topup.success"));
    asstI18nJs.put("quotaTopupFailed", I18n.msg(request, "asst.quota.topup.failed"));
    asstI18nJs.put("quotaFetchFailed", I18n.msg(request, "asst.quota.fetch.failed"));
    asstI18nJs.put("quotaRemaining", I18n.msg(request, "asst.quota.remaining"));
    asstI18nJs.put("quotaPayHintLoading", I18n.msg(request, "asst.quota.pay.hint.loading"));
    asstI18nJs.put("quotaPayUnavailable", I18n.msg(request, "asst.quota.pay.unavailable"));
    asstI18nJs.put("quotaWechatHint", I18n.msg(request, "asst.quota.wechat.hint"));
    asstI18nJs.put("quotaWechatCreditsLabel", I18n.msg(request, "asst.quota.wechat.creditsLabel"));
    asstI18nJs.put("quotaWechatEstimated", I18n.msg(request, "asst.quota.wechat.estimated"));
    asstI18nJs.put("quotaWechatButton", I18n.msg(request, "asst.quota.wechat.button"));
    asstI18nJs.put("quotaWechatWait", I18n.msg(request, "asst.quota.wechat.wait"));
    asstI18nJs.put("quotaWechatSuccess", I18n.msg(request, "asst.quota.wechat.success"));
    asstI18nJs.put("quotaWechatFail", I18n.msg(request, "asst.quota.wechat.fail"));
    asstI18nJs.put("quotaWechatQrAlt", I18n.msg(request, "asst.quota.wechat.qrAlt"));
    String asstI18nJson = new Gson().toJson(asstI18nJs);
%>
<!DOCTYPE html>
<html lang="<%= I18n.htmlLangAttr(request) %>">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= I18n.msg(request, "asst.title") %></title>
    <link rel="stylesheet" href="<%= ctx %>/css/style.css?v=4">
    <link rel="stylesheet" href="<%= ctx %>/css/assistant.css?v=14">
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

        <aside class="assistant-sidebar" aria-label="<%= I18n.msg(request, "asst.sidebar.aria") %>">
            <section class="section assistant-section assistant-panel assistant-model-panel">
                <div class="assistant-panel-head">
                    <span class="assistant-panel-icon" aria-hidden="true">◇</span>
                    <div class="assistant-panel-head-text">
                        <h2 class="assistant-panel-title"><%= I18n.msg(request, "asst.model.panel.title") %></h2>
                        <p class="assistant-panel-desc"><%= I18n.msg(request, "asst.model.panel.desc") %></p>
                    </div>
                </div>
                <div class="assistant-model-row">
                    <label class="assistant-label assistant-label--block" for="providerSel"><%= I18n.msg(request, "asst.model") %></label>
                    <div class="assistant-select-wrap assistant-select-wrap--full">
                        <select id="providerSel" name="provider" class="assistant-select assistant-select--full">
                            <option value="kimi" <%= "kimi".equals(defProv) ? "selected" : "" %> <%= disableKimiOption ? "disabled" : "" %>><%= I18n.msg(request, "asst.option.kimi") %></option>
                            <option value="qwen" <%= "qwen".equals(defProv) ? "selected" : "" %> <%= disableQwenOption ? "disabled" : "" %>><%= I18n.msg(request, "asst.option.qwen") %></option>
                            <option value="openai" <%= "openai".equals(defProv) ? "selected" : "" %> <%= disableOpenaiOption ? "disabled" : "" %>><%= I18n.msg(request, "asst.option.openai") %></option>
                        </select>
                    </div>
                </div>
            </section>

            <section class="section assistant-section assistant-panel assistant-quota-panel" aria-labelledby="assistantQuotaTitle">
                <div class="assistant-panel-head">
                    <span class="assistant-panel-icon assistant-panel-icon--quota" aria-hidden="true">💰</span>
                    <div class="assistant-panel-head-text">
                        <h2 id="assistantQuotaTitle" class="assistant-panel-title"><%= I18n.msg(request, "asst.quota.title") %></h2>
                        <p id="assistantQuotaPayHint" class="assistant-panel-desc"><%= I18n.msg(request, "asst.quota.pay.hint.loading") %></p>
                    </div>
                </div>
                <div class="assistant-quota-status" id="assistantQuotaStatus"><%= I18n.msg(request, "asst.quota.status.loading") %></div>
                <div class="assistant-quota-metrics" role="group" aria-label="<%= I18n.msg(request, "asst.quota.metrics.aria") %>">
                    <div class="assistant-quota-metric">
                        <span class="assistant-quota-label"><%= I18n.msg(request, "asst.quota.freeUsage") %></span>
                        <span class="assistant-quota-value"><strong id="assistantQuotaFreeUsed">0</strong><span class="assistant-quota-sep">/</span><span id="assistantQuotaFreeLimit">0</span></span>
                    </div>
                    <div class="assistant-quota-metric">
                        <span class="assistant-quota-label"><%= I18n.msg(request, "asst.quota.paidCredits") %></span>
                        <span class="assistant-quota-value"><strong id="assistantQuotaPaidCredits">0</strong></span>
                    </div>
                </div>
                <div id="assistantQuotaTopupArea" class="assistant-quota-topup-area">
                    <p class="assistant-quota-note" id="assistantQuotaTopupNote"><%= loggedInTa ? "" : I18n.msg(request, "asst.quota.topup.loginHint") %></p>
                    <div id="assistantPayWechat" class="assistant-pay-wechat" hidden>
                        <p class="assistant-quota-note assistant-quota-note--sub"><%= I18n.msg(request, "asst.quota.wechat.hint") %></p>
                        <div class="assistant-quota-form assistant-quota-form--wechat">
                            <label class="assistant-label" for="assistantWechatCredits"><%= I18n.msg(request, "asst.quota.wechat.creditsLabel") %></label>
                            <input id="assistantWechatCredits" class="assistant-input" type="number" min="1" max="10000" step="1" value="10" />
                            <p class="assistant-wechat-estimate" id="assistantWechatEstimate" aria-live="polite"></p>
                            <button type="button" id="assistantWechatPayBtn" class="btn btn-primary"><%= I18n.msg(request, "asst.quota.wechat.button") %></button>
                            <div id="assistantWechatQrWrap" class="assistant-wechat-qr-wrap" hidden>
                                <img id="assistantWechatQrImg" src="" alt="<%= I18n.msg(request, "asst.quota.wechat.qrAlt") %>" width="220" height="220" loading="lazy" />
                            </div>
                            <p class="assistant-quota-result assistant-quota-result--wechat" id="assistantWechatPayStatus"></p>
                        </div>
                    </div>
                    <div id="assistantPayTopup" class="assistant-pay-topup" hidden>
                    <form id="assistantQuotaTopupForm" class="assistant-quota-form">
                        <label class="assistant-label" for="assistantTopupCode"><%= I18n.msg(request, "asst.quota.topup.codeLabel") %></label>
                        <input id="assistantTopupCode" class="assistant-input" type="text" autocomplete="off" placeholder="XXXX-XXXX" />
                        <label class="assistant-label" for="assistantTopupCredits"><%= I18n.msg(request, "asst.quota.topup.creditsLabel") %></label>
                        <input id="assistantTopupCredits" class="assistant-input" type="number" min="1" step="1" value="10" />
                        <button type="submit" id="assistantTopupButton" class="btn btn-primary"><%= I18n.msg(request, "asst.quota.topup.button") %></button>
                        <p class="assistant-quota-result" id="assistantQuotaResult"></p>
                    </form>
                    </div>
                    <div id="assistantQuotaNoPayBox" class="assistant-quota-nopay" hidden>
                        <p class="assistant-quota-nopay-text"><%= I18n.msg(request, "asst.quota.nopay.body") %></p>
                    </div>
                </div>
            </section>

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
        </aside>
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
            var assistantQuotaStatus = document.getElementById('assistantQuotaStatus');
            var assistantQuotaFreeUsed = document.getElementById('assistantQuotaFreeUsed');
            var assistantQuotaFreeLimit = document.getElementById('assistantQuotaFreeLimit');
            var assistantQuotaPaidCredits = document.getElementById('assistantQuotaPaidCredits');
            var assistantQuotaTopupArea = document.getElementById('assistantQuotaTopupArea');
            var assistantQuotaTopupNote = document.getElementById('assistantQuotaTopupNote');
            var assistantQuotaTopupForm = document.getElementById('assistantQuotaTopupForm');
            var assistantTopupCode = document.getElementById('assistantTopupCode');
            var assistantTopupCredits = document.getElementById('assistantTopupCredits');
            var assistantTopupButton = document.getElementById('assistantTopupButton');
            var assistantQuotaResult = document.getElementById('assistantQuotaResult');
            var assistantQuotaPayHint = document.getElementById('assistantQuotaPayHint');
            var assistantPayWechat = document.getElementById('assistantPayWechat');
            var assistantPayTopup = document.getElementById('assistantPayTopup');
            var assistantWechatCredits = document.getElementById('assistantWechatCredits');
            var assistantWechatEstimate = document.getElementById('assistantWechatEstimate');
            var assistantWechatPayBtn = document.getElementById('assistantWechatPayBtn');
            var assistantWechatQrWrap = document.getElementById('assistantWechatQrWrap');
            var assistantWechatQrImg = document.getElementById('assistantWechatQrImg');
            var assistantWechatPayStatus = document.getElementById('assistantWechatPayStatus');
            var assistantQuotaNoPayBox = document.getElementById('assistantQuotaNoPayBox');
            var canUseSaved = <%= canUseSavedResume ? "true" : "false" %>;
            var canTopup = <%= loggedInTa ? "true" : "false" %>;
            var wechatPollTimer = null;
            window.__asstFenPerCredit = 10;

            function getResumeMode() {
                var r = document.querySelector('input[name="resumeMode"]:checked');
                return r ? r.value : 'none';
            }

            function syncResumePanels() {
                var m = getResumeMode();
                resumePastePanel.hidden = (m !== 'paste');
                resumeSavedBadge.hidden = (m !== 'saved');
            }

            function updateQuotaDisplay(status) {
                if (!status) return;
                var remLabel = ASST_I18N.quotaRemaining || '';
                var rem = (status.freeRemaining != null && status.freeRemaining !== '') ? status.freeRemaining : '';
                assistantQuotaStatus.textContent = (status.period || '') + ' · ' + rem + (remLabel ? ' ' + remLabel : '');
                assistantQuotaFreeUsed.textContent = status.freeUsed;
                assistantQuotaFreeLimit.textContent = status.freeLimit;
                assistantQuotaPaidCredits.textContent = status.paidCredits;
                assistantQuotaResult.textContent = '';
                assistantQuotaResult.className = 'assistant-quota-result';
                assistantTopupButton.disabled = !canTopup;
                if (assistantWechatPayBtn) assistantWechatPayBtn.disabled = !canTopup;
            }

            function applyPayMode(data) {
                if (!assistantPayWechat || !assistantPayTopup || !assistantQuotaPayHint) return;
                window.__asstFenPerCredit = (data && data.fenPerCredit) ? data.fenPerCredit : 10;
                if (data && data.wechatPayReady) {
                    assistantPayWechat.hidden = false;
                    assistantPayTopup.hidden = true;
                    assistantQuotaPayHint.textContent = ASST_I18N.quotaWechatHint || '';
                    if (assistantQuotaNoPayBox) assistantQuotaNoPayBox.hidden = true;
                } else if (data && data.topupReady) {
                    assistantPayWechat.hidden = true;
                    assistantPayTopup.hidden = false;
                    assistantQuotaPayHint.textContent = ASST_I18N.quotaTopupHint || '';
                    if (assistantQuotaNoPayBox) assistantQuotaNoPayBox.hidden = true;
                } else {
                    assistantPayWechat.hidden = true;
                    assistantPayTopup.hidden = true;
                    assistantQuotaPayHint.textContent = ASST_I18N.quotaPayUnavailable || '';
                    if (assistantQuotaNoPayBox) assistantQuotaNoPayBox.hidden = false;
                }
                syncWechatEstimate();
            }

            function syncWechatEstimate() {
                if (!assistantWechatCredits || !assistantWechatEstimate) return;
                var c = parseInt(assistantWechatCredits.value, 10) || 0;
                var fenPer = window.__asstFenPerCredit || 10;
                if (c < 1) {
                    assistantWechatEstimate.textContent = '';
                    return;
                }
                var fen = c * fenPer;
                var yuan = (fen / 100).toFixed(2);
                var t = ASST_I18N.quotaWechatEstimated || '';
                assistantWechatEstimate.textContent = t.replace(/\{0\}/g, yuan).replace(/\{1\}/g, String(fenPer));
            }

            function stopWechatPoll() {
                if (wechatPollTimer) {
                    clearInterval(wechatPollTimer);
                    wechatPollTimer = null;
                }
            }

            function startWechatPoll(outTradeNo) {
                stopWechatPoll();
                if (!outTradeNo) return;
                wechatPollTimer = setInterval(function () {
                    fetch(ctx + '/api/assistant/pay/wechat/order?outTradeNo=' + encodeURIComponent(outTradeNo), {
                        method: 'GET',
                        credentials: 'same-origin'
                    }).then(function (r) { return r.json(); }).then(function (data) {
                        if (data && data.ok && data.status === 'PAID') {
                            stopWechatPoll();
                            if (assistantWechatPayStatus) {
                                assistantWechatPayStatus.textContent = ASST_I18N.quotaWechatSuccess;
                                assistantWechatPayStatus.className = 'assistant-quota-result assistant-quota-result--wechat assistant-quota-result--success';
                            }
                            fetchQuota();
                        }
                    }).catch(function () {});
                }, 2500);
            }

            function setQuotaMessage(text, isError) {
                assistantQuotaResult.textContent = text;
                assistantQuotaResult.className = 'assistant-quota-result' + (isError ? ' assistant-quota-result--error' : ' assistant-quota-result--success');
            }

            function renderQuotaError(msg) {
                assistantQuotaStatus.textContent = msg || ASST_I18N.quotaFetchFailed;
                assistantQuotaFreeUsed.textContent = '0';
                assistantQuotaFreeLimit.textContent = '0';
                assistantQuotaPaidCredits.textContent = '0';
            }

            function fetchQuota() {
                fetch(ctx + '/api/assistant/quota', {
                    method: 'GET',
                    credentials: 'same-origin'
                }).then(function (r) {
                    return r.json().then(function (data) {
                        return { ok: r.ok, data: data };
                    });
                }).then(function (res) {
                    if (res.ok && res.data) {
                        updateQuotaDisplay(res.data);
                        if (!canTopup) {
                            stopWechatPoll();
                            assistantQuotaTopupNote.textContent = ASST_I18N.quotaTopupLoginHint;
                            assistantQuotaTopupForm.querySelectorAll('input, button').forEach(function (el) { el.disabled = true; });
                            if (assistantPayWechat) assistantPayWechat.hidden = true;
                            if (assistantPayTopup) assistantPayTopup.hidden = true;
                            if (assistantQuotaNoPayBox) assistantQuotaNoPayBox.hidden = true;
                            if (assistantQuotaPayHint) assistantQuotaPayHint.textContent = ASST_I18N.quotaTopupLoginHint;
                        } else {
                            applyPayMode(res.data);
                        }
                    } else {
                        renderQuotaError(res.data && res.data.error ? res.data.error : ASST_I18N.quotaFetchFailed);
                    }
                }).catch(function () {
                    renderQuotaError(ASST_I18N.quotaFetchFailed);
                });
            }

            document.querySelectorAll('input[name="resumeMode"]').forEach(function (el) {
                el.addEventListener('change', syncResumePanels);
            });
            syncResumePanels();

            if (!canTopup) {
                assistantTopupButton.disabled = true;
                if (assistantWechatPayBtn) assistantWechatPayBtn.disabled = true;
            }
            if (assistantWechatCredits) {
                assistantWechatCredits.addEventListener('input', syncWechatEstimate);
            }
            if (assistantWechatPayBtn) {
                assistantWechatPayBtn.addEventListener('click', function () {
                    if (!canTopup) return;
                    var credits = parseInt(assistantWechatCredits.value, 10) || 0;
                    if (credits < 1 || credits > 10000) {
                        if (assistantWechatPayStatus) {
                            assistantWechatPayStatus.textContent = ASST_I18N.quotaWechatFail;
                            assistantWechatPayStatus.className = 'assistant-quota-result assistant-quota-result--wechat assistant-quota-result--error';
                        }
                        return;
                    }
                    assistantWechatPayBtn.disabled = true;
                    if (assistantWechatPayStatus) {
                        assistantWechatPayStatus.textContent = '';
                        assistantWechatPayStatus.className = 'assistant-quota-result assistant-quota-result--wechat';
                    }
                    fetch(ctx + '/api/assistant/pay/wechat/native', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json;charset=UTF-8' },
                        credentials: 'same-origin',
                        body: JSON.stringify({ credits: credits })
                    }).then(function (r) {
                        return r.json().then(function (data) {
                            return { ok: r.ok, data: data };
                        });
                    }).then(function (res) {
                        if (res.ok && res.data && res.data.ok && res.data.codeUrl) {
                            if (assistantWechatQrWrap) assistantWechatQrWrap.hidden = false;
                            if (assistantWechatQrImg) {
                                assistantWechatQrImg.src = 'https://quickchart.io/qr?size=220&text=' + encodeURIComponent(res.data.codeUrl);
                            }
                            if (assistantWechatPayStatus) {
                                assistantWechatPayStatus.textContent = ASST_I18N.quotaWechatWait;
                                assistantWechatPayStatus.className = 'assistant-quota-result assistant-quota-result--wechat';
                            }
                            startWechatPoll(res.data.outTradeNo);
                        } else {
                            var msg = (res.data && res.data.error) ? res.data.error : ASST_I18N.quotaWechatFail;
                            if (assistantWechatPayStatus) {
                                assistantWechatPayStatus.textContent = msg;
                                assistantWechatPayStatus.className = 'assistant-quota-result assistant-quota-result--wechat assistant-quota-result--error';
                            }
                        }
                    }).catch(function () {
                        if (assistantWechatPayStatus) {
                            assistantWechatPayStatus.textContent = ASST_I18N.quotaWechatFail;
                            assistantWechatPayStatus.className = 'assistant-quota-result assistant-quota-result--wechat assistant-quota-result--error';
                        }
                    }).finally(function () {
                        assistantWechatPayBtn.disabled = !canTopup;
                    });
                });
            }
            fetchQuota();

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

	            function mapChatError(msg, data) {
	                if (data && data.code === 'ASSISTANT_QUOTA_EXCEEDED') {
	                    var base = ASST_I18N.chatQuotaExceeded || msg;
	                    if (data.payHint) return base + ' ' + String(data.payHint);
	                    return base;
	                }
	                if (msg === 'login required for saved resume') return ASST_I18N.chatLoginSaved;
	                if (msg === 'no saved resume') return ASST_I18N.chatNoSaved;
	                if (msg === 'saved resume format not supported') return ASST_I18N.chatFmtNs;
	                if (msg === 'saved resume is empty or unreadable') return ASST_I18N.chatEmptyUnread;
	                if (msg === 'cannot extract resume text') return ASST_I18N.chatCannotExtract;
	                if (msg === 'resume text too long') return ASST_I18N.chatTooLong;
	                if (msg === 'assistant quota exceeded') return ASST_I18N.chatQuotaExceeded;
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

            assistantQuotaTopupForm.addEventListener('submit', function (e) {
                e.preventDefault();
                if (!canTopup) {
                    setQuotaMessage(ASST_I18N.quotaTopupLoginHint, true);
                    return;
                }
                var code = (assistantTopupCode.value || '').trim();
                var credits = parseInt(assistantTopupCredits.value, 10) || 0;
                if (!code || credits <= 0) {
                    setQuotaMessage(ASST_I18N.quotaTopupFailed, true);
                    return;
                }
                assistantTopupButton.disabled = true;
                fetch(ctx + '/api/assistant/quota/topup', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json;charset=UTF-8' },
                    credentials: 'same-origin',
                    body: JSON.stringify({ code: code, credits: credits })
                }).then(function (r) {
                    return r.json().then(function (data) {
                        return { ok: r.ok, data: data, status: r.status };
                    });
                }).then(function (res) {
                    if (res.ok && res.data) {
                        setQuotaMessage(ASST_I18N.quotaTopupSuccess, false);
                        updateQuotaDisplay(res.data);
                    } else {
                        var msg = (res.data && res.data.error) ? res.data.error : ASST_I18N.quotaTopupFailed;
                        setQuotaMessage(msg, true);
                    }
                }).catch(function () {
                    setQuotaMessage(ASST_I18N.quotaTopupFailed, true);
                }).finally(function () {
                    assistantTopupButton.disabled = !canTopup;
                });
            });

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
	                        if (res.data.freeRemaining !== undefined) {
	                            updateQuotaDisplay(res.data);
	                        }
	                    } else {
	                        var raw = (res.data && res.data.error) ? res.data.error : formatHttpErr(res.status);
	                        appendError(mapChatError(raw, res.data));
	                        if (res.data && res.data.freeRemaining !== undefined) {
	                            updateQuotaDisplay(res.data);
	                        }
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
