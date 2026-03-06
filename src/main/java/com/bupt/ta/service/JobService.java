package com.bupt.ta.service;

import com.bupt.ta.model.Job;
import com.bupt.ta.storage.Storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JobService {
    private static final String FILE = "jobs.json";
    private final Storage storage;

    public JobService(Storage storage) {
        this.storage = storage;
    }

    public List<Job> findAll() {
        return storage.loadList(FILE, Job.class);
    }

    public List<Job> findOpen() {
        List<Job> list = new ArrayList<>();
        for (Job j : findAll()) {
            if ("OPEN".equalsIgnoreCase(j.getStatus())) {
                list.add(j);
            }
        }
        return list;
    }

    public Optional<Job> findById(String id) {
        return findAll().stream().filter(j -> id.equals(j.getId())).findFirst();
    }

    public Job create(String title, String moduleCode, String moId, List<String> requiredSkills) {
        Job j = new Job(UUID.randomUUID().toString(), title, moduleCode, moId);
        if (requiredSkills != null) {
            j.setRequiredSkills(requiredSkills);
        }
        save(j);
        return j;
    }

    public void save(Job job) {
        List<Job> list = findAll();
        list.removeIf(j -> j.getId().equals(job.getId()));
        list.add(job);
        try {
            storage.saveList(FILE, list);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save jobs", e);
        }
    }

    public void closeJob(String jobId) {
        findById(jobId).ifPresent(j -> {
            j.setStatus("CLOSED");
            save(j);
        });
    }
}
