package com.example.backend.config;


import com.example.backend.entity.User;
import com.example.backend.respository.UserResponsitory;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import static com.example.backend.enums.Role.ADMIN;

@Configuration
@FieldDefaults(level = lombok.AccessLevel.PRIVATE,makeFinal = true)
//  @AllArgsConstructor
@RequiredArgsConstructor
@Slf4j
// thêm user Admin khi khởi tạo
public class ApplicationInitConfig {
    UserResponsitory userResponsitory;
    PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner applicationRunner(){
        return args -> {
            if(userResponsitory.findByUsername("admin").isEmpty()){
                User user = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin"))
                        .role(ADMIN)
                        .build();

                userResponsitory.save(user);
                log.info("Admin user created");
            }
        };
    }
}
