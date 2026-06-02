package com.example.backend.service;

import com.example.backend.dto.lesson.response.LessonProgressResponse;
import com.example.backend.entity.StudentModuleProgress;
import com.example.backend.enums.ProgressStatus;
import com.example.backend.enums.Status;
import com.example.backend.respository.ModuleRepository;
import com.example.backend.respository.StudentModuleProgressRepository;
import com.example.backend.respository.UserResponsitory;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StudentModuleProgressService {
    ModuleRepository moduleRepository;
    StudentModuleProgressRepository studentModuleProgressRepository;
    UserResponsitory userRepository;
    public boolean isLessonCompleted(Integer studentId, Integer lessonId) {
        long totalActiveModules = moduleRepository
                .countByLesson_IdAndStatus(lessonId, Status.ACTIVE);

        if (totalActiveModules == 0) {
            return false;
        }

        long completedModules = studentModuleProgressRepository
                .countCompletedModulesByStudentAndLesson(
                        studentId,
                        lessonId,
                        Status.ACTIVE,
                        ProgressStatus.COMPLETED
                );

        return completedModules == totalActiveModules;
    }

    public LessonProgressResponse getLessonProgress(Integer studentId, Integer lessonId) {
        long totalActiveModules = moduleRepository
                .countByLesson_IdAndStatus(lessonId, Status.ACTIVE);

        long completedModules = studentModuleProgressRepository
                .countCompletedModulesByStudentAndLesson(
                        studentId,
                        lessonId,
                        Status.ACTIVE,
                        ProgressStatus.COMPLETED
                );

        double progressPercent = 0;

        if (totalActiveModules > 0) {
            progressPercent = (completedModules * 100.0) / totalActiveModules;
        }

        boolean completed = totalActiveModules > 0 && completedModules == totalActiveModules;

        return LessonProgressResponse.builder()
                .lessonId(lessonId)
                .studentId(studentId)
                .totalModules(totalActiveModules)
                .completedModules(completedModules)
                .progressPercent(progressPercent)
                .completed(completed)
                .build();
    }
    @Transactional
    public void completeModule(Integer studentId, Integer moduleId) {
        StudentModuleProgress progress = studentModuleProgressRepository
                .findByStudent_IdAndModule_Id(studentId, moduleId)
                .orElse(StudentModuleProgress.builder()
                        .student(userRepository.findById(studentId)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy student")))
                        .module(moduleRepository.findById(moduleId)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy module")))
                        .build());

        progress.setStatus(ProgressStatus.COMPLETED);
        progress.setCompletedAt(LocalDateTime.now());

        studentModuleProgressRepository.save(progress);
    }
}
