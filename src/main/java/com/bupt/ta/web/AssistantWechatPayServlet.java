package com.bupt.ta.web;

import com.bupt.ta.model.Applicant;
import com.bupt.ta.service.assistant.AssistantConfig;
import com.bupt.ta.service.assistant.AssistantWechatPayService;
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
 * 微信 Native 下单与订单查询（需登录助教）。
 */
@WebServlet(urlPatterns = {"/api/assistant/pay/wechat/native", "/api/assistant/pay/wechat/order"})
public class AssistantWechatPayServlet extends HttpServlet {

    private final AssistantWechatPayService payService = new AssistantWechatPayService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        String uri = req.getRequestURI() != null ? req.getRequestURI() : "";
        if (!uri.endsWith("/api/assistant/pay/wechat/order")) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            writeJson(resp, err("not found"));
            return;
        }
        HttpSession session = req.getSession(false);
        Applicant user = session != null ? (Applicant) session.getAttribute("taUser") : null;
        if (user == null || user.getId() == null || user.getId().trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJson(resp, err("login required"));
            return;
        }
        String userKey = "applicant:" + user.getId().trim();
        String outTradeNo = req.getParameter("outTradeNo");
        writeJson(resp, payService.getOrderForUser(userKey, outTradeNo));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        String uri = req.getRequestURI() != null ? req.getRequestURI() : "";
        if (!uri.endsWith("/api/assistant/pay/wechat/native")) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            writeJson(resp, err("not found"));
            return;
        }
        if (!AssistantConfig.wechatPayNativeReady()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(resp, err("wechat pay not configured"));
            return;
        }
        HttpSession session = req.getSession(false);
        Applicant user = session != null ? (Applicant) session.getAttribute("taUser") : null;
        if (user == null || user.getId() == null || user.getId().trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJson(resp, err("login required"));
            return;
        }
        String userKey = "applicant:" + user.getId().trim();

        String body;
        try (java.io.BufferedReader reader = req.getReader()) {
            body = reader.lines().collect(Collectors.joining("\n"));
        }
        int credits = 0;
        try {
            JsonObject root = new JsonParser().parse(body == null || body.isEmpty() ? "{}" : body).getAsJsonObject();
            if (root.has("credits") && root.get("credits").isJsonPrimitive()) {
                credits = root.get("credits").getAsInt();
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(resp, err("invalid json"));
            return;
        }

        try {
            JsonObject o = payService.createNativeOrder(userKey, credits);
            writeJson(resp, o);
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(resp, err(e.getMessage()));
        } catch (IllegalStateException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(resp, err(e.getMessage()));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
            writeJson(resp, err(e.getMessage() != null ? e.getMessage() : "wechat error"));
        }
    }

    private static JsonObject err(String msg) {
        JsonObject o = new JsonObject();
        o.addProperty("ok", false);
        o.addProperty("error", msg);
        return o;
    }

    private void writeJson(HttpServletResponse resp, JsonObject obj) throws IOException {
        PrintWriter w = resp.getWriter();
        w.write(obj.toString());
    }
}
