package com.example.backend.service;

import com.example.backend.Mapper.LessonMapper;
import com.example.backend.dto.teacher.request.LessonCreationRequest;
import com.example.backend.entity.Lesson;
import com.example.backend.entity.Teacher;
import com.example.backend.respository.LessonRepository;
import com.example.backend.respository.TeacherResponsitory;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class LessonService {
    LessonRepository lessonRepository;
    TeacherResponsitory teacherResponsitory;
    LessonMapper lessonMapper;

    public Lesson createLesson(LessonCreationRequest request){
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        // 2. Tìm Teacher dựa trên thông tin đăng nhập
        Teacher teacher = teacherResponsitory.findByUserUsername(name)
                .orElseThrow(() -> new RuntimeException("Not have permission"));

        // 3. Map và lưu
        Lesson lesson = lessonMapper.toLesson(request);
        lesson.setTeacher(teacher);

        return lessonRepository.save(lesson);
    }
}
