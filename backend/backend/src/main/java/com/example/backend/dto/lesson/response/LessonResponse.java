package com.example.backend.dto.lesson.response;

import com.example.backend.enums.Status;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LessonResponse {
    Integer id;
    String title;
    String description;
    String what_you_learn;
    String skill_learned;
    String thumbnail_url;
    Status status;
    Date created_at;
}