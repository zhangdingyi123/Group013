package com.bupt.ta.web;

import com.bupt.ta.service.assistant.AssistantConfig;
import com.bupt.ta.service.assistant.AssistantWechatPayService;
import com.bupt.ta.service.assistant.WechatPayV3Client;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Collectors;

/**
 * 微信支付结果通知（无需登录；URL 须在商户平台配置为公网 HTTPS）。
 */
@WebServlet("/api/assistant/pay/wechat/notify")
public class AssistantWechatPayNotifyServlet extends HttpServlet {

    private final AssistantWechatPayService payService = new AssistantWechatPayService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        if (!AssistantConfig.wechatPayNativeReady()) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String serial = header(req, "Wechatpay-Serial");
        String signature = header(req, "Wechatpay-Signature");
        String timestamp = header(req, "Wechatpay-Timestamp");
        String nonce = header(req, "Wechatpay-Nonce");

        String body;
        try (java.io.BufferedReader reader = req.getReader()) {
            body = reader.lines().collect(Collectors.joining("\n"));
        }

        try {
            WechatPayV3Client client = WechatPayV3Client.fromConfig();
            if (!client.verifyNotify(serial, timestamp, nonce, body, signature)) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            JsonObject root = new JsonParser().parse(body).getAsJsonObject();
            String eventType = root.has("event_type") && root.get("event_type").isJsonPrimitive()
                    ? root.get("event_type").getAsString() : "";
            if (!"TRANSACTION.SUCCESS".equals(eventType)) {
                writeBody(resp, successJson());
                return;
            }
            JsonObject resource = root.getAsJsonObject("resource");
            if (resource == null) {
                writeBody(resp, successJson());
                return;
            }
            String associated = "";
            if (resource.has("associated_data") && resource.get("associated_data").isJsonPrimitive()) {
                associated = resource.get("associated_data").getAsString();
            }
            String resNonce = resource.has("nonce") && resource.get("nonce").isJsonPrimitive()
                    ? resource.get("nonce").getAsString() : "";
            String ciphertext = resource.has("ciphertext") && resource.get("ciphertext").isJsonPrimitive()
                    ? resource.get("ciphertext").getAsString() : "";
            String plain = client.decryptAeadToUtf8(associated, resNonce, ciphertext);
            String answer = payService.applyNotifySuccess(plain);
            writeBody(resp, answer);
        } catch (IllegalStateException e) {
            logErr("AssistantWechatPayNotify: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logErr("AssistantWechatPayNotify: " + String.valueOf(e.getMessage()));
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private static void logErr(String msg) {
        System.err.println(msg);
    }

    private static String header(HttpServletRequest req, String name) {
        String v = req.getHeader(name);
        return v != null ? v.trim() : "";
    }

    private static String successJson() {
        JsonObject o = new JsonObject();
        o.addProperty("code", "SUCCESS");
        o.addProperty("message", "成功");
        return o.toString();
    }

    private void writeBody(HttpServletResponse resp, String json) throws IOException {
        PrintWriter w = resp.getWriter();
        w.write(json);
    }
}
