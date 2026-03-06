package com.bupt.ta.ui;

import com.bupt.ta.model.Applicant;
import com.bupt.ta.model.Application;
import com.bupt.ta.model.Job;
import com.bupt.ta.model.ModuleOrganiser;
import com.bupt.ta.service.ApplicantService;
import com.bupt.ta.service.ApplicationService;
import com.bupt.ta.service.JobService;
import com.bupt.ta.service.ModuleOrganiserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MOConsole {
    private final Scanner sc = new Scanner(System.in);
    private final ModuleOrganiserService moService;
    private final JobService jobService;
    private final ApplicationService applicationService;
    private final ApplicantService applicantService;
    private ModuleOrganiser currentMO;

    public MOConsole(ModuleOrganiserService moService, JobService jobService,
                     ApplicationService applicationService, ApplicantService applicantService) {
        this.moService = moService;
        this.jobService = jobService;
        this.applicationService = applicationService;
        this.applicantService = applicantService;
    }

    public void run() {
        while (true) {
            if (currentMO == null) {
                if (!loginOrRegister()) break;
            } else {
                if (!moMenu()) break;
            }
        }
    }

    private boolean loginOrRegister() {
        System.out.println("\n--- Module Organiser: Login / Register ---");
        System.out.println("1. Register");
        System.out.println("2. Login (by email)");
        System.out.println("0. Back");
        String choice = sc.nextLine().trim();
        if ("0".equals(choice)) return false;
        if ("1".equals(choice)) {
            System.out.print("Name: ");
            String name = sc.nextLine().trim();
            System.out.print("Email: ");
            String email = sc.nextLine().trim();
            currentMO = moService.create(name, email);
            System.out.println("MO account created. ID: " + currentMO.getId());
            return true;
        }
        if ("2".equals(choice)) {
            System.out.print("Email: ");
            String email = sc.nextLine().trim();
            currentMO = moService.findAll().stream()
                    .filter(m -> email.equalsIgnoreCase(m.getEmail()))
                    .findFirst().orElse(null);
            if (currentMO == null) {
                System.out.println("No MO with this email. Register first.");
                return true;
            }
            System.out.println("Logged in as " + currentMO.getName());
            return true;
        }
        return true;
    }

    private boolean moMenu() {
        System.out.println("\n--- MO Menu ---");
        System.out.println("1. Post a job");
        System.out.println("2. List my jobs");
        System.out.println("3. Select applicant for a job");
        System.out.println("4. Logout");
        System.out.println("0. Exit");
        String choice = sc.nextLine().trim();
        switch (choice) {
            case "0": return false;
            case "1": postJob(); break;
            case "2": listMyJobs(); break;
            case "3": selectApplicant(); break;
            case "4": currentMO = null; System.out.println("Logged out."); break;
            default: System.out.println("Invalid option.");
        }
        return true;
    }

    private void postJob() {
        System.out.print("Job title: ");
        String title = sc.nextLine().trim();
        System.out.print("Module code: ");
        String moduleCode = sc.nextLine().trim();
        System.out.print("Required skills (comma-separated): ");
        String line = sc.nextLine().trim();
        List<String> skills = new ArrayList<>();
        if (!line.isEmpty()) {
            for (String s : line.split(",")) skills.add(s.trim());
        }
        Job j = jobService.create(title, moduleCode, currentMO.getId(), skills);
        System.out.println("Job posted. ID: " + j.getId());
    }

    private void listMyJobs() {
        List<Job> myJobs = new ArrayList<>();
        for (Job j : jobService.findAll()) {
            if (currentMO.getId().equals(j.getMoId())) myJobs.add(j);
        }
        if (myJobs.isEmpty()) {
            System.out.println("You have no jobs.");
            return;
        }
        for (Job j : myJobs) {
            System.out.println("  [" + j.getId() + "] " + j.getTitle() + " (" + j.getModuleCode() + ") - " + j.getStatus());
        }
    }

    private void selectApplicant() {
        listMyJobs();
        System.out.print("Enter job ID: ");
        String jobId = sc.nextLine().trim();
        if (jobService.findById(jobId).isEmpty()) {
            System.out.println("Job not found.");
            return;
        }
        List<Application> apps = applicationService.findByJob(jobId);
        if (apps.isEmpty()) {
            System.out.println("No applications for this job.");
            return;
        }
        System.out.println("Applications:");
        for (Application a : apps) {
            String name = applicantService.findById(a.getApplicantId()).map(Applicant::getName).orElse(a.getApplicantId());
            System.out.println("  [" + a.getId() + "] " + name + " - " + a.getStatus());
        }
        System.out.print("Enter application ID to select: ");
        String appId = sc.nextLine().trim();
        applicationService.selectApplicant(appId);
        System.out.println("Applicant selected.");
    }
}
