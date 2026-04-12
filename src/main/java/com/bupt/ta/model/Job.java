package com.bupt.ta.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 招聘岗位实体类。
 *
 * <p>由 {@link ModuleOrganiser}（课程组织者）创建，描述一个助教招聘岗位。
 * 支持三种岗位类型：
 * <ul>
 *   <li>{@code course_ta} — 课程助教（答疑、批改作业、上机辅导）</li>
 *   <li>{@code invigilation} — 监考</li>
 *   <li>{@code activity} — 活动协助</li>
 * </ul>
 * </p>
 *
 * <p>{@link #requiredSkills} 列表用于与 {@link Applicant#getSkills()} 进行
 * 技能匹配评分（由 Service 层的 MatchHelper 计算）。</p>
 *
 * <p>对应持久化文件：{@code data/jobs.json}</p>
 *
 * @author handmanhsker
 * @see Application
 * @see com.bupt.ta.storage.Storage#loadJobs()
 * @see com.bupt.ta.storage.Storage#saveJobs(java.util.List)
 */
public class Job {

    /** 岗位状态：招聘中 */
    public static final String STATUS_OPEN = "open";

    /** 岗位状态：已关闭 */
    public static final String STATUS_CLOSED = "closed";

    /** 唯一标识，UUID v4 格式 */
    private String id;

    /** 岗位名称，如「数据结构课程助教」 */
    private String title;

    /** 发布者（课程组织者）ID，关联 {@link ModuleOrganiser#getId()} */
    private String moduleOrganiserId;

    /** 岗位详细描述（职责、课时要求等） */
    private String description;

    /** 所需技能列表，如 ["Java 或 C++", "数据结构", "沟通表达"] */
    private List<String> requiredSkills;

    /** 岗位类型：course_ta / invigilation / activity */
    private String type;

    /** 当前状态：{@link #STATUS_OPEN} 或 {@link #STATUS_CLOSED} */
    private String status;

    /** 岗位创建时间（Unix 毫秒时间戳） */
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
