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

    /** OpenAI 官方 Embeddings */
    public static final String DEFAULT_OPENAI_EMBEDDINGS_URL = "https://api.openai.com/v1/embeddings";
    public static final String DEFAULT_OPENAI_EMBEDDING_MODEL = "text-embedding-3-small";

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

    public static boolean semanticMatchEnabled() {
        String v = firstOf(env("MATCH_SEMANTIC_ENABLED"), prop("match.semantic.enabled"));
        if (v.isEmpty()) {
            return false;
        }
        v = v.trim().toLowerCase();
        return "1".equals(v) || "true".equals(v) || "yes".equals(v) || "on".equals(v);
    }

    /**
     * 是否启用“小助手提问范围”严格限制。
     * <p>开启时，服务端会在调用上游大模型前先做一次主题判定；若明显无关则直接返回固定拒绝话术。</p>
     * <p>默认开启，可通过环境变量 {@code ASSISTANT_STRICT_SCOPE=0} 或配置 {@code assistant.strict.scope=false} 关闭。</p>
     */
    public static boolean strictScopeEnabled() {
        String v = firstOf(env("ASSISTANT_STRICT_SCOPE"), prop("assistant.strict.scope"));
        if (v.isEmpty()) {
            return true;
        }
        v = v.trim().toLowerCase();
        return "1".equals(v) || "true".equals(v) || "yes".equals(v) || "on".equals(v);
    }

    /** 向量/LLM 匹配使用的提供方：未设置时跟随 {@link #defaultProvider()}。 */
    public static String semanticMatchProvider() {
        String p = firstOf(env("MATCH_SEMANTIC_PROVIDER"), prop("match.semantic.provider"));
        if (p == null || p.trim().isEmpty()) {
            return defaultProvider();
        }
        p = p.trim().toLowerCase();
        if (PROVIDER_KIMI.equals(p) || PROVIDER_QWEN.equals(p) || PROVIDER_OPENAI.equals(p)) {
            return p;
        }
        return defaultProvider();
    }

    /** OpenAI 兼容 embeddings url：可显式配置，或从 chat.completions.url 推断。 */
    private static String inferEmbeddingsUrlFromChatCompletionsUrl(String chatUrl) {
        if (chatUrl == null || chatUrl.trim().isEmpty()) {
            return "";
        }
        String u = chatUrl.trim();
        String needle = "/chat/completions";
        int idx = u.indexOf(needle);
        if (idx >= 0) {
            return u.substring(0, idx) + "/embeddings";
        }
        return "";
    }

    public static String embeddingsUrl(String provider) {
        if (PROVIDER_OPENAI.equals(provider)) {
            String configured = firstOf(env("OPENAI_EMBEDDINGS_URL"), prop("openai.embeddings.url"));
            if (!configured.isEmpty()) {
                return configured;
            }
            String inferred = inferEmbeddingsUrlFromChatCompletionsUrl(openaiBaseUrl());
            return !inferred.isEmpty() ? inferred : DEFAULT_OPENAI_EMBEDDINGS_URL;
        }
        if (PROVIDER_QWEN.equals(provider)) {
            String configured = firstOf(env("QWEN_EMBEDDINGS_URL"), prop("qwen.embeddings.url"));
            if (!configured.isEmpty()) {
                return configured;
            }
            return inferEmbeddingsUrlFromChatCompletionsUrl(qwenBaseUrl());
        }
        if (PROVIDER_KIMI.equals(provider)) {
            String configured = firstOf(env("KIMI_EMBEDDINGS_URL"), prop("kimi.embeddings.url"));
            if (!configured.isEmpty()) {
                return configured;
            }
            return inferEmbeddingsUrlFromChatCompletionsUrl(kimiBaseUrl());
        }
        return "";
    }

    public static String embeddingModel(String provider) {
        if (PROVIDER_OPENAI.equals(provider)) {
            return firstOf(env("OPENAI_EMBEDDING_MODEL"), prop("openai.embedding.model"), DEFAULT_OPENAI_EMBEDDING_MODEL);
        }
        if (PROVIDER_QWEN.equals(provider)) {
            return firstOf(env("QWEN_EMBEDDING_MODEL"), prop("qwen.embedding.model"));
        }
        if (PROVIDER_KIMI.equals(provider)) {
            return firstOf(env("KIMI_EMBEDDING_MODEL"), prop("kimi.embedding.model"));
        }
        return "";
    }

    public static String apiKeyForProvider(String provider) {
        if (PROVIDER_OPENAI.equals(provider)) {
            return openaiApiKey();
        }
        if (PROVIDER_QWEN.equals(provider)) {
            return qwenApiKey();
        }
        if (PROVIDER_KIMI.equals(provider)) {
            return kimiApiKey();
        }
        return "";
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

    // ---------------- 额度与付费（小助手） ----------------

    /**
     * 每个用户每月免费对话次数上限（超出后需使用付费点数）。
     * <p>环境变量：{@code ASSISTANT_MONTHLY_FREE_QUOTA}；配置键：{@code assistant.monthly.free.quota}。</p>
     */
    public static int monthlyFreeQuota() {
        String v = firstOf(env("ASSISTANT_MONTHLY_FREE_QUOTA"), prop("assistant.monthly.free.quota"));
        if (v.isEmpty()) {
            return 30;
        }
        try {
            int n = Integer.parseInt(v.trim());
            return Math.max(0, Math.min(100000, n));
        } catch (Exception ignored) {
            return 30;
        }
    }

    /**
     * 兑换码/充值码（模拟付费后的发码流程）。
     * <p>环境变量：{@code ASSISTANT_TOPUP_CODE}；配置键：{@code assistant.topup.code}。</p>
     */
    public static String topupCode() {
        return firstOf(env("ASSISTANT_TOPUP_CODE"), prop("assistant.topup.code"));
    }

    /**
     * 当免费额度用尽时的提示/付费引导（可选）。
     * <p>环境变量：{@code ASSISTANT_PAY_HINT}；配置键：{@code assistant.pay.hint}。</p>
     */
    public static String payHint() {
        return firstOf(env("ASSISTANT_PAY_HINT"), prop("assistant.pay.hint"));
    }

    /**
     * 微信支付 Native：公众号或小程序绑定的 AppID（Native 下单必填）。
     */
    public static String wechatPayAppId() {
        return firstOf(env("WECHAT_PAY_APPID"), prop("assistant.pay.wechat.appid"));
    }

    public static String wechatPayMchId() {
        return firstOf(env("WECHAT_PAY_MCHID"), prop("assistant.pay.wechat.mchid"));
    }

    /** 商户 API 证书序列号（证书管理页可见）。 */
    public static String wechatPayMerchantSerial() {
        return firstOf(env("WECHAT_PAY_MERCHANT_SERIAL"), prop("assistant.pay.wechat.merchant.serial"));
    }

    /** APIv3 密钥（32 字节，微信支付商户平台设置）。 */
    public static String wechatPayApiV3Key() {
        return firstOf(env("WECHAT_PAY_API_V3_KEY"), prop("assistant.pay.wechat.api.v3.key"));
    }

    /** 商户 API 私钥 PEM 文件绝对路径（apiclient_key.pem）。 */
    public static String wechatPayPrivateKeyPath() {
        return firstOf(env("WECHAT_PAY_PRIVATE_KEY_PATH"), prop("assistant.pay.wechat.private.key.path"));
    }

    /**
     * 支付结果通知 URL，须为公网 HTTPS，与商户平台配置的完全一致。
     * 例：https://your.domain/ta-recruitment/api/assistant/pay/wechat/notify
     */
    public static String wechatPayNotifyUrl() {
        return firstOf(env("WECHAT_PAY_NOTIFY_URL"), prop("assistant.pay.wechat.notify.url"));
    }

    /** 每点额度对应价格（分），如 10 表示 1 点 = 0.10 元。 */
    public static int wechatPayFenPerCredit() {
        String v = firstOf(env("WECHAT_PAY_FEN_PER_CREDIT"), prop("assistant.pay.wechat.fen.per.credit"));
        if (v.isEmpty()) {
            return 10;
        }
        try {
            int n = Integer.parseInt(v.trim());
            return Math.max(1, Math.min(100000, n));
        } catch (Exception ignored) {
            return 10;
        }
    }

    public static String wechatPayDescription() {
        String d = firstOf(env("WECHAT_PAY_DESCRIPTION"), prop("assistant.pay.wechat.description"));
        return d.isEmpty() ? "小助手额度充值" : d;
    }

    /**
     * 是否已配置微信 Native 扫码（私钥文件存在且必填项齐全）。
     */
    public static boolean wechatPayNativeReady() {
        String appid = wechatPayAppId();
        String mchid = wechatPayMchId();
        String serial = wechatPayMerchantSerial();
        String key = wechatPayApiV3Key();
        String pk = wechatPayPrivateKeyPath();
        String notify = wechatPayNotifyUrl();
        if (appid.isEmpty() || mchid.isEmpty() || serial.isEmpty() || key.length() != 32 || pk.isEmpty() || notify.isEmpty()) {
            return false;
        }
        try {
            return Files.isRegularFile(Paths.get(pk));
        } catch (Exception e) {
            return false;
        }
    }
}
