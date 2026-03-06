package com.bupt.ta.model;

/**
 * Module Organiser (MO) who posts jobs and selects applicants.
 */
public class ModuleOrganiser {
    private String id;
    private String name;
    private String email;

    public ModuleOrganiser() {}

    public ModuleOrganiser(String id, String name, String email) {
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
}
