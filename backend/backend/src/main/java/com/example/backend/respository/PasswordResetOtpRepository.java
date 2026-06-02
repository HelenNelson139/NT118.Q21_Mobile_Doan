package com.example.backend.respository;

import com.example.backend.entity.PasswordResetOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Integer> {

    Optional<PasswordResetOtp> findTopByPhoneAndUsedFalseOrderByIdDesc(String phone);
}