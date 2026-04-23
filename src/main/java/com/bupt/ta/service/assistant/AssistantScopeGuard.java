package com.bupt.ta.service.assistant;

import java.util.List;

/**
 * 小助手提问范围守卫：在服务端先做一次主题判断，避免把明显无关的问题发给上游大模型，
 * 从而实现“只回答本系统/助教招聘相关问题”的严格边界。
 */
public final class AssistantScopeGuard {

    private static final String[] CORE_KEYWORDS = new String[] {
            "助教", "ta", "招聘", "应聘", "岗位", "职位", "申请", "录用", "面试", "简历", "履历",
            "站内", "系统", "网站", "平台", "页面", "功能", "流程", "步骤", "入口", "操作",
            "登录", "注册", "账号", "账户", "密码", "忘记密码", "验证码",
            "上传", "下载", "提交", "撤回", "修改", "编辑", "保存", "删除",
            "个人中心", "管理", "管理员", "招聘方", "招聘端", "发布", "审核", "筛选", "匹配", "评分",
            "论坛", "帖子", "回复", "私信", "消息", "通知", "公告",
            "截止", "时间", "进度", "状态", "结果", "offer"
    };

    private static final String[] RESUME_KEYWORDS = new String[] {
            "简历", "自我介绍", "教育", "经历", "项目", "科研", "课程", "成绩", "技能", "证书",
            "奖项", "实习", "社团", "志愿", "排版", "格式", "润色", "措辞", "要点", "亮点", "改进"
    };

    private static final String[] CONTINUATION_HINTS = new String[] {
            "那", "然后", "接下来", "怎么办", "怎么做", "怎么弄", "为什么", "可以吗", "行吗", "是否",
            "还要", "需要", "下一步", "哪里", "在哪", "怎么", "如何"
    };

    private static final String[] SENSITIVE_KEYWORDS = new String[] {
            "api key", "apikey", "secret", "token", "密钥", "key", "sk-", "access key"
    };

    private static final String[] MODEL_PROVIDER_KEYWORDS = new String[] {
            "openai", "kimi", "qwen", "moonshot", "dashscope", "通义", "千问", "月之暗面"
    };

    private AssistantScopeGuard() {}

    public static boolean isInScope(List<OpenAiCompatibleChatClient.ChatMessage> messages, boolean resumeMode) {
        if (messages == null || messages.isEmpty()) {
            return true;
        }

        String lastUser = lastUserMessage(messages);
        if (lastUser == null) {
            return true;
        }
        String normalized = normalize(lastUser);
        if (looksLikeRequestingServerSecrets(normalized)) {
            return false;
        }

        if (containsAny(normalized, CORE_KEYWORDS)) {
            return true;
        }
        if (resumeMode && containsAny(normalized, RESUME_KEYWORDS)) {
            return true;
        }

        // 对“那怎么办/下一步呢”这类跟随问题做容错：若近期对话出现过范围关键词，则放行。
        if (normalized.length() <= 24 && containsAny(normalized, CONTINUATION_HINTS)) {
            int checked = 0;
            for (int i = messages.size() - 1; i >= 0 && checked < 6; i--) {
                OpenAiCompatibleChatClient.ChatMessage m = messages.get(i);
                if (m == null || m.content == null) {
                    continue;
                }
                if (!"user".equals(m.role) && !"assistant".equals(m.role)) {
                    continue;
                }
                checked++;
                String t = normalize(m.content);
                if (containsAny(t, CORE_KEYWORDS) || (resumeMode && containsAny(t, RESUME_KEYWORDS))) {
                    return true;
                }
            }
        }

        return false;
    }

    private static String lastUserMessage(List<OpenAiCompatibleChatClient.ChatMessage> messages) {
        for (int i = messages.size() - 1; i >= 0; i--) {
            OpenAiCompatibleChatClient.ChatMessage m = messages.get(i);
            if (m == null) {
                continue;
            }
            if ("user".equals(m.role)) {
                String c = m.content;
                if (c != null) {
                    String t = c.trim();
                    if (!t.isEmpty()) {
                        return t;
                    }
                }
                return "";
            }
        }
        return null;
    }

    private static boolean looksLikeRequestingServerSecrets(String normalized) {
        // 例如：“给我 OpenAI API Key/密钥”——与系统使用无关且具风险，直接判定为越界。
        if (!containsAny(normalized, SENSITIVE_KEYWORDS)) {
            return false;
        }
        return containsAny(normalized, MODEL_PROVIDER_KEYWORDS);
    }

    private static boolean containsAny(String text, String[] needles) {
        if (text == null || text.isEmpty() || needles == null) {
            return false;
        }
        for (String k : needles) {
            if (k == null || k.isEmpty()) {
                continue;
            }
            if (text.contains(k)) {
                return true;
            }
        }
        return false;
    }

    private static String normalize(String s) {
        if (s == null) {
            return "";
        }
        // 只做轻量归一化：英文统一小写，压缩空白，便于关键词命中。
        String lower = s.toLowerCase();
        return lower.replace('\u3000', ' ').replaceAll("\\s+", " ").trim();
    }
}

