package com.bupt.ta.service;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 优化版技能提取器：支持同义词映射 + 单词边界正则
 */
public class SkillExtractor {

    private static final Map<String, Set<String>> SYNONYMS = new HashMap<>();

    static {
        // 技术技能
        putSynonym("java", "java", "j2se", "java se", "jdk", "java8", "java11");
        putSynonym("python", "python", "py", "python3");
        putSynonym("c++", "c++", "cpp", "c plus plus", "cplusplus");
        putSynonym("sql", "sql", "mysql", "postgresql", "oracle", "sqlite");
        putSynonym("javascript", "javascript", "js", "ecmascript");
        // 软技能
        putSynonym("Communication", "沟通", "交流", "表达", "人际", "沟通表达", "与人交流", "跨部门沟通","communication");
        putSynonym("团队合作", "团队合作", "协作", "teamwork", "配合", "协同");
        putSynonym("领导力", "领导力", "领导", "管理", "带队");
        // 可继续扩充
    }

    private static void putSynonym(String canonical, String... synonyms) {
        Set<String> set = new HashSet<>();
        for (String s : synonyms) {
            set.add(s.toLowerCase());
        }
        SYNONYMS.put(canonical.toLowerCase(), set);
    }

    private static Set<String> getSynonymSet(String canonicalLower) {
        Set<String> syns = SYNONYMS.get(canonicalLower);
        return (syns == null || syns.isEmpty()) ? Collections.singleton(canonicalLower) : syns;
    }

    private static boolean containsWord(String textLower, String wordLower) {
        if (wordLower.matches("^[a-z0-9_]+$")) {
            String regex = "\\b" + Pattern.quote(wordLower) + "\\b";
            return Pattern.compile(regex).matcher(textLower).find();
        } else if (wordLower.matches("^[\u4e00-\u9fa5]+$")) {
            return textLower.contains(wordLower);
        } else {
            String regex = "(?<![a-zA-Z0-9])" + Pattern.quote(wordLower) + "(?![a-zA-Z0-9])";
            return Pattern.compile(regex).matcher(textLower).find();
        }
    }

    /**
     * 从简历文本中提取技能（基于同义词和单词边界）
     * @param resumeText 简历纯文本
     * @param knownSkills 需要检测的技能列表
     * @return 匹配到的技能集合（原始大小写）
     */
    public static Set<String> extractSkills(String resumeText, Collection<String> knownSkills) {
        Set<String> result = new LinkedHashSet<>();
        if (resumeText == null || resumeText.trim().isEmpty() || knownSkills == null || knownSkills.isEmpty()) {
            return result;
        }
        String lowerText = resumeText.toLowerCase();
        for (String skill : knownSkills) {
            if (skill == null || skill.trim().isEmpty()) continue;
            String trimmed = skill.trim();
            String canonical = trimmed.toLowerCase();
            Set<String> synonyms = getSynonymSet(canonical);
            boolean found = false;
            for (String syn : synonyms) {
                if (containsWord(lowerText, syn)) {
                    found = true;
                    break;
                }
            }
            if (found) {
                result.add(trimmed);
            }
        }
        return result;
    }

    /**
     * 提取技能并返回权重映射（规则命中时权重=1.0）
     */
    public static Map<String, Double> extractSkillsWithWeight(String resumeText, Collection<String> knownSkills) {
        Map<String, Double> weightMap = new LinkedHashMap<>();
        for (String skill : extractSkills(resumeText, knownSkills)) {
            weightMap.put(skill, 1.0);
        }
        return weightMap;
    }
}