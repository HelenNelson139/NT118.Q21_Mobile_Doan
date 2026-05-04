package com.example.backend.dto.student.request;

import com.example.backend.dto.user.request.UpdateUserRequest;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateStudentRequest extends UpdateUserRequest {
    Date date_of_birth;
}
