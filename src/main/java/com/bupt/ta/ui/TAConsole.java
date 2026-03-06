package com.bupt.ta.ui;

import com.bupt.ta.model.Applicant;
import com.bupt.ta.model.Application;
import com.bupt.ta.model.Job;
import com.bupt.ta.service.ApplicantService;
import com.bupt.ta.service.ApplicationService;
import com.bupt.ta.service.JobService;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TAConsole {
    private final Scanner sc = new Scanner(System.in);
    private final ApplicantService applicantService;
    private final JobService jobService;
    private final ApplicationService applicationService;
    private Applicant currentApplicant;

    public TAConsole(ApplicantService applicantService, JobService jobService, ApplicationService applicationService) {
        this.applicantService = applicantService;
        this.jobService = jobService;
        this.applicationService = applicationService;
    }

    public void run() {
        while (true) {
            if (currentApplicant == null) {
                if (!loginOrRegister()) break;
            } else {
                if (!taMenu()) break;
            }
        }
    }

    private boolean loginOrRegister() {
        System.out.println("\n--- TA: Login / Register ---");
        System.out.println("1. Register (create profile)");
        System.out.println("2. Login (by email)");
        System.out.println("0. Back");
        String choice = sc.nextLine().trim();
        if ("0".equals(choice)) return false;
        if ("1".equals(choice)) {
            System.out.print("Name: ");
            String name = sc.nextLine().trim();
            System.out.print("Email: ");
            String email = sc.nextLine().trim();
            if (applicantService.findByEmail(email).isPresent()) {
                System.out.println("Email already registered. Use Login.");
                return true;
            }
            currentApplicant = applicantService.create(name, email);
            System.out.println("Profile created. ID: " + currentApplicant.getId());
            return true;
        }
        if ("2".equals(choice)) {
            System.out.print("Email: ");
            String email = sc.nextLine().trim();
            currentApplicant = applicantService.findByEmail(email).orElse(null);
            if (currentApplicant == null) {
                System.out.println("No applicant with this email. Register first.");
                return true;
            }
            System.out.println("Logged in as " + currentApplicant.getName());
            return true;
        }
        return true;
    }

    private boolean taMenu() {
        System.out.println("\n--- TA Menu ---");
        System.out.println("1. View/Edit my profile");
        System.out.println("2. Upload CV (set path)");
        System.out.println("3. Find available jobs");
        System.out.println("4. Apply for a job");
        System.out.println("5. Check my application status");
        System.out.println("6. Logout");
        System.out.println("0. Exit");
        String choice = sc.nextLine().trim();
        switch (choice) {
            case "0": return false;
            case "1": viewEditProfile(); break;
            case "2": uploadCv(); break;
            case "3": findJobs(); break;
            case "4": applyForJob(); break;
            case "5": checkStatus(); break;
            case "6": currentApplicant = null; System.out.println("Logged out."); break;
            default: System.out.println("Invalid option.");
        }
        return true;
    }

    private void viewEditProfile() {
        System.out.println("Profile: " + currentApplicant.getName() + " | " + currentApplicant.getEmail());
        System.out.println("CV path: " + (currentApplicant.getCvPath() != null ? currentApplicant.getCvPath() : "(not set)"));
        System.out.println("Skills: " + currentApplicant.getSkills());
        System.out.print("Update skills? (comma-separated, or Enter to skip): ");
        String line = sc.nextLine().trim();
        if (!line.isEmpty()) {
            List<String> skills = new ArrayList<>();
            for (String s : line.split(",")) skills.add(s.trim());
            applicantService.updateSkills(currentApplicant.getId(), skills);
            currentApplicant = applicantService.findById(currentApplicant.getId()).orElse(currentApplicant);
            System.out.println("Skills updated.");
        }
    }

    private void uploadCv() {
        System.out.print("Enter path to your CV file (e.g. cv.txt): ");
        String path = sc.nextLine().trim();
        if (path.isEmpty()) return;
        applicantService.updateCv(currentApplicant.getId(), path);
        currentApplicant = applicantService.findById(currentApplicant.getId()).orElse(currentApplicant);
        System.out.println("CV path saved.");
    }

    private void findJobs() {
        List<Job> open = jobService.findOpen();
        if (open.isEmpty()) {
            System.out.println("No open jobs at the moment.");
            return;
        }
        System.out.println("Available jobs:");
        for (Job j : open) {
            System.out.println("  [" + j.getId() + "] " + j.getTitle() + " (" + j.getModuleCode() + ") skills: " + j.getRequiredSkills());
        }
    }

    private void applyForJob() {
        List<Job> open = jobService.findOpen();
        if (open.isEmpty()) {
            System.out.println("No open jobs.");
            return;
        }
        System.out.print("Enter job ID to apply: ");
        String jobId = sc.nextLine().trim();
        if (jobService.findById(jobId).isEmpty()) {
            System.out.println("Job not found.");
            return;
        }
        applicationService.apply(currentApplicant.getId(), jobId);
        System.out.println("Application submitted.");
    }

    private void checkStatus() {
        List<Application> list = applicationService.findByApplicant(currentApplicant.getId());
        if (list.isEmpty()) {
            System.out.println("No applications yet.");
            return;
        }
        for (Application a : list) {
            Job j = jobService.findById(a.getJobId()).orElse(null);
            String title = j != null ? j.getTitle() : a.getJobId();
            System.out.println("  " + title + " -> " + a.getStatus());
        }
    }
}
