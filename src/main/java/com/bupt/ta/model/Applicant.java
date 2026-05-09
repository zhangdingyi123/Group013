package com.bupt.ta.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 应聘者（助教申请人）实体
 */
public class Applicant {
    private String id;
    private String name;
    private String email;
    private String passwordHash;
    private String studentId;
    private String phone;             // 联系电话
    private List<String> skills;      // 技能标签，如 Java, Python, 监考
    private String resumePath;        // 简历文件路径（相对 data/resumes/）
    private long createdAt;

    public Applicant() {
        this.skills = new ArrayList<>();
    }

    public Applicant(String id, String name, String email, String passwordHash, String studentId) {
        this();
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.studentId = studentId;
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
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills != null ? skills : new ArrayList<>(); }
    public String getResumePath() { return resumePath; }
    public void setResumePath(String resumePath) { this.resumePath = resumePath; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
