package com.bupt.ta.service;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link SkillExtractor} 单元测试：同义词与词边界匹配。
 */
class SkillExtractorTest {

    @Test
    void extractSkills_findsJavaSynonym() {
        String resume = "Experienced with JDK 11 and Spring.";
        List<String> required = Arrays.asList("Java", "Python");
        Set<String> found = SkillExtractor.extractSkills(resume, required);
        assertTrue(found.contains("Java"));
        assertFalse(found.contains("Python"));
    }

    @Test
    void extractSkills_findsChineseCommunication() {
        String resume = "具备良好的沟通表达与团队协作能力。";
        List<String> required = Arrays.asList("Communication", "Java");
        Set<String> found = SkillExtractor.extractSkills(resume, required);
        assertTrue(found.contains("Communication"));
    }

    @Test
    void extractSkills_emptyInput_returnsEmpty() {
        assertTrue(SkillExtractor.extractSkills("", Arrays.asList("Java")).isEmpty());
        assertTrue(SkillExtractor.extractSkills("some text", null).isEmpty());
    }

    @Test
    void extractSkillsWithWeight_mapsToOne() {
        var weights = SkillExtractor.extractSkillsWithWeight(
                "Proficient in Python3.", Arrays.asList("Python"));
        assertEquals(1.0, weights.get("Python"), 0.001);
    }
}
