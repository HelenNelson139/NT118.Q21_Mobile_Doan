package com.example.backend.respository;

import com.example.backend.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.backend.entity.Module;

import java.util.List;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Integer> {
    List<Module> findByTitleContainingIgnoreCase(String title);
    List<Module> findByLessonId(Integer lessonId);
    List<Module> findByLessonIdAndStatusNot(Integer lessonId, Status status);
}
