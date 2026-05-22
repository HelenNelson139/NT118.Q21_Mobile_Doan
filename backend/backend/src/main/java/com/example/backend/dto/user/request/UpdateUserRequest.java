package com.example.backend.dto.user.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateUserRequest {
    String username;
    String email;
    String phone;
    String full_name;
}
