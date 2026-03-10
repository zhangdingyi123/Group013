package com.bupt.ta.service;

import com.bupt.ta.model.Applicant;
import com.bupt.ta.model.Application;
import com.bupt.ta.model.Job;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 轻量级「AI 辅助」：岗位与应聘者技能匹配、技能短板识别、工作负荷均衡建议。
 * 基于规则与统计，无外部 AI 接口。
 */
public class MatchHelper {
    private final ApplicantService applicantService = new ApplicantService();
    private final JobService jobService = new JobService();
    private final ApplicationService applicationService = new ApplicationService();

    /**
     * 计算应聘者与岗位的技能匹配度 (0-100)。
     * 匹配度 = (匹配技能数 / 岗位所需技能数) * 100，若无所需技能则按 0 计。
     */
    public int matchScore(Applicant applicant, Job job) {
        if (job.getRequiredSkills() == null || job.getRequiredSkills().isEmpty()) {
            return 100;
        }
        Set<String> required = job.getRequiredSkills().stream()
                .map(String::toLowerCase).collect(Collectors.toSet());
        Set<String> has = applicant.getSkills() != null
                ? applicant.getSkills().stream().map(String::toLowerCase).collect(Collectors.toSet())
                : Collections.emptySet();
        long match = required.stream().filter(has::contains).count();
        return (int) (match * 100 / required.size());
    }

    /**
     * 识别应聘者相对某岗位的技能短板（岗位需要但应聘者没有的技能）。
     */
    public List<String> skillGaps(Applicant applicant, Job job) {
        if (job.getRequiredSkills() == null || job.getRequiredSkills().isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> required = new HashSet<>(job.getRequiredSkills());
        Set<String> has = applicant.getSkills() != null
                ? new HashSet<>(applicant.getSkills())
                : Collections.emptySet();
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
                int score = matchScore(a, job);
                result.add(new ApplicantMatch(a, score, skillGaps(a, job)));
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
}
