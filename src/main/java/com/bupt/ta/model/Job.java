package com.bupt.ta.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 招聘岗位（课程助教/监考等）
 */
public class Job {
    public static final String STATUS_OPEN = "open";
    public static final String STATUS_CLOSED = "closed";

    private String id;
    private String title;             // 岗位名称，如「数据结构课程助教」
    private String moduleOrganiserId; // 课程组织者ID
    private String description;       // 岗位描述
    private List<String> requiredSkills; // 所需技能
    private String type;             // 类型：course_ta / invigilation / activity
    private String status;
    private long createdAt;

    public Job() {
        this.requiredSkills = new ArrayList<>();
        this.status = STATUS_OPEN;
    }

    public Job(String id, String title, String moduleOrganiserId, String description, String type) {
        this();
        this.id = id;
        this.title = title;
        this.moduleOrganiserId = moduleOrganiserId;
        this.description = description;
        this.type = type;
        this.createdAt = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getModuleOrganiserId() { return moduleOrganiserId; }
    public void setModuleOrganiserId(String moduleOrganiserId) { this.moduleOrganiserId = moduleOrganiserId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<String> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(List<String> requiredSkills) { this.requiredSkills = requiredSkills != null ? requiredSkills : new ArrayList<>(); }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
