package com.example.service;

import com.example.repository.CouponCountRepository;
import com.example.producer.CouponCreateProducer;
import org.springframework.stereotype.Service;

@Service
public class CouponServiceWithRedisAndKafka implements CouponCommand {
    private final CouponCountRepository couponCountRepository;

    private final CouponCreateProducer couponCreateProducer;

    public CouponServiceWithRedisAndKafka(
        CouponCountRepository couponCountRepository,
        CouponCreateProducer couponCreateProducer) {
        this.couponCountRepository = couponCountRepository;
        this.couponCreateProducer = couponCreateProducer;
    }

    @Override
    public void issueCoupon(Long userId) {
        long count = couponCountRepository.increment();

        if (count > MAX_COUPON_COUNT) {
            return;
        }

        couponCreateProducer.createCoupon(userId);
    }
}
