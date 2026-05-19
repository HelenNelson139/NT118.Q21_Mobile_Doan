package com.example.backend.respository;

import com.example.backend.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Integer> {
    List<Lesson> findByTitleContainingIgnoreCase(String title);
}
