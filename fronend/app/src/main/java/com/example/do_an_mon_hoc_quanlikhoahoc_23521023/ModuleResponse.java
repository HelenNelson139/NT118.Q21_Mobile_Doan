package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import com.google.gson.annotations.SerializedName;
public class ModuleResponse {
    private Integer id;
    private Integer lessonId;
    private String title;
    private String objective;
    private String content;
    private String example;
    @SerializedName("image_example_url")
    private String imageExampleUrl;

    @SerializedName("order_index")
    private Integer orderIndex;
    private String status;
    public ModuleResponse() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getLessonId() {
        return lessonId;
    }

    public void setLessonId(Integer lessonId) {
        this.lessonId = lessonId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getObjective() {
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public String getImageExampleUrl() {
        return imageExampleUrl;
    }

    public void setImageExampleUrl(String imageExampleUrl) {
        this.imageExampleUrl = imageExampleUrl;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}