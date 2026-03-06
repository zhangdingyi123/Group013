package com.bupt.ta.model;

import java.util.ArrayList;
import java.util.List;

/**
 * TA applicant profile.
 */
public class Applicant {
    private String id;
    private String name;
    private String email;
    private String cvPath;       // path to uploaded CV file (e.g. .txt)
    private List<String> skills;

    public Applicant() {
        this.skills = new ArrayList<>();
    }

    public Applicant(String id, String name, String email) {
        this();
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCvPath() { return cvPath; }
    public void setCvPath(String cvPath) { this.cvPath = cvPath; }
    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills != null ? skills : new ArrayList<>(); }
}
