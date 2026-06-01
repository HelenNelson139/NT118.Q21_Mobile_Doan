package com.example.backend.dto.lesson.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

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
    MultipartFile file;
    Integer order_index;
}
