package com.example.backend.service;

import com.example.backend.Mapper.LessonMapper;
import com.example.backend.dto.lesson.request.LessonCreationRequest;
import com.example.backend.dto.lesson.request.LessonUpdateRequest;
import com.example.backend.dto.lesson.response.LessonResponse;
import com.example.backend.dto.lesson.response.LessonStudentCountResponse;
import com.example.backend.entity.Lesson;
import com.example.backend.entity.Teacher;
import com.example.backend.entity.User;
import com.example.backend.enums.Status;
import com.example.backend.respository.EnrollmentRepository;
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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class LessonService {
    LessonRepository lessonRepository;
    TeacherResponsitory teacherResponsitory;
    LessonMapper lessonMapper;
    SupabaseStorageService supabaseStorageService;
    ModuleService moduleService;
    EnrollmentRepository enrollmentRepository;

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
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isTeacher = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_TEACHER"));
        boolean isStudent = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_STUDENT"));
            if (isTeacher) {
                Jwt jwt = (Jwt) authentication.getPrincipal();
                String currentTeacherUsername = jwt.getSubject();
                lessons = lessons.stream()
                        .filter(lesson -> lesson.getTeacher() != null
                                && lesson.getTeacher().getUser().getUsername() != null
                                && lesson.getTeacher().getUser().getUsername().equals(currentTeacherUsername))
                        .filter(lesson -> lesson.getStatus() != Status.REJECTED && lesson.getStatus() != Status.DELETED)
                        .toList();
            } else if (isStudent) {
            lessons = lessons.stream()
                    .filter(lesson -> lesson.getStatus() != Status.PENDING && lesson.getStatus() != Status.REJECTED)
                    .toList();
        }
        return lessonMapper.toLessonResponseList(lessons);
    }

    public List<LessonResponse> findAllLesson(){
        List<Lesson> listLessonFound = lessonRepository.findAll();
        return lessonMapper.toLessonResponseList(listLessonFound);
    }

    public List<LessonResponse> findAllLessonActive(){
        List<Lesson> listLessonFound = lessonRepository.findAll();
        return listLessonFound.stream()
                .filter(lesson -> lesson.getStatus() == Status.ACTIVE )
                .map(lessonMapper::toLessonResponse)
                .toList();
    }

    public List<LessonResponse> findAllLessonPending(){
        List<Lesson> listLessonFound = lessonRepository.findAll();
        return listLessonFound.stream()
                .filter(lesson -> lesson.getStatus() == Status.PENDING )
                .map(lessonMapper::toLessonResponse)
                .toList();
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
        if (lesson.getStatus() == Status.REJECTED || lesson.getStatus() == Status.DELETED) {
            throw new RuntimeException("Khóa học không tồn tại hoặc đã bị xóa!");
        }
        return lessonMapper.toLessonResponse(lesson);
    }

    @Transactional
    public LessonResponse approveLesson(Integer id){
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy khóa học để thực hiện duyệt!"));
        if (lesson.getStatus() != Status.PENDING) {
            throw new RuntimeException("Khóa học này không nằm trong danh sách yêu cầu !");
        }
        moduleService.approvePendingModulesByLesson(id);
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

    public List<LessonResponse> getLessonsByTeacherId(Integer teacherId) {
        // Tìm danh sách thực thể Lesson theo trường teacherId
        List<com.example.backend.entity.Lesson> lessons = lessonRepository.findByTeacherId(teacherId);

        // Chuyển đổi List<Lesson> thành List<LessonResponse> thông qua maper của bạn
        return lessons.stream()
                .filter(lesson -> lesson.getStatus() == Status.ACTIVE)
                .map(lessonMapper::toLessonResponse)
                .collect(Collectors.toList());
    }

    public List<LessonResponse> getAllPendingOrHasPendingModules() {
        List<Lesson> lessons = lessonRepository
                .findAllPendingOrHasPendingModules(Status.PENDING);

        return lessons.stream()
                .map(lessonMapper::toLessonResponse)
                .toList();
    }


    @Transactional
    public LessonResponse UpdateLesson(Integer id, LessonUpdateRequest request) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khóa học không tồn tại hoặc đã bị xóa!"));

        if (request.getTitle() != null) {
            lesson.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            lesson.setDescription(request.getDescription());
        }

        if (request.getWhat_you_learn() != null) {
            lesson.setWhat_you_learn(request.getWhat_you_learn());
        }

        if (request.getSkill_learned() != null) {
            lesson.setSkill_learned(request.getSkill_learned());
        }


        Lesson updatedLesson = lessonRepository.save(lesson);
        return lessonMapper.toLessonResponse(updatedLesson);
    }

    public List<LessonStudentCountResponse> getStudentCountPerLesson() {
        return enrollmentRepository.countStudentsPerLesson();
    }

    public Integer getStudentCountByLessonId(Integer lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Khóa học không tồn tại hoặc đã bị xóa!"));
        return enrollmentRepository.countById_LessonId(lessonId).intValue();
    }
}
