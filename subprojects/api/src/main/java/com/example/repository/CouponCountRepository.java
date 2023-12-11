package com.example.repository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CouponCountRepository {

    public static final String REDIS_COUPON_COUNT_KEY = "coupon_count";

    private final RedisTemplate<String, String> redisTemplate;

    public CouponCountRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Long increment() {
        return redisTemplate
            .opsForValue()
            .increment(REDIS_COUPON_COUNT_KEY);
    }
}
