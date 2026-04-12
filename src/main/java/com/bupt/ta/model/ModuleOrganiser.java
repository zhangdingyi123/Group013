package com.bupt.ta.model;

/**
 * 课程组织者（Module Organiser）实体类。
 *
 * <p>代表负责发布助教招聘岗位的教师/课程负责人角色。
 * 课程组织者可以创建 {@link Job}、审阅 {@link Application}、
 * 安排面试，并与 {@link Applicant} 进行私信沟通。</p>
 *
 * <p>对应持久化文件：{@code data/module_organisers.json}</p>
 *
 * @author handmanhsker
 * @see Job
 * @see com.bupt.ta.storage.Storage#loadModuleOrganisers()
 * @see com.bupt.ta.storage.Storage#saveModuleOrganisers(java.util.List)
 */
public class ModuleOrganiser {

    /** 唯一标识，UUID v4 格式 */
    private String id;

    /** 组织者姓名 */
    private String name;

    /** 登录邮箱（同时作为登录账号） */
    private String email;

    /** 密码的 SHA-256 哈希值（Base64 编码） */
    private String passwordHash;

    /** 所属院系或课程组 */
    private String department;

    /** 账号创建时间（Unix 毫秒时间戳） */
    private long createdAt;

    public ModuleOrganiser() {}

    public ModuleOrganiser(String id, String name, String email, String passwordHash, String department) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.department = department;
        this.createdAt = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
