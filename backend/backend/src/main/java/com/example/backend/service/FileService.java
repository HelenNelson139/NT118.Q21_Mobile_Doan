package com.example.backend.service;

import com.example.backend.dto.file.FileResponse;
import com.example.backend.dto.file.FileUploadRequest;
import com.example.backend.entity.Files;
import com.example.backend.entity.Module;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.respository.FileRepository;
import com.example.backend.respository.ModuleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@AllArgsConstructor
public class FileService {

    private final SupabaseStorageService supabaseStorageService;
    private final ModuleRepository moduleRepository;
    private final FileRepository fileRepository;

    public void uploadFile(FileUploadRequest fileUploadRequest) {

        if (fileUploadRequest.getModule_id() == null) {
            throw new RuntimeException("module_id không được để trống");
        }

        Module module = moduleRepository.findById(fileUploadRequest.getModule_id())
                .orElseThrow(() -> new AppException(ErrorCode.MODULE_NOT_FOUND));

        MultipartFile multipartFile = fileUploadRequest.getFile();

        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new RuntimeException("File không được để trống");
        }

        String fileName = fileUploadRequest.getFile_name();

        if (fileName == null || fileName.isBlank()) {
            fileName = multipartFile.getOriginalFilename();
        }

        if (fileName == null || fileName.isBlank()) {
            fileName = "module_file";
        }

        /*
         * Quan trọng:
         * Dùng uploadModuleFile(), không dùng uploadFile().
         * uploadFile() của bạn là hàm chỉ dành cho avatar/image.
         */
        String fileUrl = supabaseStorageService.uploadModuleFile(
                multipartFile,
                "modules/" + module.getId()
        );

        Files file = new Files();
        file.setModule(module);
        file.setFile_name(fileName);
        file.setFile_url(fileUrl);

        fileRepository.save(file);
    }

    public List<FileResponse> getFilesByModule(Integer moduleId) {
        List<Files> files = fileRepository.findByModule_Id(moduleId);

        return files.stream()
                .map(file -> FileResponse.builder()
                        .file_name(file.getFile_name())
                        .file_url(file.getFile_url())
                        .build())
                .toList();
    }
}