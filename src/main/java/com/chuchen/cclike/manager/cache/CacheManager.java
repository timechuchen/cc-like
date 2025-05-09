package com.chuchen.cclike.manager.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

/**
 * @author chuchen
 * @date 2025/5/9 10:57
 * @description 为了易用性考虑，需要将 HeavyKeeper 和本地缓存工具封装为通用的一层
 */
@Component
@Slf4j
public class CacheManager {

    private TopK hotKeyDetector;
    private Cache<String, Object> localCache;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Bean
    public TopK getHotKeyDetector() {
        hotKeyDetector = new HeavyKeeper(
                100,
                100000,
                5,
                0.92,
                10
        );
        return hotKeyDetector;
    }

    @Bean
    public Cache<String, Object> localCache() {
        return localCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build();
    }

    // 构造复合 key
    private String buildCacheKey(String hashKey, String key) {
        return hashKey + ":" + key;
    }

    public Object get(String hashKey, String key) {
        // 构造唯一的 composite key
        String compositeKey = buildCacheKey(hashKey, key);

        // 先从本地缓存中获取
        Object value = localCache.getIfPresent(compositeKey);
        if(value != null) {
            log.info("本地缓存获取到的数据 {} = {}", compositeKey, value);
            // 记录访问次数（每次访问次数加一）
            hotKeyDetector.add(key, 1);
            return value;
        }

        // 本地缓存未命中，从 redis 中获取
        Object redisValue = redisTemplate.opsForHash().get(hashKey, key);
        if(redisValue == null) {
            return null;
        }

        // 记录访问次数（每次访问次数加一）
        AddResult addResult = hotKeyDetector.add(key, 1);

        if(addResult.isHotKey()) {
            localCache.put(compositeKey, redisValue);
        }

        return redisValue;
    }

    public void putIfPresent(String hashKey, String key, Object value) {
        String compositeKey = buildCacheKey(hashKey, key);
        Object object = localCache.getIfPresent(compositeKey);

        if (object == null) {
            return;
        }

        localCache.put(compositeKey, value);
    }

    // 定时清理过期的热 key 检测数据
    @Scheduled(fixedRate = 20, timeUnit = TimeUnit.SECONDS)
    public void cleanHotKeys() {
        hotKeyDetector.fading();
    }
}
