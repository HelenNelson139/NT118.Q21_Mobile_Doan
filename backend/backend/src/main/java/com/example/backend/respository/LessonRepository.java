package com.example.backend.respository;

import com.example.backend.entity.Lesson;
import com.example.backend.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Integer> {
    List<Lesson> findByTitleContainingIgnoreCase(String title);

    @Query("""
    SELECT l
    FROM Lesson l
    WHERE (:status IS NULL OR l.status = :status)
    AND (:teacherId IS NULL OR l.teacher.id = :teacherId)
    AND (:keyword IS NULL OR LOWER(l.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
""")
    Page<Lesson> searchLessons(
            @Param("status") Status status,
            @Param("teacherId") Integer teacherId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
    SELECT DISTINCT l
    FROM Lesson l
    WHERE l.status = :pendingStatus
       OR l.id IN (
            SELECT m.lesson.id
            FROM Module m
            WHERE m.status = :pendingStatus
       )
""")
    List<Lesson> findAllPendingOrHasPendingModules(
            @Param("pendingStatus") Status pendingStatus
    );

    List<com.example.backend.entity.Lesson> findByTeacherId(Integer teacherId);
    @Query("SELECT l FROM Lesson l WHERE l.id NOT IN " +
            "(SELECT e.lesson.id FROM Enrollment e WHERE e.id.userId = :studentId)")
    List<Lesson> findLessonsNotEnrolledByStudentId(@Param("studentId") Integer studentId);
}
