package com.example.backend.dto.user.response;

import com.example.backend.enums.Role;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponseProfile {
    String username;
    String password;
    String email;
    String phone;
    String full_name;
    String avatar_url;
    String status;
    Role role;
}