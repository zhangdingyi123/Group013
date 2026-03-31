package com.bupt.ta.model;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 应聘者与课程组织者之间的私信
 */
public class DirectMessage {
    public static final String SENDER_TA = "ta";
    public static final String SENDER_MO = "mo";

    private String id;
    private String applicantId;
    private String moduleOrganiserId;
    /** {@link #SENDER_TA} 或 {@link #SENDER_MO} */
    private String senderRole;
    private String body;
    private long sentAt;
    /** 发起联系时关联的岗位（可选，便于追溯） */
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
