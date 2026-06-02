package com.example.backend.dto.Authentication.request;

import lombok.Data;

@Data
public class VerifyOtpRequest {
    String phone;
    String otp;
}