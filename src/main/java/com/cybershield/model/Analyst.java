package com.cybershield.model;

import com.cybershield.model.enums.AnalystStatus;

public class Analyst {
    private String analystId;
    private String name;
    private String skill;
    private String experienceLevel;
    private AnalystStatus status;

    public Analyst() {}

    public Analyst(String analystId, String name, String skill, String experienceLevel, AnalystStatus status) {
        this.analystId = analystId;
        this.name = name;
        this.skill = skill;
        this.experienceLevel = experienceLevel;
        this.status = status;
    }

    public String getAnalystId() { return analystId; }
    public void setAnalystId(String analystId) { this.analystId = analystId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSkill() { return skill; }
    public void setSkill(String skill) { this.skill = skill; }

    public String getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }

    public AnalystStatus getStatus() { return status; }
    public void setStatus(AnalystStatus status) { this.status = status; }
}
