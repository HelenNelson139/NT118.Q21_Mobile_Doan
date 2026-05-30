package com.example.backend.dto.Authentication.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationResponse {
    String role;
    String full_name;
    Integer id;
    String accessToken;
    boolean authenticated;
}
