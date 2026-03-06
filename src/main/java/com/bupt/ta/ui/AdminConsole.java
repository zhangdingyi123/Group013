package com.bupt.ta.ui;

import com.bupt.ta.model.Applicant;
import com.bupt.ta.model.Application;
import com.bupt.ta.service.ApplicantService;
import com.bupt.ta.service.ApplicationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin: check TA overall workload (number of selected assignments per TA).
 */
public class AdminConsole {
    private final ApplicantService applicantService;
    private final ApplicationService applicationService;

    public AdminConsole(ApplicantService applicantService, ApplicationService applicationService) {
        this.applicantService = applicantService;
        this.applicationService = applicationService;
    }

    public void run() {
        System.out.println("\n--- Admin: TA Workload ---");
        Map<String, Integer> workload = new HashMap<>();
        for (Application a : applicationService.findAll()) {
            if ("SELECTED".equalsIgnoreCase(a.getStatus())) {
                workload.merge(a.getApplicantId(), 1, Integer::sum);
            }
        }
        List<Applicant> applicants = applicantService.findAll();
        if (applicants.isEmpty()) {
            System.out.println("No applicants in the system.");
            return;
        }
        for (Applicant ta : applicants) {
            int count = workload.getOrDefault(ta.getId(), 0);
            System.out.println("  " + ta.getName() + " (" + ta.getEmail() + "): " + count + " assigned job(s)");
        }
    }
}
