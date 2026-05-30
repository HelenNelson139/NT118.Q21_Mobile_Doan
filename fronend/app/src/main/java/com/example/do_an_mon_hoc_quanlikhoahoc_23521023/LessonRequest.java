package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

public class LessonRequest {
    private String title;
    private String description;
    private String what_you_learn;
    private String skill_learned;
    private Integer teacherId;
    public LessonRequest(String title, String description, String what_you_learn, String skill_learned, Integer teacherId) {
        this.title = title;
        this.description = description;
        this.what_you_learn = what_you_learn;
        this.skill_learned = skill_learned;
        this.teacherId = teacherId;
    }
}