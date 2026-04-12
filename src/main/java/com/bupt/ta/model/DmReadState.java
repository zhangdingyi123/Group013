package com.bupt.ta.model;

/**
 * 私信会话的「已读游标」实体类。
 *
 * <p>以 (applicantId, moduleOrganiserId) 标识一个会话，
 * 分别记录双方最后阅读时间戳，用于计算各自的未读消息条数。</p>
 *
 * <p>计算未读数的逻辑示例（以申请人视角）：<br>
 * 未读数 = 对方发送的消息中 {@code sentAt > taLastReadAt} 的条数。</p>
 *
 * <p>对应持久化文件：{@code data/dm_read_states.json}</p>
 *
 * @author handmanhsker
 * @see DirectMessage
 * @see com.bupt.ta.storage.Storage#loadDmReadStates()
 * @see com.bupt.ta.storage.Storage#saveDmReadStates(java.util.List)
 */
public class DmReadState {

    /** 申请人 ID，关联 {@link Applicant#getId()} */
    private String applicantId;

    /** 课程组织者 ID，关联 {@link ModuleOrganiser#getId()} */
    private String moduleOrganiserId;

    /** 申请人已读至该时间戳（含）之前的对方消息（Unix 毫秒） */
    private long taLastReadAt;

    /** 课程组织者已读至该时间戳（含）之前的对方消息（Unix 毫秒） */
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
