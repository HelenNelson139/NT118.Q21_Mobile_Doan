package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.student.request.CreateStudentRequest;
import com.example.backend.dto.student.request.UpdateStudentRequest;
import com.example.backend.dto.student.response.StudentResponseProfile;
import com.example.backend.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
public class StudentController {
    @Autowired
    private StudentService studentService;
    @PostMapping("/register")
    public ApiResponse<String> registerStudent(@ModelAttribute CreateStudentRequest createStudentRequest){
        studentService.register(createStudentRequest);
        return ApiResponse.<String>builder()
                .code(1000)
                .message("Đăng ký học viên thành công")
                .result("Dữ liệu đã được lưu cho mã: " + createStudentRequest.getStudent_code())
                .build();
    }

    @PatchMapping("/update")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ApiResponse<String> updateStudent(@RequestBody UpdateStudentRequest updateStudentRequest, @RequestParam Integer userId){
        studentService.update(userId, updateStudentRequest);
        return ApiResponse.<String>builder()
                .code(10000)
                .message("Thay đổi thông tin học sinh thành công")
                .result("Dữ liệu thay đổi cho mã sinh viên " + userId)
                .build();
    }

    @GetMapping("/get")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public StudentResponseProfile getStudentProfile(@RequestParam Integer userId){
        return studentService.getUserProfile(userId);
    }
}
