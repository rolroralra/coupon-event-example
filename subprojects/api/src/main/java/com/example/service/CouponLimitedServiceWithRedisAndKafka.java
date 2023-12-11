package com.example.service;

import com.example.producer.CouponCreateProducer;
import com.example.repository.CouponAppliedUserRepository;
import com.example.repository.CouponCountRepository;
import org.springframework.stereotype.Service;

@Service
public class CouponLimitedServiceWithRedisAndKafka implements CouponCommand {
    private final CouponCountRepository couponCountRepository;

    private final CouponCreateProducer couponCreateProducer;

    private final CouponAppliedUserRepository couponAppliedUserRepository;

    public CouponLimitedServiceWithRedisAndKafka(
        CouponCountRepository couponCountRepository,
        CouponCreateProducer couponCreateProducer,
        CouponAppliedUserRepository couponAppliedUserRepository) {
        this.couponCountRepository = couponCountRepository;
        this.couponCreateProducer = couponCreateProducer;
        this.couponAppliedUserRepository = couponAppliedUserRepository;
    }

    @Override
    public void issueCoupon(Long userId) {
        couponAppliedUserRepository.add(userId);

        long count = couponCountRepository.increment();

        if (count > MAX_COUPON_COUNT) {
            return;
        }

        couponCreateProducer.createCoupon(userId);
    }
}
