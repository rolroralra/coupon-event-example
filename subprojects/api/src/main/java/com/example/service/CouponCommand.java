package com.example.service;

public interface CouponCommand {
    int MAX_COUPON_COUNT = 100;

    void issueCoupon(Long userId);

}
