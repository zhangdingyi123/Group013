package com.bupt.ta.model;

/**
 * 岗位申请记录
 */
public class Application {
    public static final String STATUS_PENDING = "pending";   // 待审核
    public static final String STATUS_ACCEPTED = "accepted"; // 已录用
    public static final String STATUS_REJECTED = "rejected"; // 已拒绝
    public static final String STATUS_CANCELLED = "cancelled"; // 已取消

    private String id;
    private String applicantId;
    private String jobId;
    private String status;
    private String note;             // 申请人备注
    private long appliedAt;

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
}
