package com.example.backend.Mapper;

import com.example.backend.dto.lesson.request.ModuleCreationRequest;
import com.example.backend.dto.lesson.response.ModuleResponse;
import com.example.backend.entity.Module;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ModuleMapper {
    @Mapping(target = "lesson", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "image_example_url", ignore = true)
    Module toModule(ModuleCreationRequest request);
    @Mapping(source = "lesson.id", target = "lessonId")
    ModuleResponse toModuleResponse(Module module);
    List<ModuleResponse> toModuleResponseList(List<Module> modules);
}
