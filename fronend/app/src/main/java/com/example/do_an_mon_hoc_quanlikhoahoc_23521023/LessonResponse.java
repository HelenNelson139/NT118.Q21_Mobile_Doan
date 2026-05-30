package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;
import com.google.gson.annotations.SerializedName;

public class LessonResponse {
    private Integer id;
    private String title;
    private String description;
    @SerializedName("what_you_learn")
    private String whatYouLearn;
    @SerializedName("skill_learned")
    private String skillLearned;
    @SerializedName("thumbnail_url")
    private String thumbnailUrl;
    private String status;
    private String created_at;
    private String teacher_name;
    public Integer getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getWhatYouLearn() { return whatYouLearn; }
    public String getSkillLearned() { return skillLearned; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return created_at; }
    public void setStatus(String status) {
        this.status = status;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getTeacher_name() {
        return teacher_name;
    }

    public void setTeacher_name(String teacher_name) {
        this.teacher_name = teacher_name;
    }
}
