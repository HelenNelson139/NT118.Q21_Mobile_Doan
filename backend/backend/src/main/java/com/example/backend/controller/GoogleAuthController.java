package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.dto.Authentication.response.AuthenticationResponse;
import com.example.backend.dto.Authentication.response.GoogleAuthResponse;
import com.example.backend.dto.user.request.GoogleLoginRequest;
import com.example.backend.service.GoogleAuthService;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class GoogleAuthController {
    GoogleAuthService googleAuthService;

    @PostMapping("/google")
    public GoogleAuthResponse login(@RequestBody GoogleLoginRequest request) throws Exception {
        String token = googleAuthService.authenticate(request.getIdToken());
        return new GoogleAuthResponse(token);
    }

}
