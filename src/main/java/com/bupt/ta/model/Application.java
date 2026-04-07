package com.bupt.ta.model;

/**
 * 岗位申请记录
 */
public class Application {
    public static final String STATUS_PENDING = "pending";   // 待审核
    public static final String STATUS_INTERVIEW = "interview"; // 待面试（含试讲）
    public static final String STATUS_ACCEPTED = "accepted"; // 已录用
    public static final String STATUS_REJECTED = "rejected"; // 已拒绝
    public static final String STATUS_CANCELLED = "cancelled"; // 已取消

    /** 应聘者对面试安排的反馈（与 {@link #STATUS_INTERVIEW} 配合） */
    public static final String TA_IV_PENDING = "pending";       // 待确认
    public static final String TA_IV_CONFIRMED = "confirmed";   // 已确认参加
    public static final String TA_IV_DECLINED = "declined";     // 拒绝参加
    public static final String TA_IV_RESCHEDULE = "reschedule"; // 希望更换时间

    private String id;
    private String applicantId;
    private String jobId;
    private String status;
    private String note;             // 申请人备注
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
