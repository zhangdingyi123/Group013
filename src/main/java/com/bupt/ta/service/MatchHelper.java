package com.bupt.ta.service;

import com.bupt.ta.model.Applicant;
import com.bupt.ta.model.Application;
import com.bupt.ta.model.Job;

import java.util.*;

/**
 * AI 辅助：岗位与应聘者技能匹配、技能短板、工作负荷统计。
 * 供 API、桌面端与 Servlet/JSP 共用。
 */
public final class MatchHelper {

    public static List<Map<String, Object>> jobsWithMatch(String applicantId,
                                                           ApplicantService applicantService,
                                                           JobService jobService) {
        Applicant a = applicantService.findById(applicantId).orElse(null);
        List<String> mySkills = a != null && a.getSkills() != null ? a.getSkills() : new ArrayList<>();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Job j : jobService.findOpen()) {
            List<String> required = j.getRequiredSkills() != null ? j.getRequiredSkills() : new ArrayList<>();
            List<String> missing = missingSkills(required, mySkills);
            int matchScore = required.isEmpty() ? 100 : (int) Math.round(100.0 * (required.size() - missing.size()) / required.size());
            Map<String, Object> item = new HashMap<>();
            item.put("job", j);
            item.put("matchScore", matchScore);
            item.put("missingSkills", missing);
            result.add(item);
        }
        result.sort((x, y) -> (Integer) y.get("matchScore") - (Integer) x.get("matchScore"));
        return result;
    }

    public static List<Map<String, Object>> applicationsWithStats(String jobId,
                                                                  ApplicantService applicantService,
                                                                  JobService jobService,
                                                                  ApplicationService applicationService) {
        Job job = jobService.findById(jobId).orElse(null);
        List<String> required = job != null && job.getRequiredSkills() != null ? job.getRequiredSkills() : new ArrayList<>();
        Map<String, Integer> workloadMap = new HashMap<>();
        for (Application app : applicationService.findAll()) {
            if ("SELECTED".equalsIgnoreCase(app.getStatus()))
                workloadMap.merge(app.getApplicantId(), 1, Integer::sum);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Application app : applicationService.findByJob(jobId)) {
            Applicant a = applicantService.findById(app.getApplicantId()).orElse(null);
            List<String> mySkills = a != null && a.getSkills() != null ? a.getSkills() : new ArrayList<>();
            List<String> missing = missingSkills(required, mySkills);
            int matchScore = required.isEmpty() ? 100 : (int) Math.round(100.0 * (required.size() - missing.size()) / required.size());
            int workload = workloadMap.getOrDefault(app.getApplicantId(), 0);
            Map<String, Object> row = new HashMap<>();
            row.put("application", app);
            row.put("applicantName", a != null ? a.getName() : app.getApplicantId());
            row.put("applicantEmail", a != null ? a.getEmail() : "");
            row.put("matchScore", matchScore);
            row.put("missingSkills", missing);
            row.put("workload", workload);
            result.add(row);
        }
        result.sort((x, y) -> {
            int c = (Integer) y.get("matchScore") - (Integer) x.get("matchScore");
            if (c != 0) return c;
            return (Integer) x.get("workload") - (Integer) y.get("workload");
        });
        return result;
    }

    private static List<String> missingSkills(List<String> required, List<String> mySkills) {
        List<String> missing = new ArrayList<>();
        for (String s : required) {
            if (s == null || s.isBlank()) continue;
            boolean found = false;
            for (String mine : mySkills) {
                if (mine != null && mine.trim().equalsIgnoreCase(s.trim())) { found = true; break; }
            }
            if (!found) missing.add(s.trim());
        }
        return missing;
    }
}
