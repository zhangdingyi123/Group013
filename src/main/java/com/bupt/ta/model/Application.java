package com.bupt.ta.model;

/**
 * 岗位申请记录实体类。
 *
 * <p>记录 {@link Applicant} 对 {@link Job} 的申请，包含完整的状态流转：
 * pending → interview → accepted/rejected，以及申请人主动取消（cancelled）。</p>
 *
 * <p>面试流程由 {@link ModuleOrganiser} 发起，设置面试时间和地点后，
 * 申请人可通过 {@link #interviewTaStatus} 字段确认/拒绝/请求改期。</p>
 *
 * <h3>状态流转图</h3>
 * <pre>
 *   pending ──→ interview ──→ accepted
 *     │              │
 *     │              └──→ rejected
 *     └──→ cancelled
 * </pre>
 *
 * <p>对应持久化文件：{@code data/applications.json}</p>
 *
 * @author handmanhsker
 * @see com.bupt.ta.storage.Storage#loadApplications()
 * @see com.bupt.ta.storage.Storage#saveApplications(java.util.List)
 */
public class Application {

    /** 申请状态：待审核 */
    public static final String STATUS_PENDING = "pending";
    /** 申请状态：待面试（含试讲） */
    public static final String STATUS_INTERVIEW = "interview";
    /** 申请状态：已录用 */
    public static final String STATUS_ACCEPTED = "accepted";
    /** 申请状态：已拒绝 */
    public static final String STATUS_REJECTED = "rejected";
    /** 申请状态：申请人已取消 */
    public static final String STATUS_CANCELLED = "cancelled";

    /** 面试反馈：待确认（与 {@link #STATUS_INTERVIEW} 配合使用） */
    public static final String TA_IV_PENDING = "pending";
    /** 面试反馈：已确认参加 */
    public static final String TA_IV_CONFIRMED = "confirmed";
    /** 面试反馈：拒绝参加 */
    public static final String TA_IV_DECLINED = "declined";
    /** 面试反馈：希望更换时间 */
    public static final String TA_IV_RESCHEDULE = "reschedule";

    /** 唯一标识，UUID v4 格式 */
    private String id;

    /** 申请人 ID，关联 {@link Applicant#getId()} */
    private String applicantId;

    /** 岗位 ID，关联 {@link Job#getId()} */
    private String jobId;

    /** 当前状态，取值为 STATUS_* 常量之一 */
    private String status;

    /** 申请人备注（投递时填写的附言） */
    private String note;

    /** 申请提交时间（Unix 毫秒时间戳） */
    private long appliedAt;
    /** 面试/试讲时间（毫秒时间戳，与 {@link #STATUS_INTERVIEW} 配合使用） */
    private long interviewAt;
    /** 地点或线上链接等说明 */
    private String interviewDetail;
    /** 应聘者是否已确认参加面试（旧数据兼容，逻辑上以 {@link #interviewTaStatus} 为准） */
    private boolean interviewConfirmed;
    /** pending / confirmed / declined / reschedule */
    private String interviewTaStatus;

    public Application() {
        this.status = STATUS_PENDING;
    }

    public Application(String id, String applicantId, String jobId) {
        this();
        this.id = id;
        this.applicantId = applicantId;
        this.jobId = jobId;
        this.appliedAt = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getApplicantId() { return applicantId; }
    public void setApplicantId(String applicantId) { this.applicantId = applicantId; }
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public long getAppliedAt() { return appliedAt; }
    public void setAppliedAt(long appliedAt) { this.appliedAt = appliedAt; }
    public long getInterviewAt() { return interviewAt; }
    public void setInterviewAt(long interviewAt) { this.interviewAt = interviewAt; }
    public String getInterviewDetail() { return interviewDetail; }
    public void setInterviewDetail(String interviewDetail) { this.interviewDetail = interviewDetail; }
    public boolean isInterviewConfirmed() { return interviewConfirmed; }
    public void setInterviewConfirmed(boolean interviewConfirmed) { this.interviewConfirmed = interviewConfirmed; }
    public String getInterviewTaStatus() {
        if (interviewTaStatus != null && !interviewTaStatus.isEmpty()) {
            return interviewTaStatus;
        }
        if (interviewConfirmed) {
            return TA_IV_CONFIRMED;
        }
        return TA_IV_PENDING;
    }
    public void setInterviewTaStatus(String interviewTaStatus) { this.interviewTaStatus = interviewTaStatus; }

    /** 清空面试相关字段（状态离开 interview 时调用） */
    public void clearInterviewFields() {
        this.interviewAt = 0L;
        this.interviewDetail = null;
        this.interviewConfirmed = false;
        this.interviewTaStatus = null;
    }
}
