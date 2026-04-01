package com.bupt.ta.model;

/**
 * 好友请求：ta 表示应聘者发起，mo 表示课程组织者发起；接受后写入 {@link FriendLink}。
 */
public class FriendRequest {
    public static final String FROM_TA = "ta";
    public static final String FROM_MO = "mo";
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_ACCEPTED = "accepted";

    private String id;
    private String applicantId;
    private String moduleOrganiserId;
    private String fromRole;
    private String status;
    private long createdAt;

    public FriendRequest() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getApplicantId() { return applicantId; }
    public void setApplicantId(String applicantId) { this.applicantId = applicantId; }
    public String getModuleOrganiserId() { return moduleOrganiserId; }
    public void setModuleOrganiserId(String moduleOrganiserId) { this.moduleOrganiserId = moduleOrganiserId; }
    public String getFromRole() { return fromRole; }
    public void setFromRole(String fromRole) { this.fromRole = fromRole; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
