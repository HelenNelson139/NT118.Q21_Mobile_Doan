package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.teacher.request.TeacherCreationRequest;
import com.example.backend.dto.teacher.request.UpdateTeacherRequest;
import com.example.backend.dto.teacher.response.TeacherResponseProfile;
import com.example.backend.dto.user.request.CreateUserRequest;
import com.example.backend.dto.user.request.UpdateUserRequest;
import com.example.backend.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static java.util.stream.DoubleStream.builder;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {
    @Autowired
    private TeacherService teacherService;
    @PostMapping("/register")
    public ApiResponse<String> createTeacher(@ModelAttribute TeacherCreationRequest teacherCreationRequest){
        teacherService.register(teacherCreationRequest);
        return ApiResponse.<String>builder()
                .code(1000)
                .message("Đăng ký giáo viên thành công")
                .build();
           }


    @PatchMapping("/update")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ApiResponse<String> updateTeacher(@RequestBody UpdateTeacherRequest updateTeacherRequest, @RequestParam Integer userId){
        teacherService.update(userId, updateTeacherRequest);
        return ApiResponse.<String>builder()
                .code(10000)
                .message("Đã cập nhật thông tin giảng viên")
                .result("Mã giảng viên được cập nhật " + userId)
                .build();
    }

    @GetMapping("/get")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public TeacherResponseProfile getTeacherProfile(@RequestParam Integer userId){
        return teacherService.getUserProfile(userId);
    }

    @GetMapping("/{lessonId}/students")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ApiResponse<List<Integer>> getStudentLessons(
            @PathVariable Integer lessonId
    ) {
        return ApiResponse.<List<Integer>>builder()
                .code(1000)
                .message("Get student by lesson ids successful")
                .result(teacherService.getStudentIdsByLessonId(lessonId))
                .build();
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ApiResponse<List<TeacherResponseProfile>> getAllTeachers() {
        return ApiResponse.<List<TeacherResponseProfile>>builder()
                .code(1000)
                .message("Get all teachers successful")
                .result(teacherService.getAllTeachers())
                .build();
    }


}
