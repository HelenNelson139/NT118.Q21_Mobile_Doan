package com.example.backend.dto.file;

import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class FileUploadRequest {
    Integer module_id;
    String file_name;
    MultipartFile file;
}
