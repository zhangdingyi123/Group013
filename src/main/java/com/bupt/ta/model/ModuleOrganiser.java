package com.bupt.ta.model;

/**
 * 课程组织者（发布岗位的角色）
 */
public class ModuleOrganiser {
    private String id;
    private String name;
    private String email;
    private String passwordHash;
    private String department;       // 院系/课程组
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
