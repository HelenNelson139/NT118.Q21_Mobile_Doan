package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.lesson.request.LessonCreationRequest;
import com.example.backend.dto.lesson.response.LessonResponse;
import com.example.backend.entity.Lesson;
import com.example.backend.enums.Status;
import com.example.backend.respository.UserResponsitory;
import com.example.backend.service.LessonService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class LessonController {
    LessonService lessonService;
    private final JsonMapper.Builder builder;
    @Autowired
    private UserResponsitory userRepository;


    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<LessonResponse> createLesson(@ModelAttribute LessonCreationRequest request){
        return ApiResponse.<LessonResponse>builder()
                .code(1000)
                .message("Create Successful")
                .result(lessonService.createLesson(request))
                .build();
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'STUDENT')")
    public ApiResponse<List<LessonResponse>> searchLessons(@RequestParam String keyword) {
        return ApiResponse.<List<LessonResponse>>builder()
                .code(1000)
                .message("Search Successful")
                .result(lessonService.searchLessons(keyword))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<String> deleteLesson(@PathVariable Integer id) {
        lessonService.deleteLesson(id);
        return ApiResponse.<String>builder()
                .code(1000)
                .message("Delete request has been sent, please wait.")
                .result("Lesson ID"  + id + " status changed to PENDING_DELETE.")
                .build();
    }

    @PutMapping("/{id}/approve-delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<LessonResponse> approveDeleteLesson(@PathVariable Integer id) {
        return ApiResponse.<LessonResponse>builder()
                .code(1000)
                .message("Admin approved deletion successfully")
                .result(lessonService.approveDeleteLesson(id))
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ApiResponse<LessonResponse> getLessonById(@PathVariable Integer id) {
        return ApiResponse.<LessonResponse>builder()
                .code(1000)
                .message("Get Lesson Detail Successful")
                .result(lessonService.getLessonById(id))
                .build();
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<LessonResponse> approveLesson(@PathVariable Integer id) {
        return ApiResponse.<LessonResponse>builder()
                .code(1000)
                .message("Lesson Approved Successfully")
                .result(lessonService.approveLesson(id))
                .build();
    }
    @GetMapping("/  all")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ApiResponse<List<LessonResponse>> getAllLessons() {
        return ApiResponse.<List<LessonResponse>>builder()
                .code(1000)
                .message("Get All of Lessons")
                .result(lessonService.findAllLesson())
                .build();
    }

    //pagination testing
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ApiResponse<Page<Lesson>> getLessons(
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) Integer teacherId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.<Page<Lesson>>builder()
                .code(1000)
                .message("Get Lessons Successfully")
                .result(lessonService.getLessons(status, teacherId, keyword, page, size))
                .build();
    }

    @GetMapping("/my-lessons")
    public ApiResponse<List<LessonResponse>> getMyLessons() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        com.example.backend.entity.User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User không tồn tại!"));
        Integer teacherId = user.getId();
        List<LessonResponse> teacherLessons = lessonService.getLessonsByTeacherId(teacherId);
        return ApiResponse.<List<LessonResponse>>builder()
                .code(1000)
                .message("Lấy danh sách bài học của giảng viên thành công")
                .result(teacherLessons)
                .build();
    }
}
