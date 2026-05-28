package com.example.backend.service;

import com.example.backend.Mapper.LessonMapper;
import com.example.backend.dto.lesson.request.LessonCreationRequest;
import com.example.backend.dto.lesson.response.LessonResponse;
import com.example.backend.entity.Lesson;
import com.example.backend.entity.Teacher;
import com.example.backend.enums.Status;
import com.example.backend.respository.LessonRepository;
import com.example.backend.respository.TeacherResponsitory;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class LessonService {
    LessonRepository lessonRepository;
    TeacherResponsitory teacherResponsitory;
    LessonMapper lessonMapper;

    public Lesson createLesson(LessonCreationRequest request){
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        //  Tìm Teacher dựa trên thông tin đăng nhập
        Teacher teacher = teacherResponsitory.findByUserUsername(name)
                .orElseThrow(() -> new RuntimeException("Not have permission"));

        Lesson lesson = lessonMapper.toLesson(request);
        lesson.setTeacher(teacher);
        lesson.setStatus(Status.PENDING);

        return lessonRepository.save(lesson);
    }

    public List<LessonResponse> searchLessons(String keyword){
        List<Lesson> lessons = lessonRepository.findByTitleContainingIgnoreCase(keyword);

        boolean isTeacher = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_TEACHER"));

        if (isTeacher) {
            lessons = lessons.stream()
                    .filter(lesson -> lesson.getStatus() != Status.REJECTED)
                    .toList();
        }

        return lessonMapper.toLessonResponseList(lessons);
    }

    public List<Lesson> findAllLesson(){
        return lessonRepository.findAll();
    }

    @Transactional
    public void deleteLesson(Integer id){
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khóa học không tồn tại hoặc đã bị xóa!"));

        lesson.setStatus(Status.PENDING);
        lessonRepository.save(lesson);
    }

    @Transactional
    public Lesson approveDeleteLesson(Integer id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học!"));

        // Kiểm tra xem khóa học có đang nằm trong danh sách chờ xóa không
        if (lesson.getStatus() != Status.PENDING) {
            throw new RuntimeException("Khóa học này không nằm trong danh sách yêu cầu xóa!");
        }

        // Admin đồng ý xóa -> Chuyển sang REJECTED (Xóa mềm / Disable)
        lesson.setStatus(Status.REJECTED);
        return lessonRepository.save(lesson);
    }

    public LessonResponse getLessonById(Integer id){
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khóa học không tồn tại hoặc đã bị xóa!"));

        boolean isTeacher = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_TEACHER"));

        if (isTeacher && lesson.getStatus() == Status.REJECTED) {
            throw new RuntimeException("Khóa học không tồn tại hoặc đã bị xóa!");
        }

        return lessonMapper.toLessonResponse(lesson);
    }

    @Transactional
    public Lesson approveLesson(Integer id){
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy khóa học để thực hiện duyệt!"));

        lesson.setStatus(Status.ACTIVE);
        return lessonRepository.save(lesson);
    }

    //pagination testing
    public Page<Lesson> getLessons(
            Status status,
            Integer teacherId,
            String keyword,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        return lessonRepository.searchLessons(status, teacherId, keyword, pageable);
    }
}
