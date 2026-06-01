package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.student.request.CreateStudentRequest;
import com.example.backend.dto.student.request.EnrollmentRequest;
import com.example.backend.dto.student.request.UpdateStudentRequest;
import com.example.backend.dto.student.response.StudentResponseProfile;
import com.example.backend.dto.teacher.response.TeacherResponseProfile;
import com.example.backend.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ApiResponse<List<StudentResponseProfile>> getAllStudents() {
        return ApiResponse.<List<StudentResponseProfile>>builder()
                .code(1000)
                .message("Get all students successful")
                .result(studentService.getAllStudents())
                .build();
    }

    @PostMapping("/course")
    @PreAuthorize("hasAnyRole('STUDENT')")
    public ApiResponse<String> enrollCourse(@RequestBody EnrollmentRequest enrollmentRequest){
        studentService.enrollLesson(enrollmentRequest);
        return ApiResponse.<String>builder()
                .code(1000)
                .message("Đăng ký lớp học thành công")
                .result("Sinh viên đăng ký lớp học thành công "+ enrollmentRequest.getLessonId())
                .build();
    }

    @GetMapping("/{studentId}/lessons")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ApiResponse<List<Integer>> getStudentLessons(
            @PathVariable Integer studentId
    ) {
        return ApiResponse.<List<Integer>>builder()
                .code(1000)
                .message("Get student lesson ids successful")
                .result(studentService.getStudentlessonByStudentId(studentId))
                .build();
    }

    @GetMapping("/{studentId}/lessons_not_enroll")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ApiResponse<List<Integer>> getStudentLessonsNotEnroll(
            @PathVariable Integer studentId
    ){
        return ApiResponse.<List<Integer>>builder()
                .code(1000)
                .message("Get student lesson not enroll ids successful")
                .result(studentService.getUnenrolledLessonsByStudentId(studentId))
                .build();
    }
}
