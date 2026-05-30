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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ModuleService {
    ModuleRepository moduleRepository;
    LessonRepository lessonRepository;
    ModuleMapper moduleMapper;
    SupabaseStorageService supabaseStorageService;

    @Transactional
    public ModuleResponse createModule(ModuleCreationRequest request) {
        Lesson lesson = lessonRepository.findById(request.getLessonId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học tương ứng!"));

        if (lesson.getStatus() == Status.REJECTED || lesson.getStatus() == Status.PENDING) {
            throw new RuntimeException("Không thể thêm bài học vào khóa học đã bị xóa hoặc đang chờ xóa!");
        }

        Module module = moduleMapper.toModule(request);
        module.setLesson(lesson);
        module.setStatus(Status.PENDING);
        MultipartFile image = request.getImage();
        if(image != null && !image.isEmpty()){
            String image_url = supabaseStorageService.uploadFile(
                    image,
                    "modules/" + module.getId()
            );
            module.setImage_example_url(image_url);
        }
        Module savedModule = moduleRepository.save(module);
        return moduleMapper.toModuleResponse(savedModule);
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
    public ModuleResponse approveModule(Integer id) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bài học không tồn tại!"));
        module.setStatus(Status.ACTIVE);
        Module savedModule = moduleRepository.save(module);
        return moduleMapper.toModuleResponse(savedModule);
    }

    @Transactional
    public ModuleResponse approveDeleteModule(Integer id) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bài học không tồn tại!"));

        if (module.getStatus() != Status.PENDING) {
            throw new RuntimeException("Bài học này không nằm trong danh sách yêu cầu xóa!");
        }

        module.setStatus(Status.REJECTED);
        Module savedModule = moduleRepository.save(module);
        return moduleMapper.toModuleResponse(savedModule);
    }
}

