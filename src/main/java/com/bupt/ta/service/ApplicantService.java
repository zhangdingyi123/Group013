package com.bupt.ta.service;

import com.bupt.ta.model.Applicant;
import com.bupt.ta.storage.Storage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ApplicantService {
    public List<Applicant> findAll() throws IOException {
        return Storage.loadApplicants();
    }

    public Optional<Applicant> findById(String id) throws IOException {
        return Storage.loadApplicants().stream().filter(a -> id.equals(a.getId())).findFirst();
    }

    public Optional<Applicant> findByEmail(String email) throws IOException {
        return Storage.loadApplicants().stream().filter(a -> email.equalsIgnoreCase(a.getEmail())).findFirst();
    }

    public Optional<Applicant> findByStudentId(String studentId) throws IOException {
        if (studentId == null || studentId.trim().isEmpty()) return Optional.empty();
        String sid = studentId.trim();
        return Storage.loadApplicants().stream().filter(a -> sid.equals(a.getStudentId())).findFirst();
    }

    public Applicant create(String name, String email, String passwordHash, String studentId) throws IOException {
        List<Applicant> list = Storage.loadApplicants();
        String sid = studentId != null ? studentId.trim() : "";
        if (!sid.isEmpty() && list.stream().anyMatch(a -> sid.equals(a.getStudentId()))) {
            return null; // 学号已注册过
        }
        Applicant a = new Applicant(UUID.randomUUID().toString(), name, email, passwordHash, sid);
        list.add(a);
        Storage.saveApplicants(list);
        return a;
    }

    public boolean update(Applicant applicant) throws IOException {
        List<Applicant> list = Storage.loadApplicants();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(applicant.getId())) {
                list.set(i, applicant);
                Storage.saveApplicants(list);
                return true;
            }
        }
        return false;
    }

    public String saveResume(String applicantId, String content) throws IOException {
        return Storage.saveResume(applicantId, content);
    }

    public String saveResumeFile(String applicantId, java.io.InputStream in, String originalFilename) throws IOException {
        return Storage.saveResumeFile(applicantId, in, originalFilename);
    }

    public String getResumeContent(String resumePath) throws IOException {
        return Storage.readResume(resumePath);
    }

    public boolean isResumeText(String resumePath) {
        return resumePath != null && resumePath.toLowerCase().endsWith(".txt");
    }
}
