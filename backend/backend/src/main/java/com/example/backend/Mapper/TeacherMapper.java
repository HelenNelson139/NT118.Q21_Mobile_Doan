package com.example.backend.Mapper;


import com.example.backend.dto.teacher.request.TeacherCreationRequest;
import com.example.backend.dto.teacher.request.UpdateTeacherRequest;
import com.example.backend.dto.teacher.response.TeacherResponseProfile;
import com.example.backend.entity.Teacher;
import com.example.backend.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface TeacherMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "teacher_code", ignore = true)
    Teacher toCreate(TeacherCreationRequest teacherCreationRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(UpdateTeacherRequest request, @MappingTarget Teacher teacher);

    @Mapping(target = "department", source = "teacher.department")
    @Mapping(target = "teacher_code", source = "teacher.teacher_code" )
    @Mapping(target = "id", source = "teacher.user.id")
    TeacherResponseProfile toProfileResponse(User user);
}
