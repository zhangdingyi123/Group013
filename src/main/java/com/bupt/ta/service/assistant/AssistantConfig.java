package com.bupt.ta.service.assistant;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * 从 classpath 下的 {@code assistant.properties}、可选本地文件路径与环境变量读取配置。
 * 密钥类项优先级：环境变量优先于配置文件（便于部署时覆盖）。
 * <p>配置文件在首次读取时惰性加载，避免类在静态初始化阶段因 ClassLoader 上下文不完整而读不到资源。</p>
 * <p>若 classpath 仍读不到，可设置环境变量 {@code ASSISTANT_PROPERTIES_PATH} 或 JVM 参数
 * {@code -Dassistant.properties.path=} 指向 properties 文件的绝对路径。</p>
 */
public final class AssistantConfig {

    public static final String PROVIDER_KIMI = "kimi";
    public static final String PROVIDER_QWEN = "qwen";
    public static final String PROVIDER_OPENAI = "openai";

    /** OpenAI 兼容：Moonshot Kimi，默认 kimi-k2.5 */
    public static final String DEFAULT_KIMI_BASE = "https://api.moonshot.cn/v1/chat/completions";
    public static final String DEFAULT_KIMI_MODEL = "kimi-k2.5";

    /** OpenAI 兼容：阿里云 DashScope */
    public static final String DEFAULT_QWEN_BASE = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
    public static final String DEFAULT_QWEN_MODEL = "qwen-plus";

    /** OpenAI 官方 Chat Completions */
    public static final String DEFAULT_OPENAI_BASE = "https://api.openai.com/v1/chat/completions";
    public static final String DEFAULT_OPENAI_MODEL = "gpt-4o-mini";

    private static final String RESOURCE_NAME = "assistant.properties";
    private static volatile Properties filePropsCache;
    private static final Object FILE_PROPS_LOCK = new Object();

    private AssistantConfig() {}

    /** 外部配置文件路径：环境变量 {@code ASSISTANT_PROPERTIES_PATH} 或 {@code -Dassistant.properties.path=} */
    private static String pathOverrideString() {
        String pathOverride = env("ASSISTANT_PROPERTIES_PATH");
        if (pathOverride == null) {
            String sp = System.getProperty("assistant.properties.path");
            if (sp != null && !sp.trim().isEmpty()) {
                pathOverride = sp.trim();
            }
        }
        return (pathOverride != null && !pathOverride.trim().isEmpty()) ? pathOverride.trim() : null;
    }

    private static Properties loadPropertiesFromPath(Path fp) throws IOException {
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(fp);
                InputStreamReader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            p.load(r);
        }
        return p;
    }

    /** 合并时忽略值为空的键，避免外部 properties 里 {@code openai.api.key=} 盖掉 WAR 内已填的密钥 */
    private static void putAllNonEmpty(Properties target, Properties source) {
        for (String name : source.stringPropertyNames()) {
            String v = source.getProperty(name);
            if (v != null && !v.trim().isEmpty()) {
                target.setProperty(name, v.trim());
            }
        }
    }

    /** 与类同包、classpath 根目录下的资源；再尝试定义本类的 ClassLoader 与 TCCL */
    private static InputStream resourceStream(String name) {
        InputStream abs = AssistantConfig.class.getResourceAsStream("/" + name);
        if (abs != null) {
            return abs;
        }
        ClassLoader cl = AssistantConfig.class.getClassLoader();
        if (cl != null) {
            InputStream in = cl.getResourceAsStream(name);
            if (in != null) {
                return in;
            }
        }
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        if (tccl != null && tccl != cl) {
            return tccl.getResourceAsStream(name);
        }
        return null;
    }

    private static Properties loadFileProps() {
        Properties p = new Properties();
        String pathOverride = pathOverrideString();
        if (pathOverride != null) {
            Path fp = Paths.get(pathOverride);
            if (Files.isRegularFile(fp)) {
                try {
                    return loadPropertiesFromPath(fp);
                } catch (IOException ignored) {
                    p = new Properties();
                }
            }
        }
        InputStream in = resourceStream(RESOURCE_NAME);
        if (in == null) {
            return p;
        }
        try (InputStream closeMe = in) {
            p.load(new InputStreamReader(closeMe, StandardCharsets.UTF_8));
        } catch (IOException ignored) {
            // 读取失败则视为空配置
        }
        return p;
    }

    /**
     * 由 {@code ServletContextListener} 在 Web 应用启动时调用，从 WAR 内
     * {@code /WEB-INF/classes/assistant.properties} 注入配置，避免仅靠 ClassLoader 链在某些部署方式下读不到该文件。
     *
     * @param in 来自 {@code ServletContext.getResourceAsStream} 的字节流；由调用方关闭
     */
    public static void primeFromWarClasspathResource(InputStream in) throws IOException {
        if (in == null) {
            return;
        }
        Properties loaded = new Properties();
        loaded.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        synchronized (FILE_PROPS_LOCK) {
            filePropsCache = loaded;
        }
    }

    /**
     * 合并配置：优先使用 {@link #primeFromWarClasspathResource} 注入的 WAR 内属性，再用外部路径文件覆盖同名键。
     * 否则退回 {@link #loadFileProps()}（classpath / 路径二选一逻辑）。
     * <p>修复：仅返回 WAR 缓存时，会忽略 {@code ASSISTANT_PROPERTIES_PATH}，导致 OpenAI 等只写在外部文件中的密钥永远不生效。</p>
     */
    private static Properties fileProps() {
        String pathStr = pathOverrideString();
        Path pathFile = null;
        if (pathStr != null) {
            Path fp = Paths.get(pathStr);
            if (Files.isRegularFile(fp)) {
                pathFile = fp;
            }
        }
        synchronized (FILE_PROPS_LOCK) {
            Properties merged = new Properties();
            if (filePropsCache != null) {
                merged.putAll(filePropsCache);
            }
            if (pathFile != null) {
                try {
                    putAllNonEmpty(merged, loadPropertiesFromPath(pathFile));
                } catch (IOException ignored) {
                    // 外部文件读失败则仅依赖 WAR / 下方 fallback
                }
            }
            if (!merged.isEmpty()) {
                return merged;
            }
            if (filePropsCache == null) {
                filePropsCache = loadFileProps();
            }
            return filePropsCache;
        }
    }

    private static String env(String name) {
        String v = System.getenv(name);
        return (v != null && !v.trim().isEmpty()) ? v.trim() : null;
    }

    private static String prop(String key) {
        String v = fileProps().getProperty(key);
        return (v != null && !v.trim().isEmpty()) ? v.trim() : null;
    }

    /** 按顺序取第一个非空：环境名与属性键可混用 */
    private static String firstOf(String... candidates) {
        for (String s : candidates) {
            if (s != null && !s.trim().isEmpty()) {
                return s.trim();
            }
        }
        return "";
    }

    public static String kimiApiKey() {
        return firstOf(
                env("KIMI_API_KEY"),
                env("MOONSHOT_API_KEY"),
                prop("kimi.api.key"),
                prop("moonshot.api.key"));
    }

    public static String qwenApiKey() {
        return firstOf(
                env("QWEN_API_KEY"),
                env("DASHSCOPE_API_KEY"),
                prop("qwen.api.key"),
                prop("dashscope.api.key"));
    }

    public static String openaiApiKey() {
        return firstOf(
                env("OPENAI_API_KEY"),
                prop("openai.api.key"));
    }

    public static String kimiBaseUrl() {
        return firstOf(
                env("KIMI_CHAT_COMPLETIONS_URL"),
                prop("kimi.chat.completions.url"),
                DEFAULT_KIMI_BASE);
    }

    public static String kimiModel() {
        return firstOf(
                env("KIMI_MODEL"),
                prop("kimi.model"),
                DEFAULT_KIMI_MODEL);
    }

    public static String qwenBaseUrl() {
        return firstOf(
                env("QWEN_CHAT_COMPLETIONS_URL"),
                prop("qwen.chat.completions.url"),
                DEFAULT_QWEN_BASE);
    }

    public static String qwenModel() {
        return firstOf(
                env("QWEN_MODEL"),
                prop("qwen.model"),
                DEFAULT_QWEN_MODEL);
    }

    public static String openaiBaseUrl() {
        return firstOf(
                env("OPENAI_CHAT_COMPLETIONS_URL"),
                prop("openai.chat.completions.url"),
                DEFAULT_OPENAI_BASE);
    }

    public static String openaiModel() {
        return firstOf(
                env("OPENAI_MODEL"),
                prop("openai.model"),
                DEFAULT_OPENAI_MODEL);
    }

    /** 默认提供方：kimi / qwen / openai，未设置时优先已配置密钥的一方 */
    public static String defaultProvider() {
        String p = firstOf(env("ASSISTANT_DEFAULT_PROVIDER"), prop("assistant.default.provider"));
        if (!p.isEmpty()) {
            p = p.toLowerCase();
            if (PROVIDER_KIMI.equals(p) || PROVIDER_QWEN.equals(p) || PROVIDER_OPENAI.equals(p)) {
                return p;
            }
        }
        if (!kimiApiKey().isEmpty()) {
            return PROVIDER_KIMI;
        }
        if (!qwenApiKey().isEmpty()) {
            return PROVIDER_QWEN;
        }
        if (!openaiApiKey().isEmpty()) {
            return PROVIDER_OPENAI;
        }
        return PROVIDER_KIMI;
    }
}
