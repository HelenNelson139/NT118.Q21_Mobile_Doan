package com.example.backend.service;

import com.example.backend.Mapper.StudentMapper;
import com.example.backend.Mapper.TeacherMapper;
import com.example.backend.Mapper.UserMapper;
import com.example.backend.dto.student.request.CreateStudentRequest;
import com.example.backend.dto.student.request.UpdateStudentRequest;
import com.example.backend.dto.student.response.StudentResponseProfile;
import com.example.backend.dto.teacher.request.UpdateTeacherRequest;
import com.example.backend.dto.user.request.CreateUserRequest;
import com.example.backend.dto.user.request.UpdateUserRequest;
import com.example.backend.dto.user.response.UserResponseProfile;
import com.example.backend.entity.Student;
import com.example.backend.entity.Teacher;
import com.example.backend.entity.User;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.respository.StudentResponsitory;
import com.example.backend.respository.UserResponsitory;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class StudentService extends IUserService<CreateUserRequest, UpdateStudentRequest> {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserResponsitory userResponsitory;
    private final StudentResponsitory studentResponsitory;
    private final StudentMapper studentMapper;
    private final UserService userService;
    private final TeacherMapper teacherMapper;

    @Override
    @Transactional
    public void register(CreateUserRequest createUserRequest){
        if(userService.checkUserExist(createUserRequest)){
            User user = userMapper.toCreate(createUserRequest);
            user.setPassword(passwordEncoder.encode(createUserRequest.getPassword()));
            userResponsitory.save(user);
            if(createUserRequest instanceof CreateStudentRequest createStudentRequest){
                Student student = studentMapper.toCreate(createStudentRequest);
                student.setUser(user);
                studentResponsitory.save(student);
            }
        }

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
        return studentMapper.toProfileResponse(user);
    }



}
