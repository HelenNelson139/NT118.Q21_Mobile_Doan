package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import com.google.gson.annotations.SerializedName;

public class LessonUpdateRequest {

    private String title;
    private String description;

    @SerializedName("what_you_learn")
    private String whatYouLearn;

    @SerializedName("skill_learned")
    private String skillLearned;

    public LessonUpdateRequest(
            String title,
            String description,
            String whatYouLearn,
            String skillLearned
    ) {
        this.title = title;
        this.description = description;
        this.whatYouLearn = whatYouLearn;
        this.skillLearned = skillLearned;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getWhatYouLearn() {
        return whatYouLearn;
    }

    public String getSkillLearned() {
        return skillLearned;
    }
}