package com.bupt.ta.model;

/**
 * 应聘者与课程组织者互为好友后可私信（与「已投递未撤销」二选一或并存）。
 */
public class FriendLink {
    private String applicantId;
    private String moduleOrganiserId;
    private long createdAt;

    public FriendLink() {}

    public FriendLink(String applicantId, String moduleOrganiserId, long createdAt) {
        this.applicantId = applicantId;
        this.moduleOrganiserId = moduleOrganiserId;
        this.createdAt = createdAt;
    }

    public String getApplicantId() { return applicantId; }
    public void setApplicantId(String applicantId) { this.applicantId = applicantId; }
    public String getModuleOrganiserId() { return moduleOrganiserId; }
    public void setModuleOrganiserId(String moduleOrganiserId) { this.moduleOrganiserId = moduleOrganiserId; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
