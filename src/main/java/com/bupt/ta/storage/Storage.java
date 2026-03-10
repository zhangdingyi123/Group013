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
 * 基于 JSON 文件的持久化存储，不使用数据库。
 * 数据目录默认为项目下的 data/，可通过 setDataDir 或环境变量覆盖。
 */
public class Storage {
    private static final String DEFAULT_DATA_DIR = "data";
    private static final String APPLICANTS_FILE = "applicants.json";
    private static final String JOBS_FILE = "jobs.json";
    private static final String APPLICATIONS_FILE = "applications.json";
    private static final String MODULE_ORGANISERS_FILE = "module_organisers.json";
    private static final String ADMINS_FILE = "admins.json";
    private static final String RESUMES_DIR = "resumes";

    private static volatile String dataDir = DEFAULT_DATA_DIR;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public static void setDataDir(String dir) {
        dataDir = dir != null ? dir : DEFAULT_DATA_DIR;
    }

    public static String getDataDir() {
        return dataDir;
    }

    public static Path dataPath(String filename) {
        return Paths.get(dataDir, filename);
    }

    public static Path resumesPath() {
        return Paths.get(dataDir, RESUMES_DIR);
    }

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

    // ---------- Applicants ----------
    private static final Type APPLICANT_LIST_TYPE = new TypeToken<ArrayList<Applicant>>(){}.getType();
    public static List<Applicant> loadApplicants() throws IOException {
        return readList(dataPath(APPLICANTS_FILE), APPLICANT_LIST_TYPE);
    }
    public static void saveApplicants(List<Applicant> list) throws IOException {
        writeList(dataPath(APPLICANTS_FILE), list);
    }

    // ---------- Jobs ----------
    private static final Type JOB_LIST_TYPE = new TypeToken<ArrayList<Job>>(){}.getType();
    public static List<Job> loadJobs() throws IOException {
        return readList(dataPath(JOBS_FILE), JOB_LIST_TYPE);
    }
    public static void saveJobs(List<Job> list) throws IOException {
        writeList(dataPath(JOBS_FILE), list);
    }

    // ---------- Applications ----------
    private static final Type APPLICATION_LIST_TYPE = new TypeToken<ArrayList<Application>>(){}.getType();
    public static List<Application> loadApplications() throws IOException {
        return readList(dataPath(APPLICATIONS_FILE), APPLICATION_LIST_TYPE);
    }
    public static void saveApplications(List<Application> list) throws IOException {
        writeList(dataPath(APPLICATIONS_FILE), list);
    }

    // ---------- Module Organisers ----------
    private static final Type MO_LIST_TYPE = new TypeToken<ArrayList<ModuleOrganiser>>(){}.getType();
    public static List<ModuleOrganiser> loadModuleOrganisers() throws IOException {
        return readList(dataPath(MODULE_ORGANISERS_FILE), MO_LIST_TYPE);
    }
    public static void saveModuleOrganisers(List<ModuleOrganiser> list) throws IOException {
        writeList(dataPath(MODULE_ORGANISERS_FILE), list);
    }

    // ---------- Admins ----------
    private static final Type ADMIN_LIST_TYPE = new TypeToken<ArrayList<Admin>>(){}.getType();
    public static List<Admin> loadAdmins() throws IOException {
        return readList(dataPath(ADMINS_FILE), ADMIN_LIST_TYPE);
    }
    public static void saveAdmins(List<Admin> list) throws IOException {
        writeList(dataPath(ADMINS_FILE), list);
    }

    /**
     * 保存简历文本到 data/resumes/{applicantId}.txt
     */
    public static String saveResume(String applicantId, String content) throws IOException {
        ensureDataDir();
        String filename = applicantId + ".txt";
        Path path = resumesPath().resolve(filename);
        Files.write(path, content.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return filename;
    }

    private static final String[] ALLOWED_RESUME_EXT = { ".txt", ".pdf", ".doc", ".docx" };

    /**
     * 保存上传的简历文件（.txt / .pdf / .doc / .docx）到 data/resumes/{applicantId}.{ext}
     * @return 保存后的文件名，若扩展名不允许则返回 null
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
     * 仅对 .txt 返回文本内容；.pdf/.doc/.docx 返回 null（需通过下载获取）
     */
    public static String readResume(String resumePath) throws IOException {
        if (resumePath == null || !resumePath.toLowerCase().endsWith(".txt")) return null;
        Path path = resumesPath().resolve(resumePath);
        if (!Files.exists(path)) return null;
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    public static Path getResumeFilePath(String resumePath) {
        return resumePath != null ? resumesPath().resolve(resumePath) : null;
    }
}
