package com.example.backend.respository;

import com.example.backend.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@EnableJpaAuditing
public interface TeacherResponsitory extends JpaRepository<Teacher, Integer> {
    Optional<Teacher> findByUserEmail(String email);

}
