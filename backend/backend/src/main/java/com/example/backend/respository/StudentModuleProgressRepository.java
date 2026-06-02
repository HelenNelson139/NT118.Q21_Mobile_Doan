package com.example.backend.respository;

import com.example.backend.entity.StudentModuleProgress;
import com.example.backend.enums.ProgressStatus;
import com.example.backend.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StudentModuleProgressRepository
        extends JpaRepository<StudentModuleProgress, Integer> {

    Optional<StudentModuleProgress> findByStudent_IdAndModule_Id(
            Integer studentId,
            Integer moduleId
    );

    @Query("""
        SELECT COUNT(p)
        FROM StudentModuleProgress p
        WHERE p.student.id = :studentId
          AND p.module.lesson.id = :lessonId
          AND p.module.status = :moduleStatus
          AND p.status = :progressStatus
    """)
    long countCompletedModulesByStudentAndLesson(
            @Param("studentId") Integer studentId,
            @Param("lessonId") Integer lessonId,
            @Param("moduleStatus") Status moduleStatus,
            @Param("progressStatus") ProgressStatus progressStatus
    );
}