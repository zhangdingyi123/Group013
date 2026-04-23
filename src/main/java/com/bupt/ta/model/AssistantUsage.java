package com.bupt.ta.model;

/**
 * 智能小助手额度使用情况（基于 JSON 文件存储）。
 *
 * <p>说明：免费额度按月重置（period=YYYY-MM），付费额度为累计“点数”并可跨月使用。</p>
 */
public class AssistantUsage {
    private String userKey;     // applicant:{id} / anon:{sessionId}
    private String period;      // YYYY-MM
    private int freeUsed;       // 本周期内已用免费次数
    private int paidCredits;    // 剩余付费点数
    private long updatedAt;

    public AssistantUsage() {}

    public AssistantUsage(String userKey) {
        this.userKey = userKey;
        this.freeUsed = 0;
        this.paidCredits = 0;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public int getFreeUsed() {
        return freeUsed;
    }

    public void setFreeUsed(int freeUsed) {
        this.freeUsed = freeUsed;
    }

    public int getPaidCredits() {
        return paidCredits;
    }

    public void setPaidCredits(int paidCredits) {
        this.paidCredits = paidCredits;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
