package com.example.backend.respository;

import com.example.backend.entity.Enrollment;
import com.example.backend.entity.EnrollmentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@EnableJpaAuditing
public interface EnrollmentRepository extends JpaRepository<Enrollment, EnrollmentId> {

}