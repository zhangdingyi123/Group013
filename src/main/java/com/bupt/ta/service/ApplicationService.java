package com.bupt.ta.service;

import com.bupt.ta.model.Application;
import com.bupt.ta.model.Applicant;
import com.bupt.ta.model.Job;
import com.bupt.ta.storage.Storage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class ApplicationService {
    private final ApplicantService applicantService = new ApplicantService();
    private final JobService jobService = new JobService();

    public List<Application> findAll() throws IOException {
        return Storage.loadApplications();
    }

    public List<Application> findByApplicantId(String applicantId) throws IOException {
        return Storage.loadApplications().stream()
                .filter(a -> applicantId.equals(a.getApplicantId()))
                .collect(Collectors.toList());
    }

    public List<Application> findByJobId(String jobId) throws IOException {
        return Storage.loadApplications().stream()
                .filter(a -> jobId.equals(a.getJobId()))
                .collect(Collectors.toList());
    }

    public Optional<Application> findById(String id) throws IOException {
        return Storage.loadApplications().stream().filter(a -> id.equals(a.getId())).findFirst();
    }

    public Optional<Application> findByApplicantAndJob(String applicantId, String jobId) throws IOException {
        return Storage.loadApplications().stream()
                .filter(a -> applicantId.equals(a.getApplicantId()) && jobId.equals(a.getJobId()))
                .findFirst();
    }

    public Application apply(String applicantId, String jobId, String note) throws IOException {
        List<Application> list = Storage.loadApplications();
        if (list.stream().anyMatch(a -> applicantId.equals(a.getApplicantId()) && jobId.equals(a.getJobId())
                && !Application.STATUS_CANCELLED.equals(a.getStatus()))) {
            return null; // 已存在未取消的申请；管理员强行取消后可再次申请
        }
        Optional<Job> job = jobService.findById(jobId);
        if (job.isEmpty() || !Job.STATUS_OPEN.equals(job.get().getStatus())) {
            return null;
        }
        Application app = new Application(UUID.randomUUID().toString(), applicantId, jobId);
        app.setNote(note);
        list.add(app);
        Storage.saveApplications(list);
        return app;
    }

    public boolean updateStatus(String applicationId, String status) throws IOException {
        List<Application> list = Storage.loadApplications();
        for (Application a : list) {
            if (a.getId().equals(applicationId)) {
                a.setStatus(status);
                if (!Application.STATUS_INTERVIEW.equals(status)) {
                    a.clearInterviewFields();
                }
                Storage.saveApplications(list);
                return true;
            }
        }
        return false;
    }

    /** MO 将待审核申请设为待面试，或对「拒绝/更换时间」的申请重新填写时间地点 */
    public boolean scheduleInterview(String applicationId, long interviewAtMs, String interviewDetail) throws IOException {
        if (interviewAtMs <= 0) {
            return false;
        }
        String detail = interviewDetail != null ? interviewDetail.trim() : "";
        if (detail.isEmpty()) {
            return false;
        }
        List<Application> list = Storage.loadApplications();
        for (Application a : list) {
            if (!a.getId().equals(applicationId)) {
                continue;
            }
            if (Application.STATUS_PENDING.equals(a.getStatus())) {
                a.setStatus(Application.STATUS_INTERVIEW);
                a.setInterviewAt(interviewAtMs);
                a.setInterviewDetail(detail);
                a.setInterviewTaStatus(Application.TA_IV_PENDING);
                a.setInterviewConfirmed(false);
                Storage.saveApplications(list);
                return true;
            }
            if (Application.STATUS_INTERVIEW.equals(a.getStatus())) {
                String ts = a.getInterviewTaStatus();
                if (Application.TA_IV_DECLINED.equals(ts) || Application.TA_IV_RESCHEDULE.equals(ts)) {
                    a.setInterviewAt(interviewAtMs);
                    a.setInterviewDetail(detail);
                    a.setInterviewTaStatus(Application.TA_IV_PENDING);
                    a.setInterviewConfirmed(false);
                    Storage.saveApplications(list);
                    return true;
                }
            }
        }
        return false;
    }

    /** TA 确认参加 */
    public boolean confirmInterviewByApplicant(String applicationId, String applicantId) throws IOException {
        return setInterviewTaResponse(applicationId, applicantId, Application.TA_IV_CONFIRMED);
    }

    /** TA 拒绝参加面试 */
    public boolean declineInterviewByApplicant(String applicationId, String applicantId) throws IOException {
        return setInterviewTaResponse(applicationId, applicantId, Application.TA_IV_DECLINED);
    }

    /** TA 希望更换时间 */
    public boolean requestRescheduleByApplicant(String applicationId, String applicantId) throws IOException {
        return setInterviewTaResponse(applicationId, applicantId, Application.TA_IV_RESCHEDULE);
    }

    private boolean setInterviewTaResponse(String applicationId, String applicantId, String newStatus) throws IOException {
        List<Application> list = Storage.loadApplications();
        for (Application a : list) {
            if (a.getId().equals(applicationId) && applicantId.equals(a.getApplicantId())
                    && Application.STATUS_INTERVIEW.equals(a.getStatus())) {
                if (!Application.TA_IV_PENDING.equals(a.getInterviewTaStatus())) {
                    return false;
                }
                a.setInterviewTaStatus(newStatus);
                a.setInterviewConfirmed(Application.TA_IV_CONFIRMED.equals(newStatus));
                Storage.saveApplications(list);
                return true;
            }
        }
        return false;
    }

    public boolean update(Application application) throws IOException {
        List<Application> list = Storage.loadApplications();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(application.getId())) {
                list.set(i, application);
                Storage.saveApplications(list);
                return true;
            }
        }
        return false;
    }

    /** 获取某应聘者在某岗位上的申请（用于展示状态） */
    public Optional<Application> getApplication(String applicantId, String jobId) throws IOException {
        return findByApplicantAndJob(applicantId, jobId);
    }

    /**
     * 管理员将已录用记录转移至另一位助教：保持岗位仍为已录用，仅变更 applicantId。
     */
    public boolean transferAcceptedHire(String applicationId, String newApplicantId) throws IOException {
        if (applicationId == null || newApplicantId == null) {
            return false;
        }
        String targetId = newApplicantId.trim();
        if (targetId.isEmpty()) {
            return false;
        }
        Optional<Application> opt = findById(applicationId.trim());
        if (opt.isEmpty()) {
            return false;
        }
        Application app = opt.get();
        if (!Application.STATUS_ACCEPTED.equals(app.getStatus())) {
            return false;
        }
        if (targetId.equals(app.getApplicantId())) {
            return false;
        }
        if (applicantService.findById(targetId).isEmpty()) {
            return false;
        }
        for (Application other : findByJobId(app.getJobId())) {
            if (other.getId().equals(app.getId())) {
                continue;
            }
            if (!targetId.equals(other.getApplicantId())) {
                continue;
            }
            if (Application.STATUS_ACCEPTED.equals(other.getStatus())) {
                return false;
            }
            String st = other.getStatus();
            if (!Application.STATUS_CANCELLED.equals(st) && !Application.STATUS_REJECTED.equals(st)) {
                return false;
            }
        }
        app.setApplicantId(targetId);
        app.clearInterviewFields();
        return update(app);
    }
}
