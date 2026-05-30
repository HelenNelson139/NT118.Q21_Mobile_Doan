package com.example.backend.dto.lesson.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LessonUpdateRequest {
    String title;
    String description;
    String what_you_learn;
    String skill_learned;
}