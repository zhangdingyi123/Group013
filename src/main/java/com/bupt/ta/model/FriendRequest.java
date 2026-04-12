package com.bupt.ta.model;

/**
 * 好友请求实体类。
 *
 * <p>任一方均可发起好友请求：
 * <ul>
 *   <li>{@link #FROM_TA} — 由申请人发起</li>
 *   <li>{@link #FROM_MO} — 由课程组织者发起</li>
 * </ul>
 * 对方接受后，系统自动写入一条 {@link FriendLink} 记录。</p>
 *
 * <p>状态只有两种：{@link #STATUS_PENDING}（待处理）和
 * {@link #STATUS_ACCEPTED}（已接受）。拒绝操作直接删除记录。</p>
 *
 * <p>对应持久化文件：{@code data/friend_requests.json}</p>
 *
 * @author handmanhsker
 * @see FriendLink
 * @see com.bupt.ta.storage.Storage#loadFriendRequests()
 * @see com.bupt.ta.storage.Storage#saveFriendRequests(java.util.List)
 */
public class FriendRequest {

    /** 发起方常量：助教申请人 */
    public static final String FROM_TA = "ta";

    /** 发起方常量：课程组织者 */
    public static final String FROM_MO = "mo";

    /** 请求状态：待处理 */
    public static final String STATUS_PENDING = "pending";

    /** 请求状态：已接受（将同时创建 {@link FriendLink}） */
    public static final String STATUS_ACCEPTED = "accepted";

    /** 唯一标识，UUID v4 格式 */
    private String id;

    /** 申请人 ID，关联 {@link Applicant#getId()} */
    private String applicantId;

    /** 课程组织者 ID，关联 {@link ModuleOrganiser#getId()} */
    private String moduleOrganiserId;

    /** 发起方角色：{@link #FROM_TA} 或 {@link #FROM_MO} */
    private String fromRole;

    /** 当前状态：{@link #STATUS_PENDING} 或 {@link #STATUS_ACCEPTED} */
    private String status;

    /** 请求创建时间（Unix 毫秒时间戳） */
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
