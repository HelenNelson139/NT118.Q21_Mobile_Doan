package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

public class StudentCourseRequest {

    private Integer userId;
    private Integer lessonId;

    public StudentCourseRequest(Integer userId, Integer lessonId) {
        this.userId = userId;
        this.lessonId = lessonId;
    }

    public Integer getUserId() {
        return userId;
    }

    public Integer getLessonId() {
        return lessonId;
    }
}