package com.example.backend.service;

import com.example.backend.Mapper.ModuleMapper;
import com.example.backend.dto.lesson.request.ModuleCreationRequest;
import com.example.backend.dto.lesson.response.ModuleResponse;
import com.example.backend.entity.Lesson;
import com.example.backend.enums.Status;
import com.example.backend.respository.LessonRepository;
import com.example.backend.respository.ModuleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.backend.entity.Module;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ModuleService {
    ModuleRepository moduleRepository;
    LessonRepository lessonRepository;
    ModuleMapper moduleMapper;

    @Transactional
    public Module createModule(ModuleCreationRequest request) {
        Lesson lesson = lessonRepository.findById(request.getLessonId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học tương ứng!"));

        if (lesson.getStatus() == Status.REJECTED || lesson.getStatus() == Status.PENDING) {
            throw new RuntimeException("Không thể thêm bài học vào khóa học đã bị xóa hoặc đang chờ xóa!");
        }

        Module module = moduleMapper.toModule(request);

        module.setLesson(lesson);
        module.setStatus(Status.PENDING);

        return moduleRepository.save(module);
    }


    public List<ModuleResponse> searchModules(String keyword) {
        List<Module> modules = moduleRepository.findByTitleContainingIgnoreCase(keyword);

        boolean isTeacher = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_TEACHER"));

        if (isTeacher) {
            modules = modules.stream()
                    .filter(m -> m.getStatus() != Status.REJECTED && m.getStatus() != Status.PENDING)
                    .toList();
        }
        return moduleMapper.toModuleResponseList(modules);
    }

    public ModuleResponse getModuleById(Integer id) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bài học không tồn tại hoặc đã bị ẩn!"));

        boolean isTeacher = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_TEACHER"));

        if (isTeacher && (module.getStatus() == Status.REJECTED || module.getStatus() == Status.PENDING)) {
            throw new RuntimeException("Bài học không tồn tại hoặc đã bị ẩn!");
        }
        return moduleMapper.toModuleResponse(module);
    }


    @Transactional
    public void deleteModule(Integer id) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bài học không tồn tại!"));
        module.setStatus(Status.PENDING);
        moduleRepository.save(module);
    }


    @Transactional
    public Module approveModule(Integer id) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bài học không tồn tại!"));
        module.setStatus(Status.ACTIVE);
        return moduleRepository.save(module);
    }

    @Transactional
    public Module approveDeleteModule(Integer id) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bài học không tồn tại!"));

        if (module.getStatus() != Status.PENDING) {
            throw new RuntimeException("Bài học này không nằm trong danh sách yêu cầu xóa!");
        }

        module.setStatus(Status.REJECTED);
        return moduleRepository.save(module);
    }
}

