package com.example.backend.Mapper;

import com.example.backend.dto.student.request.CreateStudentRequest;
import com.example.backend.dto.student.request.UpdateStudentRequest;
import com.example.backend.dto.student.response.StudentResponseProfile;
import com.example.backend.entity.Student;
import com.example.backend.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface StudentMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "student_code", ignore = true)
    Student toCreate(CreateStudentRequest createStudentRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(UpdateStudentRequest request, @MappingTarget Student student);

    @Mapping(target = "date_of_birth", source = "student.date_of_birth")
    StudentResponseProfile toProfileResponse(User user);
}
