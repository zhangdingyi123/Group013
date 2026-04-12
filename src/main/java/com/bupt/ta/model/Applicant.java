package com.bupt.ta.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 助教申请人（TA Applicant）实体类。
 *
 * <p>代表系统中的学生用户，可浏览岗位、投递申请、上传简历，
 * 以及与课程组织者（{@link ModuleOrganiser}）进行私信沟通。</p>
 *
 * <p>技能列表（{@link #skills}）用于与 {@link Job#getRequiredSkills()} 进行匹配评分。</p>
 *
 * <p>对应持久化文件：{@code data/applicants.json}</p>
 *
 * @author handmanhsker
 * @see com.bupt.ta.storage.Storage#loadApplicants()
 * @see com.bupt.ta.storage.Storage#saveApplicants(java.util.List)
 */
public class Applicant {

    /** 唯一标识，UUID v4 格式 */
    private String id;

    /** 申请人姓名 */
    private String name;

    /** 登录邮箱（同时作为登录账号） */
    private String email;

    /** 密码的 SHA-256 哈希值（Base64 编码） */
    private String passwordHash;

    /** 学号（BUPT 学生唯一编号） */
    private String studentId;

    /** 联系电话 */
    private String phone;

    /** 技能标签列表，如 ["Java", "Python", "数据结构"]，用于岗位匹配 */
    private List<String> skills;

    /** 简历文件路径（相对于 data/resumes/ 目录），如 "{id}.txt" 或 "{id}.pdf" */
    private String resumePath;

    /** 账号创建时间（Unix 毫秒时间戳） */
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
