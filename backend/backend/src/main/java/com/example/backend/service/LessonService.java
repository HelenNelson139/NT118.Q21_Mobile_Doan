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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class LessonService {
    LessonRepository lessonRepository;
    TeacherResponsitory teacherResponsitory;
    LessonMapper lessonMapper;
    SupabaseStorageService supabaseStorageService;

    public LessonResponse createLesson(LessonCreationRequest request){
        Teacher teacher = teacherResponsitory.findById(request.getTeacherId())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        Lesson lesson = lessonMapper.toLesson(request);
        lesson.setTeacher(teacher);
        lesson.setStatus(Status.PENDING);

        MultipartFile thumbnail = request.getThumbnail();
        if(thumbnail != null && !thumbnail.isEmpty()){
            String thumbnail_url = supabaseStorageService.uploadFile(
                    thumbnail,
                    "lessons/" + lesson.getId()
            );
            lesson.setThumbnail_url(thumbnail_url);
        }
        Lesson savedLesson = lessonRepository.save(lesson);
        return lessonMapper.toLessonResponse(savedLesson);
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

    public List<LessonResponse> findAllLesson(){
        List<Lesson> listLessonFound = lessonRepository.findAll();
        return lessonMapper.toLessonResponseList(listLessonFound);
    }

    @Transactional
    public void deleteLesson(Integer id){
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khóa học không tồn tại hoặc đã bị xóa!"));
        lesson.setStatus(Status.PENDING);
        lessonRepository.save(lesson);
    }

    @Transactional
    public LessonResponse approveDeleteLesson(Integer id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học!"));
        if (lesson.getStatus() != Status.PENDING) {
            throw new RuntimeException("Khóa học này không nằm trong danh sách yêu cầu xóa!");
        }
        lesson.setStatus(Status.REJECTED);
        Lesson savedLesson = lessonRepository.save(lesson);
        return lessonMapper.toLessonResponse(savedLesson);
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
    public LessonResponse approveLesson(Integer id){
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy khóa học để thực hiện duyệt!"));
        lesson.setStatus(Status.ACTIVE);
        Lesson savedLesson = lessonRepository.save(lesson);
        return lessonMapper.toLessonResponse(savedLesson);
    }
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
