package com.example.backend.respository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.backend.entity.Module;

import java.util.List;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Integer> {
    List<Module> findByTitleContainingIgnoreCase(String title);
    List<Module> findByLessonId(Integer lessonId);
}
