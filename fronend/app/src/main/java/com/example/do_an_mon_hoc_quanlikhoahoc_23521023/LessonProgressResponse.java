package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import com.google.gson.annotations.SerializedName;

public class LessonProgressResponse {

    @SerializedName("lessonId")
    private Integer lessonId;

    @SerializedName("studentId")
    private Integer studentId;

    @SerializedName("totalModules")
    private Integer totalModules;

    @SerializedName("completedModules")
    private Integer completedModules;

    @SerializedName("progressPercent")
    private Double progressPercent;

    @SerializedName("completed")
    private Boolean completed;

    public Integer getLessonId() {
        return lessonId;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public Integer getTotalModules() {
        return totalModules == null ? 0 : totalModules;
    }

    public Integer getCompletedModules() {
        return completedModules == null ? 0 : completedModules;
    }

    public Double getProgressPercent() {
        return progressPercent == null ? 0.0 : progressPercent;
    }

    public Boolean getCompleted() {
        return completed != null && completed;
    }
}