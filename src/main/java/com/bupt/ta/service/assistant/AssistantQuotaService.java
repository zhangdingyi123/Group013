package com.bupt.ta.service.assistant;

import com.bupt.ta.model.AssistantUsage;
import com.bupt.ta.storage.Storage;

import java.io.IOException;
import java.time.YearMonth;
import java.util.List;

/**
 * 智能小助手额度：按月免费次数 + 付费点数（跨月累计）。
 */
public class AssistantQuotaService {

    public static final String CODE_QUOTA_EXCEEDED = "ASSISTANT_QUOTA_EXCEEDED";
    public static final String CODE_TOPUP_DISABLED = "ASSISTANT_TOPUP_DISABLED";
    public static final String CODE_TOPUP_INVALID = "ASSISTANT_TOPUP_INVALID";

    public static final class Status {
        public final String period;
        public final int freeLimit;
        public final int freeUsed;
        public final int freeRemaining;
        public final int paidCredits;

        public Status(String period, int freeLimit, int freeUsed, int freeRemaining, int paidCredits) {
            this.period = period;
            this.freeLimit = freeLimit;
            this.freeUsed = freeUsed;
            this.freeRemaining = freeRemaining;
            this.paidCredits = paidCredits;
        }
    }

    public static final class ConsumeResult {
        public final boolean ok;
        public final String code;      // ok=false 时
        public final String message;   // ok=false 时
        public final Status status;
        public final boolean usedPaid;

        private ConsumeResult(boolean ok, String code, String message, Status status, boolean usedPaid) {
            this.ok = ok;
            this.code = code;
            this.message = message;
            this.status = status;
            this.usedPaid = usedPaid;
        }

        public static ConsumeResult ok(Status status, boolean usedPaid) {
            return new ConsumeResult(true, "", "", status, usedPaid);
        }

        public static ConsumeResult fail(String code, String message, Status status) {
            return new ConsumeResult(false, code, message, status, false);
        }
    }

    private static String currentPeriod() {
        return YearMonth.now().toString(); // YYYY-MM
    }

    private static AssistantUsage findOrCreate(List<AssistantUsage> list, String userKey) {
        for (AssistantUsage u : list) {
            if (u != null && userKey.equals(u.getUserKey())) {
                return u;
            }
        }
        AssistantUsage created = new AssistantUsage(userKey);
        list.add(created);
        return created;
    }

    private static void normalizePeriod(AssistantUsage usage, String period) {
        if (usage.getPeriod() == null || usage.getPeriod().trim().isEmpty() || !period.equals(usage.getPeriod())) {
            usage.setPeriod(period);
            usage.setFreeUsed(0);
        }
    }

    public Status status(String userKey) throws IOException {
        int freeLimit = AssistantConfig.monthlyFreeQuota();
        String period = currentPeriod();
        List<AssistantUsage> list = Storage.loadAssistantUsage();
        AssistantUsage usage = findOrCreate(list, userKey);
        normalizePeriod(usage, period);
        int freeUsed = Math.max(0, usage.getFreeUsed());
        int remaining = Math.max(0, freeLimit - freeUsed);
        int paidCredits = Math.max(0, usage.getPaidCredits());
        return new Status(period, freeLimit, freeUsed, remaining, paidCredits);
    }

    /**
     * 尝试消耗一次额度：先扣免费次数，再扣付费点数。
     */
    public ConsumeResult tryConsume(String userKey) throws IOException {
        int freeLimit = AssistantConfig.monthlyFreeQuota();
        String period = currentPeriod();
        List<AssistantUsage> list = Storage.loadAssistantUsage();
        AssistantUsage usage = findOrCreate(list, userKey);
        normalizePeriod(usage, period);

        int freeUsed = Math.max(0, usage.getFreeUsed());
        int paidCredits = Math.max(0, usage.getPaidCredits());

        boolean usedPaid = false;
        if (freeUsed < freeLimit) {
            usage.setFreeUsed(freeUsed + 1);
        } else if (paidCredits > 0) {
            usage.setPaidCredits(paidCredits - 1);
            usedPaid = true;
        } else {
            Status st = new Status(period, freeLimit, freeUsed, 0, paidCredits);
            return ConsumeResult.fail(CODE_QUOTA_EXCEEDED, "assistant quota exceeded", st);
        }

        usage.setUpdatedAt(System.currentTimeMillis());
        Storage.saveAssistantUsage(list);

        int newFreeUsed = Math.max(0, usage.getFreeUsed());
        int newRemaining = Math.max(0, freeLimit - newFreeUsed);
        int newPaid = Math.max(0, usage.getPaidCredits());
        return ConsumeResult.ok(new Status(period, freeLimit, newFreeUsed, newRemaining, newPaid), usedPaid);
    }

    /**
     * 充值付费点数。由外部“付费/兑换码”流程触发（此处只做最小可用的服务端记账）。
     */
    public ConsumeResult topup(String userKey, String code, int credits) throws IOException {
        String expect = AssistantConfig.topupCode();
        if (expect == null || expect.trim().isEmpty()) {
            Status st = status(userKey);
            return ConsumeResult.fail(CODE_TOPUP_DISABLED, "topup disabled", st);
        }
        if (code == null || !expect.equals(code.trim())) {
            Status st = status(userKey);
            return ConsumeResult.fail(CODE_TOPUP_INVALID, "invalid topup code", st);
        }
        if (credits <= 0 || credits > 10000) {
            Status st = status(userKey);
            return ConsumeResult.fail(CODE_TOPUP_INVALID, "invalid credits", st);
        }

        List<AssistantUsage> list = Storage.loadAssistantUsage();
        AssistantUsage usage = findOrCreate(list, userKey);
        normalizePeriod(usage, currentPeriod());
        int cur = Math.max(0, usage.getPaidCredits());
        usage.setPaidCredits(cur + credits);
        usage.setUpdatedAt(System.currentTimeMillis());
        Storage.saveAssistantUsage(list);

        return ConsumeResult.ok(status(userKey), false);
    }

    /**
     * 支付回调成功后增加付费点数（不经兑换码）。
     */
    public void grantPaidCredits(String userKey, int credits) throws IOException {
        if (credits <= 0 || credits > 10000) {
            return;
        }
        List<AssistantUsage> list = Storage.loadAssistantUsage();
        AssistantUsage usage = findOrCreate(list, userKey);
        normalizePeriod(usage, currentPeriod());
        int cur = Math.max(0, usage.getPaidCredits());
        usage.setPaidCredits(cur + credits);
        usage.setUpdatedAt(System.currentTimeMillis());
        Storage.saveAssistantUsage(list);
    }

    /**
     * 在上游调用失败时进行一次简单退款：若之前消耗的是付费点数则加回 1；
     * 否则回退本周期免费已用次数 1（不小于 0）。
     */
    public void refundOnce(String userKey, boolean consumedPaid) throws IOException {
        List<AssistantUsage> list = Storage.loadAssistantUsage();
        AssistantUsage usage = findOrCreate(list, userKey);
        normalizePeriod(usage, currentPeriod());
        if (consumedPaid) {
            int cur = Math.max(0, usage.getPaidCredits());
            usage.setPaidCredits(cur + 1);
        } else {
            int cur = Math.max(0, usage.getFreeUsed());
            usage.setFreeUsed(Math.max(0, cur - 1));
        }
        usage.setUpdatedAt(System.currentTimeMillis());
        Storage.saveAssistantUsage(list);
    }
}
