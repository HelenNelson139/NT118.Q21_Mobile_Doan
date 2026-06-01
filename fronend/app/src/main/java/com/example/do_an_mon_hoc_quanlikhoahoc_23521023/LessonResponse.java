package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import com.google.gson.annotations.SerializedName;

import java.util.List;

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

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("teacher_name")
    private String teacherName;

    @SerializedName("module")
    private List<ModuleResponse> modules;

    public LessonResponse() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getIdAsInt() {
        return id == null ? -1 : id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWhatYouLearn() {
        return whatYouLearn;
    }

    public void setWhatYouLearn(String whatYouLearn) {
        this.whatYouLearn = whatYouLearn;
    }

    public String getWhat_you_learn() {
        return whatYouLearn;
    }

    public void setWhat_you_learn(String whatYouLearn) {
        this.whatYouLearn = whatYouLearn;
    }

    public String getSkillLearned() {
        return skillLearned;
    }

    public void setSkillLearned(String skillLearned) {
        this.skillLearned = skillLearned;
    }

    public String getSkill_learned() {
        return skillLearned;
    }

    public void setSkill_learned(String skillLearned) {
        this.skillLearned = skillLearned;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getThumbnail_url() {
        return thumbnailUrl;
    }

    public void setThumbnail_url(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreated_at() {
        return createdAt;
    }

    public void setCreated_at(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getTeacher_name() {
        return teacherName;
    }

    public void setTeacher_name(String teacherName) {
        this.teacherName = teacherName;
    }

    public List<ModuleResponse> getModules() {
        return modules;
    }

    public void setModules(List<ModuleResponse> modules) {
        this.modules = modules;
    }
}