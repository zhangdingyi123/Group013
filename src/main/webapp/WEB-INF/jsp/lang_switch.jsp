<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.bupt.ta.util.I18n" %>
<div class="lang-switch" role="navigation" aria-label="<%= I18n.msg(request, "lang.switch.aria") %>">
    <span class="lang-switch-inner">
        <a href="<%= I18n.switchLangUrl(request, "zh") %>" class="lang-switch-item <%= "zh".equals(I18n.lang(request)) ? "is-active" : "" %>" hreflang="zh-CN"><%= I18n.msg(request, "lang.zh") %></a>
        <span class="lang-switch-sep" aria-hidden="true">/</span>
        <a href="<%= I18n.switchLangUrl(request, "en") %>" class="lang-switch-item <%= "en".equals(I18n.lang(request)) ? "is-active" : "" %>" hreflang="en"><%= I18n.msg(request, "lang.en") %></a>
    </span>
</div>
