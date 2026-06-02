package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

public class AdminCourseSummary {

    private Integer id;
    private String title;
    private String lecturer;
    private String postDate;

    public AdminCourseSummary(Integer id, String title, String lecturer, String postDate) {
        this.id = id;
        this.title = title;
        this.lecturer = lecturer;
        this.postDate = postDate;
    }

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getLecturer() {
        return lecturer;
    }

    public String getPostDate() {
        return postDate;
    }
}