package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.teacher.request.LessonCreationRequest;
import com.example.backend.entity.Lesson;
import com.example.backend.service.LessonService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
