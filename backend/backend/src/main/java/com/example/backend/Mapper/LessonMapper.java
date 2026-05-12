package com.example.backend.Mapper;

import com.example.backend.dto.teacher.request.LessonCreationRequest;
import com.example.backend.entity.Lesson;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LessonMapper {
    @Mapping(target = "teacher", ignore = true)
    Lesson toLesson(LessonCreationRequest request);
}
