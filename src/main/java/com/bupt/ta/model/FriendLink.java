package com.bupt.ta.model;

/**
 * 好友关系实体类。
 *
 * <p>记录 {@link Applicant} 与 {@link ModuleOrganiser} 之间已建立的好友关系。
 * 好友关系建立后双方可进行私信（{@link DirectMessage}）沟通。</p>
 *
 * <p>好友关系通过 {@link FriendRequest} 请求并被接受后创建。
 * 与"已投递且未撤销"的权限可并存——即使未成为好友，
 * 在申请有效期内也可私信联系。</p>
 *
 * <p>对应持久化文件：{@code data/friend_links.json}</p>
 *
 * @author handmanhsker
 * @see FriendRequest
 * @see com.bupt.ta.storage.Storage#loadFriendLinks()
 * @see com.bupt.ta.storage.Storage#saveFriendLinks(java.util.List)
 */
public class FriendLink {

    /** 申请人 ID，关联 {@link Applicant#getId()} */
    private String applicantId;

    /** 课程组织者 ID，关联 {@link ModuleOrganiser#getId()} */
    private String moduleOrganiserId;

    /** 好友关系建立时间（Unix 毫秒时间戳） */
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
