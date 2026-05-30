package com.example.backend.Mapper;

import com.example.backend.dto.lesson.request.LessonCreationRequest;
import com.example.backend.dto.lesson.response.LessonResponse;
import com.example.backend.entity.Lesson;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LessonMapper {
    @Mapping(target = "teacher", ignore = true)
    @Mapping(target = "thumbnail_url", ignore = true)
    Lesson toLesson(LessonCreationRequest request);
    @Mapping(source = "teacher.user.full_name", target = "teacher_name")
    LessonResponse toLessonResponse(Lesson lesson);
    List<LessonResponse> toLessonResponseList(List<Lesson> lessons);
}
