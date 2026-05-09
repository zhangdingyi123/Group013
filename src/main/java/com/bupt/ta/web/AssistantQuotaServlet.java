package com.bupt.ta.web;

import com.bupt.ta.model.Applicant;
import com.bupt.ta.service.assistant.AssistantConfig;
import com.bupt.ta.service.assistant.AssistantQuotaService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Collectors;

/**
 * JSON API：查询/充值 小助手额度。
 *
 * <ul>
 *   <li>GET /api/assistant/quota：查询当前登录助教额度</li>
 *   <li>POST /api/assistant/quota/topup：使用兑换码充值（需登录）</li>
 * </ul>
 */
@WebServlet({"/api/assistant/quota", "/api/assistant/quota/topup"})
public class AssistantQuotaServlet extends HttpServlet {

    private final AssistantQuotaService quotaService = new AssistantQuotaService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        HttpSession session = req.getSession(false);
        Applicant user = session != null ? (Applicant) session.getAttribute("taUser") : null;
        if (user == null || user.getId() == null || user.getId().trim().isEmpty()) {
            writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "login required");
            return;
        }
        String userKey = "applicant:" + user.getId().trim();

        AssistantQuotaService.Status st = quotaService.status(userKey);
        JsonObject o = new JsonObject();
        o.addProperty("ok", true);
        o.addProperty("period", st.period);
        o.addProperty("freeLimit", st.freeLimit);
        o.addProperty("freeUsed", st.freeUsed);
        o.addProperty("freeRemaining", st.freeRemaining);
        o.addProperty("paidCredits", st.paidCredits);
        o.addProperty("loggedIn", user != null);
        o.addProperty("wechatPayReady", AssistantConfig.wechatPayNativeReady());
        o.addProperty("topupReady", !AssistantConfig.topupCode().isEmpty());
        o.addProperty("fenPerCredit", AssistantConfig.wechatPayFenPerCredit());
        String hint = AssistantConfig.payHint();
        if (hint != null && !hint.trim().isEmpty()) {
            o.addProperty("payHint", hint.trim());
        }
        writeJson(resp, o);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        String uri = req.getRequestURI() != null ? req.getRequestURI() : "";
        if (!uri.endsWith("/api/assistant/quota/topup")) {
            writeError(resp, HttpServletResponse.SC_NOT_FOUND, "not found");
            return;
        }

        HttpSession session = req.getSession(false);
        Applicant user = session != null ? (Applicant) session.getAttribute("taUser") : null;
        if (user == null || user.getId() == null || user.getId().trim().isEmpty()) {
            writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "login required");
            return;
        }
        String userKey = "applicant:" + user.getId().trim();

        String body;
        try (java.io.BufferedReader reader = req.getReader()) {
            body = reader.lines().collect(Collectors.joining("\n"));
        }
        JsonObject root;
        try {
            root = new JsonParser().parse(body).getAsJsonObject();
        } catch (Exception e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "invalid json");
            return;
        }

        String code = root.has("code") && root.get("code").isJsonPrimitive() ? root.get("code").getAsString() : "";
        int credits = 0;
        try {
            if (root.has("credits") && root.get("credits").isJsonPrimitive()) {
                credits = root.get("credits").getAsInt();
            }
        } catch (Exception ignored) {}

        AssistantQuotaService.ConsumeResult r = quotaService.topup(userKey, code, credits);
        if (!r.ok) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject o = new JsonObject();
            o.addProperty("error", r.message);
            o.addProperty("code", r.code);
            if (r.status != null) {
                o.addProperty("period", r.status.period);
                o.addProperty("freeLimit", r.status.freeLimit);
                o.addProperty("freeUsed", r.status.freeUsed);
                o.addProperty("freeRemaining", r.status.freeRemaining);
                o.addProperty("paidCredits", r.status.paidCredits);
            }
            writeJson(resp, o);
            return;
        }

        JsonObject o = new JsonObject();
        o.addProperty("ok", true);
        if (r.status != null) {
            o.addProperty("period", r.status.period);
            o.addProperty("freeLimit", r.status.freeLimit);
            o.addProperty("freeUsed", r.status.freeUsed);
            o.addProperty("freeRemaining", r.status.freeRemaining);
            o.addProperty("paidCredits", r.status.paidCredits);
        }
        writeJson(resp, o);
    }

    private void writeJson(HttpServletResponse resp, JsonObject obj) throws IOException {
        PrintWriter w = resp.getWriter();
        w.write(obj.toString());
    }

    private void writeError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        JsonObject o = new JsonObject();
        o.addProperty("error", message);
        writeJson(resp, o);
    }
}

