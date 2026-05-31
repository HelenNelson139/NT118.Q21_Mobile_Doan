package com.example.backend.dto.student.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EnrollmentRequest {
    Integer userId;
    Integer lessonId;
}
