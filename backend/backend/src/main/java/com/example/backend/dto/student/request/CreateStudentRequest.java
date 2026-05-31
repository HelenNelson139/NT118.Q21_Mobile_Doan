package com.example.backend.dto.student.request;

import com.example.backend.dto.user.request.CreateUserRequest;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateStudentRequest extends CreateUserRequest {
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    Date date_of_birth;
}
