package com.bupt.ta.service;

import com.bupt.ta.model.Application;
import com.bupt.ta.storage.Storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ApplicationService {
    private static final String FILE = "applications.json";
    private final Storage storage;

    public ApplicationService(Storage storage) {
        this.storage = storage;
    }

    public List<Application> findAll() {
        return storage.loadList(FILE, Application.class);
    }

    public Optional<Application> findById(String id) {
        return findAll().stream().filter(a -> id.equals(a.getId())).findFirst();
    }

    public List<Application> findByApplicant(String applicantId) {
        List<Application> list = new ArrayList<>();
        for (Application a : findAll()) {
            if (applicantId.equals(a.getApplicantId())) {
                list.add(a);
            }
        }
        return list;
    }

    public List<Application> findByJob(String jobId) {
        List<Application> list = new ArrayList<>();
        for (Application a : findAll()) {
            if (jobId.equals(a.getJobId())) {
                list.add(a);
            }
        }
        return list;
    }

    public Application apply(String applicantId, String jobId) {
        for (Application a : findAll()) {
            if (applicantId.equals(a.getApplicantId()) && jobId.equals(a.getJobId())) {
                return a; // already applied
            }
        }
        Application app = new Application(UUID.randomUUID().toString(), applicantId, jobId);
        save(app);
        return app;
    }

    public void save(Application application) {
        List<Application> list = findAll();
        list.removeIf(a -> a.getId().equals(application.getId()));
        list.add(application);
        try {
            storage.saveList(FILE, list);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save applications", e);
        }
    }

    public void selectApplicant(String applicationId) {
        findById(applicationId).ifPresent(app -> {
            app.setStatus("SELECTED");
            save(app);
        });
    }

    public void rejectApplicant(String applicationId) {
        findById(applicationId).ifPresent(app -> {
            app.setStatus("REJECTED");
            save(app);
        });
    }
}
