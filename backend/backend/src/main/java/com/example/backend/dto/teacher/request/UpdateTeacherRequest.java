package com.example.backend.dto.teacher.request;

import com.example.backend.dto.user.request.UpdateUserRequest;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateTeacherRequest extends UpdateUserRequest {
    String department;
}
