package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

public class LessonUpdateRequest {
    private String title;
    private String description;
    private String what_you_learn;
    private String skill_learned;

    public LessonUpdateRequest(String title, String description, String what_you_learn, String skill_learned) {
        this.title = title;
        this.description = description;
        this.what_you_learn = what_you_learn;
        this.skill_learned = skill_learned;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getWhat_you_learn() { return what_you_learn; }
    public void setWhat_you_learn(String what_you_learn) { this.what_you_learn = what_you_learn; }
    public String getSkill_learned() { return skill_learned; }
    public void setSkill_learned(String skill_learned) { this.skill_learned = skill_learned; }
}