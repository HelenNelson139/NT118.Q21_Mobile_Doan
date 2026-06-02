package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.Authentication.request.ForgotPasswordRequest;
import com.example.backend.dto.Authentication.request.ResetPasswordRequest;
import com.example.backend.dto.Authentication.request.VerifyOtpRequest;
import com.example.backend.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        passwordResetService.forgotPassword(request);

        return ApiResponse.<Void>builder()
                .message("Mã OTP đã được gửi tới emulator")
                .build();
    }

    @PostMapping("/verify-otp")
    public ApiResponse<Boolean> verifyOtp(@RequestBody VerifyOtpRequest request) {
        boolean result = passwordResetService.verifyOtp(request);

        return ApiResponse.<Boolean>builder()
                .message("Xác thực OTP thành công")
                .result(result)
                .build();
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);

        return ApiResponse.<Void>builder()
                .message("Đổi mật khẩu thành công")
                .build();
    }
}