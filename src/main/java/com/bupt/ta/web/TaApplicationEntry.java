package com.bupt.ta.web;

import com.bupt.ta.model.Application;
import com.bupt.ta.model.Job;

/** TA 工作台「我的申请」列表项：申请记录与对应岗位。 */
public class TaApplicationEntry {
    private final Application app;
    private final Job job;

    public TaApplicationEntry(Application app, Job job) {
        this.app = app;
        this.job = job;
    }

    public Application getApp() {
        return app;
    }

    public Job getJob() {
        return job;
    }
}
