package com.bupt.ta.model;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 私信（Direct Message）实体类。
 *
 * <p>用于 {@link Applicant}（助教申请人）与 {@link ModuleOrganiser}（课程组织者）之间的
 * 一对一会话消息。会话以 (applicantId, moduleOrganiserId) 二元组标识。</p>
 *
 * <p>发送方角色由 {@link #senderRole} 标记：
 * <ul>
 *   <li>{@link #SENDER_TA} — 申请人发送</li>
 *   <li>{@link #SENDER_MO} — 课程组织者发送</li>
 * </ul>
 * </p>
 *
 * <p>对应持久化文件：{@code data/messages.json}</p>
 *
 * @author handmanhsker
 * @see DmReadState
 * @see com.bupt.ta.storage.Storage#loadMessages()
 * @see com.bupt.ta.storage.Storage#saveMessages(java.util.List)
 */
public class DirectMessage {

    /** 发送角色常量：助教申请人 */
    public static final String SENDER_TA = "ta";

    /** 发送角色常量：课程组织者 */
    public static final String SENDER_MO = "mo";

    /** 唯一标识，UUID v4 格式 */
    private String id;

    /** 申请人 ID，关联 {@link Applicant#getId()} */
    private String applicantId;

    /** 课程组织者 ID，关联 {@link ModuleOrganiser#getId()} */
    private String moduleOrganiserId;

    /** 发送方角色：{@link #SENDER_TA} 或 {@link #SENDER_MO} */
    private String senderRole;

    /** 消息正文 */
    private String body;

    /** 发送时间（Unix 毫秒时间戳） */
    private long sentAt;

    /** 关联岗位 ID（可选），便于追溯消息来源于哪个岗位 */
    private String jobId;

    public DirectMessage() {}

    public DirectMessage(String id, String applicantId, String moduleOrganiserId, String senderRole, String body, long sentAt) {
        this.id = id;
        this.applicantId = applicantId;
        this.moduleOrganiserId = moduleOrganiserId;
        this.senderRole = senderRole;
        this.body = body;
        this.sentAt = sentAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getApplicantId() { return applicantId; }
    public void setApplicantId(String applicantId) { this.applicantId = applicantId; }
    public String getModuleOrganiserId() { return moduleOrganiserId; }
    public void setModuleOrganiserId(String moduleOrganiserId) { this.moduleOrganiserId = moduleOrganiserId; }
    public String getSenderRole() { return senderRole; }
    public void setSenderRole(String senderRole) { this.senderRole = senderRole; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public long getSentAt() { return sentAt; }
    public void setSentAt(long sentAt) { this.sentAt = sentAt; }
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getSentAtText() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(sentAt));
    }
}
