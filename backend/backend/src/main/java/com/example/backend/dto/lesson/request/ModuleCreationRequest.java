package com.example.backend.dto.lesson.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ModuleCreationRequest {
    Integer lessonId;
    String title;
    String objective;
    String content;
    String example;
    String image_example_url;
    Integer order_index;
}
