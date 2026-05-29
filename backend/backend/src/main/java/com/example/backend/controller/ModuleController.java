package com.example.backend.controller;

import com.example.backend.dto.lesson.request.ModuleCreationRequest;
import com.example.backend.dto.ApiResponse;
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
    public ApiResponse<Module> createModule(@ModelAttribute ModuleCreationRequest request) {
        return ApiResponse.<Module>builder()
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
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
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
    public ApiResponse<Module> approveModule(@PathVariable Integer id) {
        return ApiResponse.<Module>builder()
                .code(1000)
                .message("Module approved successfully")
                .result(moduleService.approveModule(id))
                .build();
    }

    @PutMapping("/{id}/approve-delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Module> approveDeleteModule(@PathVariable Integer id) {
        return ApiResponse.<Module>builder()
                .code(1000)
                .message("Admin approved module deletion successfully")
                .result(moduleService.approveDeleteModule(id))
                .build();
    }
}
