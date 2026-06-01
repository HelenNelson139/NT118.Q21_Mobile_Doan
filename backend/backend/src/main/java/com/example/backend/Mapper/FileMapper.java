package com.example.backend.Mapper;

import com.example.backend.dto.file.FileUploadRequest;
import com.example.backend.dto.teacher.request.TeacherCreationRequest;
import com.example.backend.entity.Files;
import com.example.backend.entity.Teacher;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface FileMapper {
    @Mapping(target = "id", ignore = true)
    Files toUpload(FileUploadRequest fileUploadRequest);
}
