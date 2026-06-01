package com.example.backend.service;

import com.example.backend.Mapper.StudentMapper;
import com.example.backend.Mapper.TeacherMapper;
import com.example.backend.Mapper.UserMapper;
import com.example.backend.dto.student.request.CreateStudentRequest;
import com.example.backend.dto.student.request.EnrollmentRequest;
import com.example.backend.dto.student.request.UpdateStudentRequest;
import com.example.backend.dto.student.response.StudentResponseProfile;
import com.example.backend.dto.teacher.request.UpdateTeacherRequest;
import com.example.backend.dto.teacher.response.TeacherResponseProfile;
import com.example.backend.dto.user.request.CreateUserRequest;
import com.example.backend.dto.user.request.UpdateUserRequest;
import com.example.backend.dto.user.response.UserResponseProfile;
import com.example.backend.entity.*;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.respository.EnrollmentRepository;
import com.example.backend.respository.LessonRepository;
import com.example.backend.respository.StudentResponsitory;
import com.example.backend.respository.UserResponsitory;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@AllArgsConstructor
public class StudentService extends IUserService<CreateUserRequest, UpdateStudentRequest> {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserResponsitory userResponsitory;
    private final StudentResponsitory studentResponsitory;
    private final StudentMapper studentMapper;
    private final UserService userService;
    private final SupabaseStorageService supabaseStorageService;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;

    @Override
    @Transactional
    public void register(CreateUserRequest createUserRequest){
        User user = new User();
        if(userService.checkUserExist(createUserRequest)){
            user = userMapper.toCreate(createUserRequest);
            user.setPassword(passwordEncoder.encode(createUserRequest.getPassword()));
            userResponsitory.save(user);
            if(createUserRequest instanceof CreateStudentRequest createStudentRequest){
                Student student = studentMapper.toCreate(createStudentRequest);
                student.setUser(user);
                student.setStudent_code(generateStudentCode());
                studentResponsitory.save(student);
            }
        }

        MultipartFile avatar = createUserRequest.getAvatar();
        if(avatar != null && !avatar.isEmpty()){
            String avatarUrl = supabaseStorageService.uploadFile(
                    avatar,
                    "users/" + user.getId()
            );
            user.setAvatar_url(avatarUrl);
            userResponsitory.save(user);
        }
    }

    private String generateStudentCode() {
        int year = java.time.LocalDate.now().getYear();

        String prefix = "ST_" + year + "_";

        String latestCode = studentResponsitory.findLatestStudentCode(prefix);

        int nextNumber = 1;

        if (latestCode != null && !latestCode.isBlank()) {
            String numberPart = latestCode.substring(prefix.length());
            nextNumber = Integer.parseInt(numberPart) + 1;
        }

        return prefix + String.format("%03d", nextNumber);
    }

    @Override
    public void update(Integer userId, UpdateStudentRequest request){
            User user = userResponsitory.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            userMapper.updateEntityFromRequest(request, user);
                Student student = user.getStudent();
                studentMapper.updateEntityFromRequest(request, student);
                studentResponsitory.save(student);
            userResponsitory.save(user);
        }

    @Override
    public StudentResponseProfile getUserProfile(Integer userId){
        User user = userResponsitory.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.setId(userId);
        return studentMapper.toProfileResponse(user);
    }

    public List<StudentResponseProfile> getAllStudents() {
        return studentResponsitory.findAll()
                .stream()
                .map(student -> getUserProfile(student.getUser().getId()))
                .toList();
    }

    public void enrollLesson(EnrollmentRequest enrollmentRequest){
        Enrollment enrollment = new Enrollment();
        Integer lessonId = enrollmentRequest.getLessonId();
        Integer userId = enrollmentRequest.getUserId();
        User user = userResponsitory.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));
        enrollment.setId(new EnrollmentId(enrollmentRequest.getUserId(), enrollmentRequest.getLessonId()));
        enrollment.setLesson(lesson);
        enrollment.setUser(user);
        enrollmentRepository.save(enrollment);
    }
    public List<Integer> getStudentlessonByStudentId(Integer studentId) {
        return enrollmentRepository.findById_UserId(studentId)
                .stream()
                .map(enrollment -> enrollment.getLesson().getId())
                .toList();
    }

}
