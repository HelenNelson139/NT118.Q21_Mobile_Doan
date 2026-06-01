package com.example.backend.respository;

import com.example.backend.entity.Files;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.relational.core.sql.In;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@EnableJpaAuditing
public interface FileRepository extends JpaRepository<Files, Integer> {
    List<Files> findByModule_Id(Integer moduleId);
}
