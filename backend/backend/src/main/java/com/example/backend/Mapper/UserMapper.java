package com.example.backend.Mapper;

import com.example.backend.dto.user.request.CreateUserRequest;
import com.example.backend.dto.user.request.UpdateUserRequest;
import com.example.backend.dto.user.response.UserResponseProfile;
import com.example.backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.mapstruct.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created_at", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "avatar_url", ignore = true)
    User toCreate(CreateUserRequest createUserRequest);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(UpdateUserRequest request, @MappingTarget User user);
    UserResponseProfile toUserResponseProfile(User user);
}
