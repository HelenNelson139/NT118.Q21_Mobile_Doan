    package com.example.backend.dto.lesson.response;
    
    
    import com.example.backend.enums.Status;
    import lombok.*;
    import lombok.experimental.FieldDefaults;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public class ModuleResponse {
        Integer id;
        Integer lessonId; 
        String title;
        String objective;
        String content;
        String example;
        String image_example_url;
        Integer order_index;
        Status status;
    }
