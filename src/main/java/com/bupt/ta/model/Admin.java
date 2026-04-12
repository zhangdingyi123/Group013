package com.bupt.ta.model;

/**
 * 系统管理员实体类。
 *
 * <p>管理员拥有全局权限，可查看所有助教的工作负荷分布、
 * 进行岗位调配（转移录用）以及查看工作量统计（均值、最大值、最小值）。</p>
 *
 * <p>对应持久化文件：{@code data/admins.json}</p>
 *
 * @author handmanhsker
 * @see com.bupt.ta.storage.Storage#loadAdmins()
 * @see com.bupt.ta.storage.Storage#saveAdmins(java.util.List)
 */
public class Admin {

    /** 唯一标识，UUID v4 格式 */
    private String id;

    /** 管理员姓名 */
    private String name;

    /** 登录邮箱（同时作为登录账号） */
    private String email;

    /** 密码的 SHA-256 哈希值（Base64 编码） */
    private String passwordHash;

    /** 账号创建时间（Unix 毫秒时间戳） */
    private long createdAt;

    /** Gson 反序列化所需的无参构造 */
    public Admin() {}

    /**
     * 创建管理员并自动记录当前时间为 createdAt。
     *
     * @param id           UUID v4
     * @param name         姓名
     * @param email        登录邮箱
     * @param passwordHash SHA-256 哈希（Base64）
     */
    public Admin(String id, String name, String email, String passwordHash) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = System.currentTimeMillis();
    }

    /** @return 唯一标识 */
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    /** @return 管理员姓名 */
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    /** @return 登录邮箱 */
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    /** @return 密码哈希 */
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    /** @return 创建时间戳（毫秒） */
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
