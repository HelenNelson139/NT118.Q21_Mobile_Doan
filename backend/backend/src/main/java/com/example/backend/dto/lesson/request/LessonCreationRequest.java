package com.example.backend.dto.lesson.request;

import com.example.backend.entity.Teacher;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class LessonCreationRequest {
    Teacher teacher;
    String title;
    String description;
    String what_you_learn;
    String skill_learned;
    String thumbnail_url;
    Integer teacherId;
}
