package com.example.backend.dto.student.response;

import com.example.backend.dto.user.response.UserResponseProfile;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class StudentResponseProfile extends UserResponseProfile {
    Date date_of_birth;
}
