package com.example.backend.dto.Authentication.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class GoogleLoginRequest {
    String idToken;
}
