package com.example.backend.dto.Authentication.request;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    String phone;
    String otp;
    String newPassword;
}