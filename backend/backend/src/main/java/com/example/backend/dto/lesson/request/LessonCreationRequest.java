package com.example.backend.dto.lesson.request;

import com.example.backend.entity.Teacher;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class LessonCreationRequest {
    Teacher teacher;
    String title;
    String description;
    String what_you_learn;
    String skill_learned;
    MultipartFile thumbnail;
    Integer teacherId;
}
