package com.bupt.ta.web;

import com.bupt.ta.service.*;
import com.bupt.ta.storage.Storage;

import javax.servlet.ServletContext;
import java.nio.file.Paths;

/**
 * 在 Servlet 应用中复用的存储与服务（单例，随应用启动初始化）。
 */
public final class WebApp {
    private static Storage storage;
    private static ApplicantService applicantService;
    private static JobService jobService;
    private static ApplicationService applicationService;
    private static ModuleOrganiserService moService;

    public static synchronized void init(ServletContext ctx) {
        if (storage != null) return;
        String dataDir = ctx.getInitParameter("dataDir");
        if (dataDir == null || dataDir.isEmpty()) {
            dataDir = Paths.get("data").toAbsolutePath().toString();
        }
        storage = new Storage(dataDir);
        applicantService = new ApplicantService(storage);
        jobService = new JobService(storage);
        applicationService = new ApplicationService(storage);
        moService = new ModuleOrganiserService(storage);
    }

    public static ApplicantService getApplicantService() { return applicantService; }
    public static JobService getJobService() { return jobService; }
    public static ApplicationService getApplicationService() { return applicationService; }
    public static ModuleOrganiserService getMoService() { return moService; }
    public static Storage getStorage() { return storage; }
}
