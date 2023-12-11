package com.example.service;

import com.example.domain.Coupon;
import com.example.repository.CouponCountRepository;
import com.example.repository.CouponRepository;
import org.springframework.stereotype.Service;

@Service
public class CouponService implements CouponCommand {

    private final CouponRepository couponRepository;

    private final CouponCountRepository couponCountRepository;

    public CouponService(
        CouponRepository couponRepository,
        CouponCountRepository couponCountRepository
    ) {
        this.couponRepository = couponRepository;
        this.couponCountRepository = couponCountRepository;
    }

    @Override
    public void issueCoupon(Long userId) {
        long count = couponRepository.count();

        if (count > MAX_COUPON_COUNT) {
            return;
        }

        couponRepository.save(new Coupon(userId));
    }
}
