package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.file.FileResponse;
import com.example.backend.dto.file.FileUploadRequest;
import com.example.backend.entity.Files;
import com.example.backend.respository.FileRepository;
import com.example.backend.service.FileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class FileController {
    FileService fileService;
    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('TEACHER')")
    public ApiResponse<String> uploadFile(@ModelAttribute FileUploadRequest fileUploadRequest){
        fileService.uploadFile(fileUploadRequest);
        return ApiResponse.<String>builder()
                .code(1000)
                .message("Upload file successful")
                .result("Upload file successful")
                .build();
    }

    @GetMapping("/get/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    public List<FileResponse> getFiles(@PathVariable Integer id) {
        return fileService.getFilesByModule(id);
    }
}
