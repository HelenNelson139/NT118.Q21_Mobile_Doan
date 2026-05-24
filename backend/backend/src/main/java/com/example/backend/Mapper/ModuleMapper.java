package com.example.backend.Mapper;

import com.example.backend.dto.teacher.request.ModuleCreationRequest;
import com.example.backend.entity.Module;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ModuleMapper {
    @Mapping(target = "lesson", ignore = true)
    @Mapping(target = "status", ignore = true)
    Module toModule(ModuleCreationRequest request);
}
