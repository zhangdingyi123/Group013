package com.bupt.ta.model;

/**
 * TA application for a job.
 */
public class Application {
    private String id;
    private String applicantId;
    private String jobId;
    private String status;   // PENDING, SELECTED, REJECTED

    public Application() {
        this.status = "PENDING";
    }

    public Application(String id, String applicantId, String jobId) {
        this();
        this.id = id;
        this.applicantId = applicantId;
        this.jobId = jobId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getApplicantId() { return applicantId; }
    public void setApplicantId(String applicantId) { this.applicantId = applicantId; }
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
