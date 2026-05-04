package com.example.backend.service;


import com.example.backend.dto.user.request.CreateUserRequest;
import com.example.backend.dto.user.request.UpdateUserRequest;
import com.example.backend.dto.user.response.UserResponseProfile;


public abstract class IUserService<T, U> {
    public abstract void register(T request);
    public abstract void update(Integer userId, U request);
    public abstract UserResponseProfile getUserProfile(Integer userId);
}
