package com.bupt.ta.service;

import com.bupt.ta.model.Applicant;
import com.bupt.ta.storage.Storage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ApplicantService {
    private static final String FILE = "applicants.json";
    private final Storage storage;

    public ApplicantService(Storage storage) {
        this.storage = storage;
    }

    public List<Applicant> findAll() {
        return storage.loadList(FILE, Applicant.class);
    }

    public Optional<Applicant> findById(String id) {
        return findAll().stream().filter(a -> id.equals(a.getId())).findFirst();
    }

    public Optional<Applicant> findByEmail(String email) {
        return findAll().stream().filter(a -> email.equalsIgnoreCase(a.getEmail())).findFirst();
    }

    public Applicant create(String name, String email) {
        Applicant a = new Applicant(UUID.randomUUID().toString(), name, email);
        save(a);
        return a;
    }

    public void save(Applicant applicant) {
        List<Applicant> list = findAll();
        list.removeIf(a -> a.getId().equals(applicant.getId()));
        list.add(applicant);
        try {
            storage.saveList(FILE, list);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save applicants", e);
        }
    }

    public void updateCv(String applicantId, String cvPath) {
        findById(applicantId).ifPresent(a -> {
            a.setCvPath(cvPath);
            save(a);
        });
    }

    public void updateSkills(String applicantId, List<String> skills) {
        findById(applicantId).ifPresent(a -> {
            a.setSkills(skills);
            save(a);
        });
    }
}
