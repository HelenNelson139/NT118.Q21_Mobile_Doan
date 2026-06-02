package com.example.backend.dto.lesson.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonProgressResponse {

    private Integer lessonId;

    private Integer studentId;

    private long totalModules;

    private long completedModules;

    private double progressPercent;

    private boolean completed;
}
