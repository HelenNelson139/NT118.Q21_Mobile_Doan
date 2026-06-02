package com.example.backend.respository;

import com.example.backend.dto.lesson.response.LessonStudentCountResponse;
import com.example.backend.entity.Enrollment;
import com.example.backend.entity.EnrollmentId;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@EnableJpaAuditing
public interface EnrollmentRepository extends JpaRepository<Enrollment, EnrollmentId> {
    List<Enrollment> findById_UserId(Integer userId);
    List<Enrollment> findById_LessonId(Integer lessonId);
    @Query("""
        SELECT new com.example.backend.dto.lesson.response.LessonStudentCountResponse(
            e.id.lessonId, 
            e.lesson.title, 
            COUNT(e.id.userId)
        )
        FROM Enrollment e
        GROUP BY e.id.lessonId, e.lesson.title
        ORDER BY COUNT(e.id.userId) DESC
    """)
    List<LessonStudentCountResponse> countStudentsPerLesson();
    Long countById_LessonId(Integer lessonId);
}
