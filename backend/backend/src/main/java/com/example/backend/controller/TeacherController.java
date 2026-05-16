package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.teacher.request.TeacherCreationRequest;
import com.example.backend.dto.user.request.CreateUserRequest;
import com.example.backend.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static java.util.stream.DoubleStream.builder;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {
    @Autowired
    private TeacherService teacherService;
    @PostMapping("/register")
    public ApiResponse<String> createTeacher(@RequestBody TeacherCreationRequest teacherCreationRequest){
        teacherService.register(teacherCreationRequest);
        return ApiResponse.<String>builder()
                .code(1000)
                .message("Đăng ký giáo viên thành công")
                .result("Dữ liệu đã được lưu cho mã: " + teacherCreationRequest.getTeacher_code())
                .build();
           }

}
