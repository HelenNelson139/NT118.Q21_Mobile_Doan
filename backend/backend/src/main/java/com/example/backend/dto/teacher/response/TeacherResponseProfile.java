package com.example.backend.dto.teacher.response;

import com.example.backend.dto.user.response.UserResponseProfile;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Data
public class TeacherResponseProfile extends UserResponseProfile {
    String teacher_code;
    String department;
}
