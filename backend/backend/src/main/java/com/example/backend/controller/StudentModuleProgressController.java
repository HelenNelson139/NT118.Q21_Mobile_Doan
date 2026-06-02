package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.lesson.response.LessonProgressResponse;
import com.example.backend.service.LessonService;
import com.example.backend.service.StudentModuleProgressService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/progress")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StudentModuleProgressController {
    @Autowired
    StudentModuleProgressService studentModuleProgressService;
    @GetMapping("/{lessonId}/{student_Id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<LessonProgressResponse> getLessonProgress(@PathVariable Integer lessonId, @PathVariable Integer student_Id) {

        return ApiResponse.<LessonProgressResponse>builder()
                .result(studentModuleProgressService.getLessonProgress(student_Id, lessonId))
                .build();
    }

    @PostMapping("/modules/{moduleId}/{student_Id}/complete")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<Void> completeModule(
            @PathVariable Integer moduleId, @PathVariable Integer student_Id
    ) {
        studentModuleProgressService.completeModule(student_Id, moduleId);

        return ApiResponse.<Void>builder()
                .message("Đã đánh dấu hoàn thành module")
                .build();
    }
}
