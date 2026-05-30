package com.example.backend.respository;

import com.example.backend.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

@Repository
@EnableJpaAuditing
public interface TeacherResponsitory extends JpaRepository<Teacher, Integer> {
    //Optional<Teacher> findByUserEmail(String email);
    @Query(value = """
        SELECT teacher_code
        FROM teachers
        WHERE teacher_code LIKE CONCAT(:prefix, '%')
        ORDER BY teacher_code DESC
        LIMIT 1
    """, nativeQuery = true)
    String findLatestTeacherCode(@Param("prefix") String prefix);
    Optional<Teacher> findByUserUsername(String username);

}
