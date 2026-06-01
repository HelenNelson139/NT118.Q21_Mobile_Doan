package com.example.backend.service;


import com.example.backend.dto.user.request.ChangePasswordRequest;
import com.example.backend.dto.user.request.CreateUserRequest;
import com.example.backend.entity.User;
import com.example.backend.enums.Status;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.respository.UserResponsitory;
import com.google.common.collect.Multimap;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@AllArgsConstructor //cần phải khởi tạo biến private final liền sau đó không thay đổi biến này nữa nên dùng
public class UserService {

    @Autowired
    private UserResponsitory userResponsitory;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private SupabaseStorageService supabaseStorageService;
    public boolean checkUserExist(CreateUserRequest createUserRequest) {
        if (userResponsitory.findByUsername(createUserRequest.getUsername()).isPresent()) {
            throw new RuntimeException("User already exist");
        }else if (userResponsitory.findByEmail(createUserRequest.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exist");
        }else if (userResponsitory.findByPhone(createUserRequest.getPhone()).isPresent()) {
            throw new RuntimeException("Phone already exist");
        }else{
            return true;
        }
    }

    public void updateAvatar(Integer userId, MultipartFile avatar){
        User user = userResponsitory.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String avatarUrl = supabaseStorageService.uploadFile(
                avatar,
                "users/" + userId
        );

        user.setAvatar_url(avatarUrl);
        userResponsitory.save(user);
    }

    @Transactional
    public void changePassword(Integer userId, ChangePasswordRequest changePasswordRequest){
        User user = userResponsitory.findById(userId).orElseThrow(()->new AppException(ErrorCode.USER_NOT_FOUND));

        if(!passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword())){
            throw new AppException(ErrorCode.PASSWORD_ERROR);
        }

        if(passwordEncoder.matches(changePasswordRequest.getNewPassword(), user.getPassword())){
            throw new AppException(ErrorCode.PASSWORD_CHECK);
        }

        user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        userResponsitory.save(user);
    }

    @Transactional
    public void deleteUser(Integer userId){
        User user = userResponsitory.findById(userId).orElseThrow(()->new AppException(ErrorCode.USER_NOT_FOUND));
        user.setStatus("DELETED");
    }
}
