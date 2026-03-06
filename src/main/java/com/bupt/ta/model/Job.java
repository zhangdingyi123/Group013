package com.bupt.ta.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Job posted by Module Organiser.
 */
public class Job {
    private String id;
    private String title;
    private String moduleCode;
    private String moId;         // Module Organiser id
    private List<String> requiredSkills;
    private String status;       // OPEN, CLOSED

    public Job() {
        this.requiredSkills = new ArrayList<>();
        this.status = "OPEN";
    }

    public Job(String id, String title, String moduleCode, String moId) {
        this();
        this.id = id;
        this.title = title;
        this.moduleCode = moduleCode;
        this.moId = moId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getModuleCode() { return moduleCode; }
    public void setModuleCode(String moduleCode) { this.moduleCode = moduleCode; }
    public String getMoId() { return moId; }
    public void setMoId(String moId) { this.moId = moId; }
    public List<String> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(List<String> requiredSkills) { this.requiredSkills = requiredSkills != null ? requiredSkills : new ArrayList<>(); }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
