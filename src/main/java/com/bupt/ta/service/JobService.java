package com.bupt.ta.service;

import com.bupt.ta.model.Job;
import com.bupt.ta.storage.Storage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class JobService {
    public List<Job> findAll() throws IOException {
        return Storage.loadJobs();
    }

    public List<Job> findOpen() throws IOException {
        return Storage.loadJobs().stream()
                .filter(j -> Job.STATUS_OPEN.equals(j.getStatus()))
                .collect(Collectors.toList());
    }

    public List<Job> findByModuleOrganiserId(String moId) throws IOException {
        return Storage.loadJobs().stream()
                .filter(j -> moId.equals(j.getModuleOrganiserId()))
                .collect(Collectors.toList());
    }

    public Optional<Job> findById(String id) throws IOException {
        return Storage.loadJobs().stream().filter(j -> id.equals(j.getId())).findFirst();
    }

    public Job create(String title, String moId, String description, String type, List<String> requiredSkills) throws IOException {
        List<Job> list = Storage.loadJobs();
        Job job = new Job(UUID.randomUUID().toString(), title, moId, description, type);
        if (requiredSkills != null) {
            job.setRequiredSkills(requiredSkills);
        }
        list.add(job);
        Storage.saveJobs(list);
        return job;
    }

    public boolean update(Job job) throws IOException {
        List<Job> list = Storage.loadJobs();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(job.getId())) {
                list.set(i, job);
                Storage.saveJobs(list);
                return true;
            }
        }
        return false;
    }
}
