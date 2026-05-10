package com.example.backend.service;

import com.example.backend.entity.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class CacheService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // Lưu Student vào cache
    public void saveStudent(String id, Student student){
        redisTemplate.opsForValue().set(id, student);
    }

    //Lấy Student từ cache
    public Object get(String id){
        return redisTemplate.opsForValue().get("student" + id);
    }

    // Xong service implement vào Controller nữa thôi
}
