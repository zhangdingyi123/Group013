package com.bupt.ta.service.assistant;

/**
 * 从环境变量读取 Kimi（Moonshot）与通义千问（DashScope 兼容接口）配置。
 * 部署前请设置 KIMI_API_KEY 与/或 QWEN_API_KEY（或 DASHSCOPE_API_KEY）。
 */
public final class AssistantConfig {

    public static final String PROVIDER_KIMI = "kimi";
    public static final String PROVIDER_QWEN = "qwen";

    /** OpenAI 兼容：Moonshot Kimi，默认 kimi-k2.5 */
    public static final String DEFAULT_KIMI_BASE = "https://api.moonshot.cn/v1/chat/completions";
    public static final String DEFAULT_KIMI_MODEL = "kimi-k2.5";

    /** OpenAI 兼容：阿里云 DashScope */
    public static final String DEFAULT_QWEN_BASE = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
    public static final String DEFAULT_QWEN_MODEL = "qwen-plus";

    private AssistantConfig() {}

    public static String kimiApiKey() {
        String k = firstNonEmpty(System.getenv("KIMI_API_KEY"), System.getenv("MOONSHOT_API_KEY"));
        return k != null ? k.trim() : "";
    }

    public static String qwenApiKey() {
        String k = firstNonEmpty(System.getenv("QWEN_API_KEY"), System.getenv("DASHSCOPE_API_KEY"));
        return k != null ? k.trim() : "";
    }

    public static String kimiBaseUrl() {
        String u = System.getenv("KIMI_CHAT_COMPLETIONS_URL");
        return (u != null && !u.trim().isEmpty()) ? u.trim() : DEFAULT_KIMI_BASE;
    }

    public static String kimiModel() {
        String m = System.getenv("KIMI_MODEL");
        return (m != null && !m.trim().isEmpty()) ? m.trim() : DEFAULT_KIMI_MODEL;
    }

    public static String qwenBaseUrl() {
        String u = System.getenv("QWEN_CHAT_COMPLETIONS_URL");
        return (u != null && !u.trim().isEmpty()) ? u.trim() : DEFAULT_QWEN_BASE;
    }

    public static String qwenModel() {
        String m = System.getenv("QWEN_MODEL");
        return (m != null && !m.trim().isEmpty()) ? m.trim() : DEFAULT_QWEN_MODEL;
    }

    /** 默认提供方：kimi / qwen，未设置时优先已配置密钥的一方 */
    public static String defaultProvider() {
        String p = System.getenv("ASSISTANT_DEFAULT_PROVIDER");
        if (p != null) {
            p = p.trim().toLowerCase();
            if (PROVIDER_KIMI.equals(p) || PROVIDER_QWEN.equals(p)) {
                return p;
            }
        }
        if (!kimiApiKey().isEmpty()) {
            return PROVIDER_KIMI;
        }
        if (!qwenApiKey().isEmpty()) {
            return PROVIDER_QWEN;
        }
        return PROVIDER_KIMI;
    }

    private static String firstNonEmpty(String a, String b) {
        if (a != null && !a.trim().isEmpty()) {
            return a;
        }
        if (b != null && !b.trim().isEmpty()) {
            return b;
        }
        return null;
    }
}
