package com.example.repository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CouponAppliedUserRepository {
    private final RedisTemplate<String, String> redisTemplate;

    public CouponAppliedUserRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void add(Long userId) throws IllegalStateException {
        Long result = redisTemplate
            .opsForSet()
            .add("coupon-applied-user", userId.toString());

        if (result == null || result != 1) {
            throw new IllegalStateException("이미 쿠폰을 받은 사용자입니다.");
        }
    }
}
