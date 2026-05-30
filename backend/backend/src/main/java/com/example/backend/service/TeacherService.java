package com.example.backend.service;

import com.example.backend.Mapper.TeacherMapper;
import com.example.backend.Mapper.UserMapper;
import com.example.backend.dto.student.request.UpdateStudentRequest;
import com.example.backend.dto.student.response.StudentResponseProfile;
import com.example.backend.dto.teacher.request.UpdateTeacherRequest;
import com.example.backend.dto.teacher.response.TeacherResponseProfile;
import com.example.backend.dto.user.request.CreateUserRequest;
import com.example.backend.dto.teacher.request.TeacherCreationRequest;
import com.example.backend.dto.user.request.UpdateUserRequest;
import com.example.backend.entity.Student;
import com.example.backend.entity.Teacher;
import com.example.backend.entity.User;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.respository.TeacherResponsitory;
import com.example.backend.respository.UserResponsitory;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
public class TeacherService extends IUserService<CreateUserRequest, UpdateTeacherRequest>{
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserResponsitory userResponsitory;
    private final TeacherResponsitory teacherResponsitory;
    private final TeacherMapper teacherMapper;
    private final UserService userService;
    private final SupabaseStorageService supabaseStorageService;


    @Override
    @Transactional
    public void register(CreateUserRequest createUserRequest){
        User user = new User();
        if(userService.checkUserExist(createUserRequest)){
            user = userMapper.toCreate(createUserRequest);
            user.setPassword(passwordEncoder.encode(createUserRequest.getPassword()));
            userResponsitory.save(user);
            if(createUserRequest instanceof TeacherCreationRequest teacherCreationRequest){
                Teacher teacher = teacherMapper.toCreate(teacherCreationRequest);
                teacher.setUser(user);
                teacherResponsitory.save(teacher);
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

    @Override
    @Transactional
    public void update(Integer userId, UpdateTeacherRequest request){
        User user = userResponsitory.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        userMapper.updateEntityFromRequest(request, user);
            Teacher teacher = user.getTeacher();
            teacherMapper.updateEntityFromRequest(request, teacher);
            teacherResponsitory.save(teacher);
        userResponsitory.save(user);
    }
    @Override
    public TeacherResponseProfile getUserProfile(Integer userId){
        User user = userResponsitory.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        String password = user.getPassword();
        return teacherMapper.toProfileResponse(user);
    }
}
