package com.example.backend.service;

import com.example.backend.dto.Authentication.request.ForgotPasswordRequest;
import com.example.backend.dto.Authentication.request.ResetPasswordRequest;
import com.example.backend.dto.Authentication.request.VerifyOtpRequest;
import com.example.backend.entity.PasswordResetOtp;
import com.example.backend.entity.User;
import com.example.backend.respository.PasswordResetOtpRepository;
import com.example.backend.respository.UserResponsitory;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserResponsitory userRepository;
    private final PasswordResetOtpRepository passwordResetOtpRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmulatorSmsService emulatorSmsService;

    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new RuntimeException("Số điện thoại không tồn tại"));

        String otp = generateOtp();

        PasswordResetOtp passwordResetOtp = PasswordResetOtp.builder()
                .phone(user.getPhone())
                .otp(otp)
                .expiredAt(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .build();

        passwordResetOtpRepository.save(passwordResetOtp);

        emulatorSmsService.sendOtpToEmulator(otp);
    }

    public boolean verifyOtp(VerifyOtpRequest request) {
        PasswordResetOtp otpEntity = passwordResetOtpRepository
                .findTopByPhoneAndUsedFalseOrderByIdDesc(request.getPhone())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mã OTP"));

        if (otpEntity.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Mã OTP đã hết hạn");
        }

        if (!otpEntity.getOtp().equals(request.getOtp())) {
            throw new RuntimeException("Mã OTP không đúng");
        }

        return true;
    }

    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetOtp otpEntity = passwordResetOtpRepository
                .findTopByPhoneAndUsedFalseOrderByIdDesc(request.getPhone())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mã OTP"));

        if (otpEntity.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Mã OTP đã hết hạn");
        }

        if (!otpEntity.getOtp().equals(request.getOtp())) {
            throw new RuntimeException("Mã OTP không đúng");
        }

        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new RuntimeException("Số điện thoại không tồn tại"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        otpEntity.setUsed(true);
        passwordResetOtpRepository.save(otpEntity);
    }

    private String generateOtp() {
        Random random = new Random();
        int number = 100000 + random.nextInt(900000);
        return String.valueOf(number);
    }
}