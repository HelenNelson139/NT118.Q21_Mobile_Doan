package com.example.backend.service;
import com.example.backend.entity.User;
import com.example.backend.respository.UserResponsitory;
import com.example.backend.service.AuthenticationService;
import com.google.api.client.googleapis.auth.oauth2.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.util.Map;


@Service
public class GoogleAuthService {
    private final UserResponsitory userRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private AuthenticationService authenticationService;

    public GoogleAuthService(UserResponsitory userRepository) {
        this.userRepository = userRepository;
    }

    public String authenticate(String idToken) {

        String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;

        Map response = restTemplate.getForObject(url, Map.class);

        if (response == null || response.get("email") == null) {
            throw new RuntimeException("Invalid Google token");
        }

        String email = (String) response.get("email");
        String name = (String) response.get("name");

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setFull_name(name);
            user = userRepository.save(user);
        }

        return authenticationService.generateToken(user);
    }
}
