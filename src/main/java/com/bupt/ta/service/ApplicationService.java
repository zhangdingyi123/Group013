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
        if (list.stream().anyMatch(a -> applicantId.equals(a.getApplicantId()) && jobId.equals(a.getJobId()))) {
            return null; // already applied
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
}
