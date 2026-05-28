package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.user.request.ChangePasswordRequest;
import com.example.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private  UserService userService;

    @PatchMapping("/delete")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<String> deleteUser(Integer userId){
        userService.deleteUser(userId);
        return ApiResponse.<String>builder()
                .code(1000)
                .message("Xoá thành công")
                .result("Đã xoá thành công " + userId)
                .build();
    }

    @PatchMapping("/password")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'STUDENT')")
    public ApiResponse<String> changePassword( @RequestParam Integer userId, @RequestBody ChangePasswordRequest changePasswordRequest){
        userService.changePassword(userId, changePasswordRequest);
        return ApiResponse.<String>builder()
                .code(1000)
                .message("Đổi mật khẩu thành công")
                .result("Đổi mật khẩu thành công " + userId)
                .build();
    }
}
