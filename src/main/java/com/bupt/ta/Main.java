package com.bupt.ta;

import com.bupt.ta.service.ApplicantService;
import com.bupt.ta.service.ApplicationService;
import com.bupt.ta.service.JobService;
import com.bupt.ta.service.ModuleOrganiserService;
import com.bupt.ta.storage.Storage;
import com.bupt.ta.ui.AdminConsole;
import com.bupt.ta.ui.MOConsole;
import com.bupt.ta.ui.TAConsole;

import java.util.Scanner;

/**
 * TA Recruitment System - Stand-alone Java application.
 * Data stored in JSON text files under ./data (no database).
 */
public class Main {
    public static void main(String[] args) {
        Storage storage = new Storage();
        ApplicantService applicantService = new ApplicantService(storage);
        JobService jobService = new JobService(storage);
        ApplicationService applicationService = new ApplicationService(storage);
        ModuleOrganiserService moService = new ModuleOrganiserService(storage);

        TAConsole taConsole = new TAConsole(applicantService, jobService, applicationService);
        MOConsole moConsole = new MOConsole(moService, jobService, applicationService, applicantService);
        AdminConsole adminConsole = new AdminConsole(applicantService, applicationService);

        Scanner sc = new Scanner(System.in);
        System.out.println("TA Recruitment System (BUPT International School)");
        System.out.println("Data directory: " + storage.getDataDir());

        while (true) {
            System.out.println("\n--- Main Menu ---");
            System.out.println("1. TA (Teaching Assistant)");
            System.out.println("2. Module Organiser (MO)");
            System.out.println("3. Admin");
            System.out.println("0. Exit");
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "0":
                    System.out.println("Goodbye.");
                    return;
                case "1":
                    taConsole.run();
                    break;
                case "2":
                    moConsole.run();
                    break;
                case "3":
                    adminConsole.run();
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }
}
