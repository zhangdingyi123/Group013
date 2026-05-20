package com.bupt.ta.service;

import com.bupt.ta.model.Applicant;
import com.bupt.ta.model.Job;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link MatchHelper} 单元测试：规则匹配分、技能短板与岗位统计（不依赖外部 AI API）。
 */
class MatchHelperTest {

    private final MatchHelper helper = new MatchHelper();

    @Test
    void matchScore_noRequiredSkills_returns100() {
        Applicant a = applicantWithSkills("Java");
        Job job = jobWithSkills();
        assertEquals(100, helper.matchScore(a, job));
    }

    @Test
    void matchScore_partialProfileSkills() {
        Applicant a = applicantWithSkills("Java", "SQL");
        Job job = jobWithSkills("Java", "Python", "SQL");
        assertEquals(66, helper.matchScore(a, job));
    }

    @Test
    void matchScore_resumeSynonymBoostsScore() {
        Applicant a = applicantWithSkills();
        Job job = jobWithSkills("Java", "Python");
        String resume = "Used JDK and py for coursework projects.";
        assertEquals(100, helper.matchScore(a, job, resume));
    }

    @Test
    void skillGaps_listsMissingRequired() {
        Applicant a = applicantWithSkills("Java");
        Job job = jobWithSkills("Java", "Python");
        List<String> gaps = helper.skillGaps(a, job);
        assertEquals(1, gaps.size());
        assertEquals("Python", gaps.get(0));
    }

    @Test
    void computeJobMatchStats_aggregatesScores() {
        Job job = jobWithSkills("Java", "Python");
        List<MatchHelper.ApplicantMatch> list = Arrays.asList(
                new MatchHelper.ApplicantMatch(null, 80, Collections.singletonList("Python")),
                new MatchHelper.ApplicantMatch(null, 60, Arrays.asList("Java", "Python"))
        );
        MatchHelper.JobMatchStats stats = MatchHelper.computeJobMatchStats(job, list, 3);
        assertEquals(2, stats.total);
        assertEquals(70, stats.avgScore);
        assertEquals(80, stats.maxScore);
        assertEquals(60, stats.minScore);
        assertEquals(1, stats.bucket80to89);
        assertEquals(1, stats.bucket60to69);
    }

    private static Applicant applicantWithSkills(String... skills) {
        Applicant a = new Applicant("ta-1", "Test TA", "ta@test.edu", "hash", "20210001");
        a.setSkills(Arrays.asList(skills));
        return a;
    }

    private static Job jobWithSkills(String... skills) {
        Job job = new Job("job-1", "Demo Job", "mo-1", "desc", "course_ta");
        job.setRequiredSkills(Arrays.asList(skills));
        return job;
    }
}
