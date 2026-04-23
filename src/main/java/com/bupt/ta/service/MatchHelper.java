package com.bupt.ta.service;

import com.bupt.ta.model.Applicant;
import com.bupt.ta.model.Application;
import com.bupt.ta.model.Job;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 轻量级「AI 辅助」：岗位与应聘者技能匹配、技能短板识别、工作负荷均衡建议。
 * 默认基于规则与统计；可选开启 embeddings 语义匹配（见 {@link com.bupt.ta.service.assistant.AssistantConfig#semanticMatchEnabled()}）。
 */
public class MatchHelper {
    private final ApplicantService applicantService = new ApplicantService();
    private final JobService jobService = new JobService();
    private final ApplicationService applicationService = new ApplicationService();
    private final SemanticMatchService semanticMatchService = new SemanticMatchService();

    /** 岗位推荐结果的统计概览（用于招聘方快速把握整体匹配情况）。 */
    public static class JobMatchStats {
        public final int total;
        public final int avgScore; // rounded
        public final int medianScore;
        public final int minScore;
        public final int maxScore;
        public final int bucket0to59;
        public final int bucket60to69;
        public final int bucket70to79;
        public final int bucket80to89;
        public final int bucket90to100;
        public final List<SkillCount> topGaps;
        public final List<SkillCount> topStrengths;

        public JobMatchStats(
                int total,
                int avgScore,
                int medianScore,
                int minScore,
                int maxScore,
                int bucket0to59,
                int bucket60to69,
                int bucket70to79,
                int bucket80to89,
                int bucket90to100,
                List<SkillCount> topGaps,
                List<SkillCount> topStrengths) {
            this.total = total;
            this.avgScore = avgScore;
            this.medianScore = medianScore;
            this.minScore = minScore;
            this.maxScore = maxScore;
            this.bucket0to59 = bucket0to59;
            this.bucket60to69 = bucket60to69;
            this.bucket70to79 = bucket70to79;
            this.bucket80to89 = bucket80to89;
            this.bucket90to100 = bucket90to100;
            this.topGaps = topGaps != null ? topGaps : Collections.emptyList();
            this.topStrengths = topStrengths != null ? topStrengths : Collections.emptyList();
        }
    }

    public static class SkillCount {
        public final String skill;
        public final int count;

        public SkillCount(String skill, int count) {
            this.skill = skill;
            this.count = count;
        }
    }

    /**
     * 由推荐列表计算岗位匹配统计（不涉及任何外部调用）。
     *
     * @param job  岗位（用于 requiredSkills 统计优势）
     * @param list 推荐列表（可为全部或筛选后的子集）
     * @param topN TopN 技能展示数量（<=0 则不截断）
     */
    public static JobMatchStats computeJobMatchStats(Job job, List<ApplicantMatch> list, int topN) {
        if (list == null || list.isEmpty()) {
            return new JobMatchStats(0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    Collections.emptyList(), Collections.emptyList());
        }
        int total = list.size();

        int sum = 0;
        int min = 100;
        int max = 0;
        int b0 = 0, b60 = 0, b70 = 0, b80 = 0, b90 = 0;
        int[] scores = new int[total];

        Map<String, Integer> gapCountLower = new HashMap<>();
        Map<String, String> gapDisplayLower = new HashMap<>();

        for (int i = 0; i < total; i++) {
            ApplicantMatch m = list.get(i);
            int s = m != null ? m.score : 0;
            if (s < 0) s = 0;
            if (s > 100) s = 100;
            scores[i] = s;
            sum += s;
            if (s < min) min = s;
            if (s > max) max = s;
            if (s >= 90) b90++;
            else if (s >= 80) b80++;
            else if (s >= 70) b70++;
            else if (s >= 60) b60++;
            else b0++;

            if (m != null && m.gaps != null) {
                for (String g : m.gaps) {
                    if (g == null) continue;
                    String t = g.trim();
                    if (t.isEmpty()) continue;
                    String k = t.toLowerCase();
                    gapCountLower.merge(k, 1, Integer::sum);
                    gapDisplayLower.putIfAbsent(k, t);
                }
            }
        }

        Arrays.sort(scores);
        int median = scores[(scores.length - 1) / 2];
        int avg = (int) Math.round(sum * 1.0 / total);

        List<SkillCount> topGaps = toTopSkillCounts(gapCountLower, gapDisplayLower, topN);
        List<SkillCount> topStrengths = computeTopStrengths(job, total, gapCountLower, topN);
        return new JobMatchStats(total, avg, median, min, max, b0, b60, b70, b80, b90, topGaps, topStrengths);
    }

    private static List<SkillCount> computeTopStrengths(Job job, int total, Map<String, Integer> gapCountLower, int topN) {
        if (job == null || job.getRequiredSkills() == null || job.getRequiredSkills().isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, Integer> strengthCountLower = new HashMap<>();
        Map<String, String> strengthDisplayLower = new HashMap<>();
        for (String req : job.getRequiredSkills()) {
            if (req == null) continue;
            String t = req.trim();
            if (t.isEmpty()) continue;
            String k = t.toLowerCase();
            int gaps = gapCountLower.getOrDefault(k, 0);
            int has = total - gaps;
            if (has < 0) has = 0;
            strengthCountLower.put(k, has);
            strengthDisplayLower.putIfAbsent(k, t);
        }
        return toTopSkillCounts(strengthCountLower, strengthDisplayLower, topN);
    }

    private static List<SkillCount> toTopSkillCounts(Map<String, Integer> countLower, Map<String, String> displayLower, int topN) {
        if (countLower == null || countLower.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(countLower.entrySet());
        entries.sort((a, b) -> {
            int c = Integer.compare(b.getValue(), a.getValue());
            if (c != 0) return c;
            return a.getKey().compareTo(b.getKey());
        });
        int limit = topN <= 0 ? entries.size() : Math.min(topN, entries.size());
        List<SkillCount> result = new ArrayList<>(limit);
        for (int i = 0; i < limit; i++) {
            Map.Entry<String, Integer> e = entries.get(i);
            String skill = displayLower != null ? displayLower.getOrDefault(e.getKey(), e.getKey()) : e.getKey();
            result.add(new SkillCount(skill, e.getValue()));
        }
        return result;
    }

    /**
     * 计算应聘者与岗位的技能匹配度 (0-100)。
     * 匹配度 = (匹配技能数 / 岗位所需技能数) * 100，若无所需技能则按 0 计。
     * 仅使用档案技能；见 {@link #matchScore(Applicant, Job, String)}。
     */
    public int matchScore(Applicant applicant, Job job) {
        return matchScore(applicant, job, null);
    }

    /**
     * 计算匹配度：档案技能 + 可选简历正文（与岗位所需技能做 {@link #extractSkillsFromResume} 关键词匹配后合并）。
     */
    public int matchScore(Applicant applicant, Job job, String resumeText) {
        Integer semantic = semanticMatchService.semanticMatchScore(applicant, job, resumeText);
        if (semantic != null) {
            return semantic;
        }
        if (job.getRequiredSkills() == null || job.getRequiredSkills().isEmpty()) {
            return 100;
        }
        Set<String> required = job.getRequiredSkills().stream()
                .map(String::toLowerCase).collect(Collectors.toSet());
        Set<String> has = new HashSet<>();
        if (applicant.getSkills() != null) {
            for (String s : applicant.getSkills()) {
                if (s != null && !s.trim().isEmpty()) {
                    has.add(s.trim().toLowerCase());
                }
            }
        }
        if (resumeText != null && !resumeText.isEmpty()) {
            for (String s : extractSkillsFromResume(resumeText, job.getRequiredSkills())) {
                has.add(s.trim().toLowerCase());
            }
        }
        long match = required.stream().filter(has::contains).count();
        return (int) (match * 100 / required.size());
    }

    /**
     * 识别应聘者相对某岗位的技能短板（岗位需要但应聘者没有的技能）。仅档案技能。
     */
    public List<String> skillGaps(Applicant applicant, Job job) {
        return skillGaps(applicant, job, null);
    }

    /** 短板识别：档案技能 + 简历文本中与岗位要求匹配到的技能均视为已具备。 */
    public List<String> skillGaps(Applicant applicant, Job job, String resumeText) {
        if (job.getRequiredSkills() == null || job.getRequiredSkills().isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> required = new HashSet<>(job.getRequiredSkills());
        Set<String> has = new HashSet<>();
        if (applicant.getSkills() != null) {
            has.addAll(applicant.getSkills());
        }
        if (resumeText != null && !resumeText.isEmpty()) {
            has.addAll(extractSkillsFromResume(resumeText, job.getRequiredSkills()));
        }
        required.removeIf(s -> has.stream().anyMatch(h -> h.equalsIgnoreCase(s)));
        return new ArrayList<>(required);
    }

    /**
     * 为岗位推荐应聘者：按匹配度降序，并附带匹配分与技能短板。
     */
    public static class ApplicantMatch {
        public final Applicant applicant;
        public final int score;
        public final List<String> gaps;

        public ApplicantMatch(Applicant applicant, int score, List<String> gaps) {
            this.applicant = applicant;
            this.score = score;
            this.gaps = gaps;
        }
    }

    public List<ApplicantMatch> recommendApplicantsForJob(String jobId) throws IOException {
        Optional<Job> jobOpt = jobService.findById(jobId);
        if (jobOpt.isEmpty()) return Collections.emptyList();
        Job job = jobOpt.get();
        List<Application> applications = applicationService.findByJobId(jobId);
        List<ApplicantMatch> result = new ArrayList<>();
        for (Application app : applications) {
            applicantService.findById(app.getApplicantId()).ifPresent(a -> {
                String resumeText = null;
                try {
                    resumeText = resumeTextForMatching(a);
                } catch (IOException ignored) {
                }
                int score = matchScore(a, job, resumeText);
                result.add(new ApplicantMatch(a, score, skillGaps(a, job, resumeText)));
            });
        }
        result.sort((a, b) -> Integer.compare(b.score, a.score));
        return result;
    }

    /**
     * 助教工作负荷：每个应聘者当前被录用的岗位数（accepted 申请数）。
     */
    public Map<String, Integer> workloadByApplicant() throws IOException {
        List<Application> all = applicationService.findAll();
        Map<String, Integer> count = new HashMap<>();
        for (Application a : all) {
            if (Application.STATUS_ACCEPTED.equals(a.getStatus())) {
                count.merge(a.getApplicantId(), 1, Integer::sum);
            }
        }
        return count;
    }

    /**
     * 负荷均衡建议：优先推荐当前录用数较少的应聘者（在推荐列表中可据此排序或标注）。
     */
    public List<ApplicantMatch> recommendApplicantsForJobBalanced(String jobId) throws IOException {
        Map<String, Integer> workload = workloadByApplicant();
        List<ApplicantMatch> base = recommendApplicantsForJob(jobId);
        base.sort((a, b) -> {
            int scoreDiff = Integer.compare(b.score, a.score);
            if (scoreDiff != 0) return scoreDiff;
            int loadA = workload.getOrDefault(a.applicant.getId(), 0);
            int loadB = workload.getOrDefault(b.applicant.getId(), 0);
            return Integer.compare(loadA, loadB); // 负荷小的优先
        });
        return base;
    }

    // ---------- 根据简历识别技能短板 ----------

    /** 从简历文本中识别出现的技能（与已知技能集做关键词匹配，忽略大小写） */
    public Set<String> extractSkillsFromResume(String resumeText, Collection<String> knownSkills) {
        Set<String> detected = new HashSet<>();
        if (resumeText == null || resumeText.isEmpty() || knownSkills == null) return detected;
        String lower = resumeText.toLowerCase();
        for (String skill : knownSkills) {
            if (skill == null || skill.trim().isEmpty()) continue;
            if (lower.contains(skill.trim().toLowerCase())) {
                detected.add(skill.trim());
            }
        }
        return detected;
    }

    /**
     * 综合个人档案技能 + 简历文本识别出的技能，相对当前开放岗位所需技能，得出短板并给出提示。
     * 仅当简历为 .txt 时可解析文本；否则仅用档案技能。
     */
    public List<String> getResumeBasedSkillGaps(Applicant applicant, String resumeText, List<Job> openJobs) throws IOException {
        Set<String> knownSkills = new HashSet<>();
        for (Job j : openJobs) {
            if (j.getRequiredSkills() != null) {
                knownSkills.addAll(j.getRequiredSkills());
            }
        }
        if (knownSkills.isEmpty()) return Collections.emptyList();

        Set<String> hasSkills = new HashSet<>();
        if (applicant.getSkills() != null) {
            for (String s : applicant.getSkills()) {
                if (s != null && !s.trim().isEmpty()) hasSkills.add(s.trim());
            }
        }
        if (resumeText != null && !resumeText.isEmpty()) {
            Set<String> fromResume = extractSkillsFromResume(resumeText, knownSkills);
            hasSkills.addAll(fromResume);
        }

        Set<String> gaps = new HashSet<>();
        for (String req : knownSkills) {
            if (req == null || req.trim().isEmpty()) continue;
            String r = req.trim();
            boolean found = false;
            for (String h : hasSkills) {
                if (h.equalsIgnoreCase(r)) { found = true; break; }
            }
            if (!found) gaps.add(r);
        }
        return new ArrayList<>(gaps);
    }

    /**
     * 根据上传的简历文本，识别与当前开放岗位需求匹配的技能，作为「从简历得到的优点」。
     * 仅当简历为 .txt 且能解析文本时返回；否则返回空列表。
     */
    public List<String> getResumeBasedStrengths(Applicant applicant, String resumeText, List<Job> openJobs) throws IOException {
        Set<String> knownSkills = new HashSet<>();
        for (Job j : openJobs) {
            if (j.getRequiredSkills() != null) {
                knownSkills.addAll(j.getRequiredSkills());
            }
        }
        if (knownSkills.isEmpty() || resumeText == null || resumeText.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> fromResume = extractSkillsFromResume(resumeText, knownSkills);
        return new ArrayList<>(fromResume);
    }

    /** 读取简历纯文本用于匹配：.txt 直接读；.pdf/.doc/.docx 尝试抽取正文；失败则返回 null。 */
    private String resumeTextForMatching(Applicant applicant) throws IOException {
        String path = applicant.getResumePath();
        if (path == null || path.trim().isEmpty()) {
            return null;
        }
        return applicantService.extractResumePlainText(path);
    }
}
