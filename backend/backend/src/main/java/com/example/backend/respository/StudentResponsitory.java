package com.example.backend.respository;

import com.example.backend.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@EnableJpaAuditing
public interface StudentResponsitory extends JpaRepository<Student, Integer> {
    @Query(value = """
        SELECT student_code
        FROM students
        WHERE student_code LIKE CONCAT(:prefix, '%')
        ORDER BY student_code DESC
        LIMIT 1
    """, nativeQuery = true)
    String findLatestStudentCode(@Param("prefix") String prefix);

}
