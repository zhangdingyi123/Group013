package com.bupt.ta.storage;

import com.bupt.ta.model.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 基于 JSON 文件的集中式持久化存储层（不依赖数据库）。
 *
 * <h3>设计思路</h3>
 * <ul>
 *   <li>所有实体以 JSON 数组形式存储在独立文件中（如 applicants.json、jobs.json 等）</li>
 *   <li>使用 Gson 进行序列化/反序列化，输出时启用 Pretty Printing 以便人工可读</li>
 *   <li>全部方法为 {@code static}，单例模式，全局共享</li>
 *   <li>使用 {@link ReentrantReadWriteLock} 保证多线程（Servlet 并发）下的读写安全</li>
 * </ul>
 *
 * <h3>数据目录</h3>
 * <p>默认为 {@code data/}（相对于 webapp 根目录），可通过 {@link #setDataDir(String)} 覆盖。
 * 运行时由 {@code AppListener}（Servlet 启动监听器）调用 setDataDir 初始化。</p>
 *
 * <h3>文件命名约定</h3>
 * <ul>
 *   <li>实体列表 → 复数蛇形命名：applicants.json、module_organisers.json 等</li>
 *   <li>简历文件 → data/resumes/{applicantId}.{txt|pdf|docx}</li>
 * </ul>
 *
 * @author handmanhsker
 */
public class Storage {
    // ==================== 文件名常量 ====================
    /** 默认数据目录（相对路径） */
    private static final String DEFAULT_DATA_DIR = "data";

    /** 助教申请人数据文件 */
    private static final String APPLICANTS_FILE = "applicants.json";
    /** 岗位数据文件 */
    private static final String JOBS_FILE = "jobs.json";
    /** 申请记录数据文件 */
    private static final String APPLICATIONS_FILE = "applications.json";
    /** 课程组织者数据文件 */
    private static final String MODULE_ORGANISERS_FILE = "module_organisers.json";
    /** 管理员数据文件 */
    private static final String ADMINS_FILE = "admins.json";
    /** 私信数据文件 */
    private static final String MESSAGES_FILE = "messages.json";
    /** 论坛主题帖数据文件 */
    private static final String FORUM_THREADS_FILE = "forum_threads.json";
    /** 论坛回帖数据文件 */
    private static final String FORUM_REPLIES_FILE = "forum_replies.json";
    /** 好友关系数据文件 */
    private static final String FRIEND_LINKS_FILE = "friend_links.json";
    /** 好友请求数据文件 */
    private static final String FRIEND_REQUESTS_FILE = "friend_requests.json";
    /** 私信已读状态数据文件 */
    private static final String DM_READ_STATES_FILE = "dm_read_states.json";
    /** 简历文件子目录名 */
    private static final String RESUMES_DIR = "resumes";

    // ==================== 核心组件 ====================
    /** 当前数据目录路径（volatile 保证多线程可见性） */
    private static volatile String dataDir = DEFAULT_DATA_DIR;
    /** Gson 实例（Pretty Printing），线程安全 */
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    /** 读写锁：读操作共享、写操作互斥，保证 Servlet 并发安全 */
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * 覆盖数据目录路径（通常由 AppListener 在启动时调用）。
     *
     * @param dir 数据目录绝对或相对路径；传 null 则恢复默认
     */
    public static void setDataDir(String dir) {
        dataDir = dir != null ? dir : DEFAULT_DATA_DIR;
    }

    /** @return 当前数据目录路径 */
    public static String getDataDir() {
        return dataDir;
    }

    /**
     * 构建数据文件的完整路径。
     *
     * @param filename 文件名，如 "applicants.json"
     * @return 数据目录下该文件的 Path
     */
    public static Path dataPath(String filename) {
        return Paths.get(dataDir, filename);
    }

    /** @return 简历文件存储目录的 Path（data/resumes/） */
    public static Path resumesPath() {
        return Paths.get(dataDir, RESUMES_DIR);
    }

    /**
     * 确保 data/ 和 data/resumes/ 目录存在，不存在则自动创建。
     *
     * @throws IOException 目录创建失败时抛出
     */
    private static void ensureDataDir() throws IOException {
        Path dir = Paths.get(dataDir);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        Path resumes = resumesPath();
        if (!Files.exists(resumes)) {
            Files.createDirectories(resumes);
        }
    }

    /**
     * 通用 JSON → List 反序列化方法（加读锁）。
     *
     * <p>文件不存在或为空时返回空列表而非 null，避免上层 NPE。</p>
     *
     * @param path JSON 文件路径
     * @param type Gson TypeToken 对应的泛型类型
     * @param <T>  列表元素类型
     * @return 反序列化后的列表（永不为 null）
     * @throws IOException 文件读取失败时抛出
     */
    private static <T> List<T> readList(Path path, Type type) throws IOException {
        lock.readLock().lock();
        try {
            if (!Files.exists(path)) {
                return new ArrayList<>();
            }
            String json = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            if (json.trim().isEmpty()) {
                return new ArrayList<>();
            }
            List<T> list = gson.fromJson(json, type);
            return list != null ? list : new ArrayList<>();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 通用 List → JSON 序列化方法（加写锁）。
     *
     * <p>写入前自动确保目录存在；使用 TRUNCATE_EXISTING 保证原子覆写。</p>
     *
     * @param path 目标 JSON 文件路径
     * @param list 待写入的列表
     * @param <T>  列表元素类型
     * @throws IOException 文件写入失败时抛出
     */
    private static <T> void writeList(Path path, List<T> list) throws IOException {
        ensureDataDir();
        lock.writeLock().lock();
        try {
            String json = gson.toJson(list);
            Files.write(path, json.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } finally {
            lock.writeLock().unlock();
        }
    }

    // ==================== Applicants（助教申请人）====================
    private static final Type APPLICANT_LIST_TYPE = new TypeToken<ArrayList<Applicant>>(){}.getType();
    public static List<Applicant> loadApplicants() throws IOException {
        return readList(dataPath(APPLICANTS_FILE), APPLICANT_LIST_TYPE);
    }
    public static void saveApplicants(List<Applicant> list) throws IOException {
        writeList(dataPath(APPLICANTS_FILE), list);
    }

    // ==================== Jobs（招聘岗位）====================
    private static final Type JOB_LIST_TYPE = new TypeToken<ArrayList<Job>>(){}.getType();
    public static List<Job> loadJobs() throws IOException {
        return readList(dataPath(JOBS_FILE), JOB_LIST_TYPE);
    }
    public static void saveJobs(List<Job> list) throws IOException {
        writeList(dataPath(JOBS_FILE), list);
    }

    // ==================== Applications（岗位申请）====================
    private static final Type APPLICATION_LIST_TYPE = new TypeToken<ArrayList<Application>>(){}.getType();
    public static List<Application> loadApplications() throws IOException {
        return readList(dataPath(APPLICATIONS_FILE), APPLICATION_LIST_TYPE);
    }
    public static void saveApplications(List<Application> list) throws IOException {
        writeList(dataPath(APPLICATIONS_FILE), list);
    }

    // ==================== Module Organisers（课程组织者）====================
    private static final Type MO_LIST_TYPE = new TypeToken<ArrayList<ModuleOrganiser>>(){}.getType();
    public static List<ModuleOrganiser> loadModuleOrganisers() throws IOException {
        return readList(dataPath(MODULE_ORGANISERS_FILE), MO_LIST_TYPE);
    }
    public static void saveModuleOrganisers(List<ModuleOrganiser> list) throws IOException {
        writeList(dataPath(MODULE_ORGANISERS_FILE), list);
    }

    // ==================== Admins（系统管理员）====================
    private static final Type ADMIN_LIST_TYPE = new TypeToken<ArrayList<Admin>>(){}.getType();
    public static List<Admin> loadAdmins() throws IOException {
        return readList(dataPath(ADMINS_FILE), ADMIN_LIST_TYPE);
    }
    public static void saveAdmins(List<Admin> list) throws IOException {
        writeList(dataPath(ADMINS_FILE), list);
    }

    // ==================== Direct Messages（私信）====================
    private static final Type MESSAGE_LIST_TYPE = new TypeToken<ArrayList<DirectMessage>>(){}.getType();
    public static List<DirectMessage> loadMessages() throws IOException {
        return readList(dataPath(MESSAGES_FILE), MESSAGE_LIST_TYPE);
    }
    public static void saveMessages(List<DirectMessage> list) throws IOException {
        writeList(dataPath(MESSAGES_FILE), list);
    }

    private static final Type DM_READ_STATE_LIST_TYPE = new TypeToken<ArrayList<DmReadState>>(){}.getType();
    public static List<DmReadState> loadDmReadStates() throws IOException {
        return readList(dataPath(DM_READ_STATES_FILE), DM_READ_STATE_LIST_TYPE);
    }
    public static void saveDmReadStates(List<DmReadState> list) throws IOException {
        writeList(dataPath(DM_READ_STATES_FILE), list);
    }

    // ==================== Forum（论坛帖子与回帖）====================
    private static final Type FORUM_THREAD_LIST_TYPE = new TypeToken<ArrayList<ForumThread>>(){}.getType();
    public static List<ForumThread> loadForumThreads() throws IOException {
        return readList(dataPath(FORUM_THREADS_FILE), FORUM_THREAD_LIST_TYPE);
    }
    public static void saveForumThreads(List<ForumThread> list) throws IOException {
        writeList(dataPath(FORUM_THREADS_FILE), list);
    }
    private static final Type FORUM_REPLY_LIST_TYPE = new TypeToken<ArrayList<ForumReply>>(){}.getType();
    public static List<ForumReply> loadForumReplies() throws IOException {
        return readList(dataPath(FORUM_REPLIES_FILE), FORUM_REPLY_LIST_TYPE);
    }
    public static void saveForumReplies(List<ForumReply> list) throws IOException {
        writeList(dataPath(FORUM_REPLIES_FILE), list);
    }

    // ==================== Friend Links & Requests（好友关系与请求）====================
    private static final Type FRIEND_LINK_LIST_TYPE = new TypeToken<ArrayList<FriendLink>>(){}.getType();
    private static final Type FRIEND_REQUEST_LIST_TYPE = new TypeToken<ArrayList<FriendRequest>>(){}.getType();

    public static List<FriendLink> loadFriendLinks() throws IOException {
        return readList(dataPath(FRIEND_LINKS_FILE), FRIEND_LINK_LIST_TYPE);
    }
    public static void saveFriendLinks(List<FriendLink> list) throws IOException {
        writeList(dataPath(FRIEND_LINKS_FILE), list);
    }
    public static List<FriendRequest> loadFriendRequests() throws IOException {
        return readList(dataPath(FRIEND_REQUESTS_FILE), FRIEND_REQUEST_LIST_TYPE);
    }
    public static void saveFriendRequests(List<FriendRequest> list) throws IOException {
        writeList(dataPath(FRIEND_REQUESTS_FILE), list);
    }

    // ==================== Resumes（简历文件存储）====================

    /**
     * 保存简历的纯文本内容到 data/resumes/{applicantId}.txt。
     *
     * @param applicantId 申请人 UUID
     * @param content     简历纯文本内容
     * @return 保存后的文件名（如 "xxx.txt"），可直接存入 {@link Applicant#setResumePath(String)}
     * @throws IOException 写入失败时抛出
     */
    public static String saveResume(String applicantId, String content) throws IOException {
        ensureDataDir();
        String filename = applicantId + ".txt";
        Path path = resumesPath().resolve(filename);
        Files.write(path, content.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return filename;
    }

    /** 允许上传的简历文件扩展名白名单 */
    private static final String[] ALLOWED_RESUME_EXT = { ".txt", ".pdf", ".doc", ".docx" };

    /**
     * 保存用户上传的简历文件（支持 .txt / .pdf / .doc / .docx）。
     *
     * <p>文件存储路径为 data/resumes/{applicantId}.{ext}。
     * 若上传文件扩展名不在白名单中，则自动降级为 .txt。</p>
     *
     * @param applicantId      申请人 UUID
     * @param in               上传文件的输入流
     * @param originalFilename 原始文件名（用于提取扩展名）
     * @return 保存后的文件名；若 originalFilename 为空则返回 null
     * @throws IOException 写入失败时抛出
     */
    public static String saveResumeFile(String applicantId, InputStream in, String originalFilename) throws IOException {
        if (originalFilename == null || originalFilename.trim().isEmpty()) return null;
        String ext = "";
        int i = originalFilename.lastIndexOf('.');
        if (i >= 0 && i < originalFilename.length() - 1) {
            ext = originalFilename.substring(i).toLowerCase();
        }
        boolean allowed = false;
        for (String e : ALLOWED_RESUME_EXT) {
            if (e.equals(ext)) { allowed = true; break; }
        }
        if (!allowed) ext = ".txt";
        ensureDataDir();
        String filename = applicantId + ext;
        Path path = resumesPath().resolve(filename);
        Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
        return filename;
    }

    /**
     * 读取 .txt 格式简历的纯文本内容。
     *
     * <p>仅支持 .txt 文件；.pdf / .doc / .docx 需通过
     * {@link #getResumeFilePath(String)} 获取路径后由 ResumeTextExtractor 解析。</p>
     *
     * @param resumePath 简历文件名（如 "xxx.txt"）
     * @return 文本内容；文件不存在或非 .txt 格式则返回 null
     * @throws IOException 读取失败时抛出
     */
    public static String readResume(String resumePath) throws IOException {
        if (resumePath == null || !resumePath.toLowerCase().endsWith(".txt")) return null;
        Path path = resumesPath().resolve(resumePath);
        if (!Files.exists(path)) return null;
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    /**
     * 获取简历文件在文件系统中的绝对/相对 Path（用于下载 Servlet）。
     *
     * @param resumePath 简历文件名（如 "xxx.pdf"）
     * @return 完整 Path；resumePath 为 null 时返回 null
     */
    public static Path getResumeFilePath(String resumePath) {
        return resumePath != null ? resumesPath().resolve(resumePath) : null;
    }
}
