package com.example.backend.service;

import com.example.backend.Mapper.LessonMapper;
import com.example.backend.dto.teacher.request.LessonCreationRequest;
import com.example.backend.entity.Lesson;
import com.example.backend.entity.Teacher;
import com.example.backend.respository.LessonRepository;
import com.example.backend.respository.TeacherResponsitory;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

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

        //  Tìm Teacher dựa trên thông tin đăng nhập
        Teacher teacher = teacherResponsitory.findByUserUsername(name)
                .orElseThrow(() -> new RuntimeException("Not have permission"));

        //  Map và lưu
        Lesson lesson = lessonMapper.toLesson(request);
        lesson.setTeacher(teacher);

        return lessonRepository.save(lesson);
    }

    public List<Lesson> searchLessons(String keyword){
        return lessonRepository.findByTitleContainingIgnoreCase(keyword);
    }

    @Transactional
    public void deleteLesson(Integer id){
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khóa học không tồn tại hoặc đã bị xóa!") );
        lessonRepository.delete(lesson);
    }
}
