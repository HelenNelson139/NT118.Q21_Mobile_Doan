package com.example.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Service
@RequiredArgsConstructor
public class EmulatorSmsService {

    @Value("${app.adb.path:adb}")
    private String adbPath;

    @Value("${app.sms.from-phone:0909123456}")
    private String fromPhone;

    public void sendOtpToEmulator(String otp) {
        String message = "Ma OTP cua ban la " + otp;

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    adbPath,
                    "emu",
                    "sms",
                    "send",
                    fromPhone,
                    message
            );

            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            StringBuilder output = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            )) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("ADB gửi SMS thất bại. Output: " + output);
            }

        } catch (Exception e) {
            throw new RuntimeException("Không thể gửi SMS vào emulator bằng ADB: " + e.getMessage());
        }
    }
}