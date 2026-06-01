package com.example.backend.service;

import com.example.backend.dto.file.FileResponse;
import com.example.backend.dto.file.FileUploadRequest;
import com.example.backend.entity.*;
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

    public void uploadFile(FileUploadRequest fileUploadRequest){
        Files file = new Files();
        Module module = moduleRepository.findById(fileUploadRequest.getModule_id()).orElseThrow(() -> new AppException(ErrorCode.MODULE_NOT_FOUND));
        file.setModule(module);
        file.setFile_name(fileUploadRequest.getFile_name());
        MultipartFile files  = fileUploadRequest.getFile();
        if(files != null && !files.isEmpty()){
            String file_url = supabaseStorageService.uploadModuleFile(
                    files,
                    "files/" + file.getId()
            );
            file.setFile_url(file_url);
            fileRepository.save(file);
        }
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
