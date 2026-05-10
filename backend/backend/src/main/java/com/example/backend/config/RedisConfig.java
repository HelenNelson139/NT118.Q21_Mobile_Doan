package com.example.backend.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    // khởi tạo template để lưu dữ liệu trong redis
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        //1. Sử dụng StringRedisSerializer cho key
        //Mục đích: Biến các từ khóa (Key) từ kiểu Java String sang byte để lưu vào Redis.
        //StringRedisSerializer để khi kiểm tra keys thì nó sẽ hiện dưới dạng là một key đúng nghĩa
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setValueSerializer(stringRedisSerializer);

        // 2. Sử dụng GenericJackson2JsonRedisSerializer cho Value
        // Giúp lưu Object dưới dạng JSON trong Redis (dễ đọc hơn byte thô)
        // Giống như trên thì biến đổi Object thành dạng Json cho dễ đọc
        RedisSerializer<Object> jsonSerializer = RedisSerializer.json();
        redisTemplate.setValueSerializer(jsonSerializer);
        redisTemplate.setHashValueSerializer(jsonSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
