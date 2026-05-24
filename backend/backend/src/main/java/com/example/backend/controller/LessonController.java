package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.teacher.request.LessonCreationRequest;
import com.example.backend.entity.Lesson;
import com.example.backend.service.LessonService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class LessonController {
    LessonService lessonService;

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<Lesson> createLesson(@RequestBody LessonCreationRequest request){
        return ApiResponse.<Lesson>builder()
                .code(1000)
                .message("Create Successful")
                .result(lessonService.createLesson(request))
                .build();
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<List<Lesson>> searchLessons(@RequestParam String keyword) {
        return ApiResponse.<List<Lesson>>builder()
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
    public ApiResponse<Lesson> approveDeleteLesson(@PathVariable Integer id) {
        return ApiResponse.<Lesson>builder()
                .code(1000)
                .message("Admin approved deletion successfully")
                .result(lessonService.approveDeleteLesson(id))
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ApiResponse<Lesson> getLessonById(@PathVariable Integer id) {
        return ApiResponse.<Lesson>builder()
                .code(1000)
                .message("Get Lesson Detail Successful")
                .result(lessonService.getLessonById(id))
                .build();
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Lesson> approveLesson(@PathVariable Integer id) {
        return ApiResponse.<Lesson>builder()
                .code(1000)
                .message("Lesson Approved Successfully")
                .result(lessonService.approveLesson(id))
                .build();
    }
}
