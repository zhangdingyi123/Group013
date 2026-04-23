package com.bupt.ta.service.assistant;

import com.bupt.ta.model.AssistantPayOrder;
import com.bupt.ta.storage.Storage;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 小助手额度：微信 Native 下单与支付成功入账。
 */
public class AssistantWechatPayService {

    private static final Object NOTIFY_LOCK = new Object();

    private final AssistantQuotaService quotaService = new AssistantQuotaService();

    public JsonObject createNativeOrder(String userKey, int credits) throws Exception {
        if (!AssistantConfig.wechatPayNativeReady()) {
            throw new IllegalStateException("wechat pay not configured");
        }
        if (credits < 1 || credits > 10000) {
            throw new IllegalArgumentException("invalid credits");
        }
        int fenPer = AssistantConfig.wechatPayFenPerCredit();
        long total = (long) credits * fenPer;
        int amountFen = (int) Math.min(Integer.MAX_VALUE, Math.max(1L, total));

        String outTradeNo = java.util.UUID.randomUUID().toString().replace("-", "");

        AssistantPayOrder ord = new AssistantPayOrder();
        ord.setOutTradeNo(outTradeNo);
        ord.setUserKey(userKey);
        ord.setCredits(credits);
        ord.setAmountFen(amountFen);
        ord.setStatus(AssistantPayOrder.STATUS_PENDING);
        ord.setCreatedAt(System.currentTimeMillis());
        ord.setPaidAt(0L);
        ord.setTransactionId(null);

        List<AssistantPayOrder> list = new ArrayList<>(Storage.loadAssistantPayOrders());
        list.add(ord);
        Storage.saveAssistantPayOrders(list);

        WechatPayV3Client client = WechatPayV3Client.fromConfig();
        try {
            String codeUrl = client.nativePrepay(
                    outTradeNo,
                    AssistantConfig.wechatPayDescription(),
                    amountFen,
                    AssistantConfig.wechatPayNotifyUrl());
            JsonObject o = new JsonObject();
            o.addProperty("ok", true);
            o.addProperty("codeUrl", codeUrl);
            o.addProperty("outTradeNo", outTradeNo);
            o.addProperty("amountFen", amountFen);
            o.addProperty("credits", credits);
            return o;
        } catch (Exception e) {
            removeOrderByOutTradeNo(outTradeNo);
            throw e;
        }
    }

    private void removeOrderByOutTradeNo(String outTradeNo) throws IOException {
        List<AssistantPayOrder> list = new ArrayList<>(Storage.loadAssistantPayOrders());
        Iterator<AssistantPayOrder> it = list.iterator();
        while (it.hasNext()) {
            AssistantPayOrder o = it.next();
            if (o != null && outTradeNo.equals(o.getOutTradeNo())) {
                it.remove();
                break;
            }
        }
        Storage.saveAssistantPayOrders(list);
    }

    public JsonObject getOrderForUser(String userKey, String outTradeNo) throws IOException {
        JsonObject o = new JsonObject();
        if (outTradeNo == null || outTradeNo.trim().isEmpty()) {
            o.addProperty("ok", false);
            o.addProperty("error", "missing outTradeNo");
            return o;
        }
        outTradeNo = outTradeNo.trim();
        for (AssistantPayOrder ord : Storage.loadAssistantPayOrders()) {
            if (ord == null || ord.getOutTradeNo() == null) {
                continue;
            }
            if (!outTradeNo.equals(ord.getOutTradeNo())) {
                continue;
            }
            if (!userKey.equals(ord.getUserKey())) {
                o.addProperty("ok", false);
                o.addProperty("error", "forbidden");
                return o;
            }
            o.addProperty("ok", true);
            o.addProperty("status", ord.getStatus());
            o.addProperty("outTradeNo", ord.getOutTradeNo());
            o.addProperty("credits", ord.getCredits());
            o.addProperty("amountFen", ord.getAmountFen());
            return o;
        }
        o.addProperty("ok", false);
        o.addProperty("error", "not found");
        return o;
    }

    /**
     * 处理支付通知：验签在外层完成；此处幂等入账。
     *
     * @return 应答 JSON 字符串（HTTP 200 正文）
     */
    public String applyNotifySuccess(String decryptedTransactionJson) throws IOException {
        JsonObject tx = new JsonParser().parse(decryptedTransactionJson).getAsJsonObject();
        String tradeState = tx.has("trade_state") && tx.get("trade_state").isJsonPrimitive()
                ? tx.get("trade_state").getAsString() : "";
        if (!"SUCCESS".equals(tradeState)) {
            return successNotifyBody();
        }
        String outTradeNo = tx.has("out_trade_no") ? tx.get("out_trade_no").getAsString() : "";
        String mchid = tx.has("mchid") ? tx.get("mchid").getAsString() : "";
        String transactionId = tx.has("transaction_id") ? tx.get("transaction_id").getAsString() : "";
        int payerTotal = 0;
        if (tx.has("amount") && tx.get("amount").isJsonObject()) {
            JsonObject amt = tx.getAsJsonObject("amount");
            if (amt.has("total") && amt.get("total").isJsonPrimitive()) {
                payerTotal = amt.get("total").getAsInt();
            } else if (amt.has("payer_total") && amt.get("payer_total").isJsonPrimitive()) {
                payerTotal = amt.get("payer_total").getAsInt();
            }
        }
        if (outTradeNo.isEmpty()) {
            throw new IllegalStateException("missing out_trade_no in notify");
        }
        synchronized (NOTIFY_LOCK) {
            List<AssistantPayOrder> list = new ArrayList<>(Storage.loadAssistantPayOrders());
            AssistantPayOrder target = null;
            for (AssistantPayOrder ord : list) {
                if (ord != null && outTradeNo.equals(ord.getOutTradeNo())) {
                    target = ord;
                    break;
                }
            }
            if (target == null) {
                return successNotifyBody();
            }
            if (AssistantPayOrder.STATUS_PAID.equals(target.getStatus())) {
                return successNotifyBody();
            }
            if (!AssistantConfig.wechatPayMchId().equals(mchid)) {
                throw new IllegalStateException("wechat notify mchid mismatch");
            }
            if (payerTotal != target.getAmountFen()) {
                throw new IllegalStateException("wechat notify amount mismatch: expect " + target.getAmountFen() + " got " + payerTotal);
            }
            quotaService.grantPaidCredits(target.getUserKey(), target.getCredits());
            target.setStatus(AssistantPayOrder.STATUS_PAID);
            target.setPaidAt(System.currentTimeMillis());
            target.setTransactionId(transactionId);
            Storage.saveAssistantPayOrders(list);
        }
        return successNotifyBody();
    }

    private static String successNotifyBody() {
        JsonObject o = new JsonObject();
        o.addProperty("code", "SUCCESS");
        o.addProperty("message", "成功");
        return o.toString();
    }
}
