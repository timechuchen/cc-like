package com.chuchen.cclike.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * redis 配置类
 */
@Configuration
public class RedisConfig {

    /**
     * 这里主要是为了将 redisTemplate 的序列化方式改为 JSON 方式，这样在数据库的管理工具中可读性更强，便于调试
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();  
        template.setConnectionFactory(connectionFactory);  
  
        // 使用 Jackson2JsonRedisSerializer 序列化值  
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
  
        // Key 使用 String 序列化  
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);  
        template.setHashKeySerializer(new StringRedisSerializer());  
        template.setHashValueSerializer(serializer);  
  
        template.afterPropertiesSet();  
        return template;  
    }  
  
    @Bean  
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        // 让 Spring Session 使用 JSON 方式存储  
        return new GenericJackson2JsonRedisSerializer();
    }
}
