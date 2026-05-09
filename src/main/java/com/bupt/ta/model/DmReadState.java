package com.bupt.ta.model;

/**
 * 应聘者与课程组织者会话的「读光标」，用于未读条数（持久化于 dm_read_states.json）。
 */
public class DmReadState {
    private String applicantId;
    private String moduleOrganiserId;
    /** 应聘者已读至该时间戳（含）之前的对方消息 */
    private long taLastReadAt;
    /** 招聘者已读至该时间戳（含）之前的对方消息 */
    private long moLastReadAt;

    public DmReadState() {}

    public String getApplicantId() { return applicantId; }
    public void setApplicantId(String applicantId) { this.applicantId = applicantId; }
    public String getModuleOrganiserId() { return moduleOrganiserId; }
    public void setModuleOrganiserId(String moduleOrganiserId) { this.moduleOrganiserId = moduleOrganiserId; }
    public long getTaLastReadAt() { return taLastReadAt; }
    public void setTaLastReadAt(long taLastReadAt) { this.taLastReadAt = taLastReadAt; }
    public long getMoLastReadAt() { return moLastReadAt; }
    public void setMoLastReadAt(long moLastReadAt) { this.moLastReadAt = moLastReadAt; }
}
