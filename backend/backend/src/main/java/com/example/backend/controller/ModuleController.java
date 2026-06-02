package com.example.backend.controller;

import com.example.backend.dto.lesson.request.ModuleCreationRequest;
import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.lesson.response.LessonResponse;
import com.example.backend.dto.lesson.response.ModuleResponse;
import com.example.backend.entity.Module;
import com.example.backend.service.ModuleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ModuleController {

    ModuleService moduleService;

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<ModuleResponse> createModule(@ModelAttribute ModuleCreationRequest request) {
        return ApiResponse.<ModuleResponse>builder()
                .code(1000)
                .message("Create Module Successful")
                .result(moduleService.createModule(request))
                .build();
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<List<ModuleResponse>> searchModules(@RequestParam String keyword) {
        return ApiResponse.<List<ModuleResponse>>builder()
                .code(1000)
                .message("Search Modules Successful")
                .result(moduleService.searchModules(keyword))
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'STUDENT')")
    public ApiResponse<ModuleResponse> getModuleById(@PathVariable Integer id) {
        return ApiResponse.<ModuleResponse>builder()
                .code(1000)
                .message("Get Module Detail Successful")
                .result(moduleService.getModuleById(id))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<String> deleteModule(@PathVariable Integer id) {
        moduleService.deleteModule(id);
        return ApiResponse.<String>builder()
                .code(1000)
                .message("Yêu cầu xóa bài học đã được gửi lên hệ thống.")
                .result("Module ID " + id + " status changed to PENDING_DELETE.")
                .build();
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ModuleResponse> approveModule(@PathVariable Integer id) {
        return ApiResponse.<ModuleResponse>builder()
                .code(1000)
                .message("Module approved successfully")
                .result(moduleService.approveModule(id))
                .build();
    }

    @PutMapping("/{id}/approve-delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ModuleResponse> approveDeleteModule(@PathVariable Integer id) {
        return ApiResponse.<ModuleResponse>builder()
                .code(1000)
                .message("Admin approved module deletion successfully")
                .result(moduleService.approveDeleteModule(id))
                .build();
    }

    @GetMapping("/lesson/{lessonId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'STUDENT')")  
    public ApiResponse<List<ModuleResponse>> getModulesByLessonId(@PathVariable Integer lessonId) {
        return ApiResponse.<List<ModuleResponse>>builder()
                .code(1000)
                .message("Get Modules by Lesson ID Successful")
                .result(moduleService.getModulesByLessonId(lessonId))
                .build();
    }

    @GetMapping("/lesson/pending/{lessonId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ApiResponse<List<ModuleResponse>> getPendingModulesByLessonId(@PathVariable Integer lessonId) {
        return ApiResponse.<List<ModuleResponse>>builder()
                .code(1000)
                .message("Get Modules by Lesson ID Successful")
                .result(moduleService.getPendingModulesByLesson(lessonId))
                .build();
    }

    @GetMapping("/allPending")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<List<ModuleResponse>> getAllModulesPending() {
        return ApiResponse.<List<ModuleResponse>>builder()
                .code(1000)
                .message("Get All of Modules")
                .result(moduleService.findAllModulePending())
                .build();
    }


}
