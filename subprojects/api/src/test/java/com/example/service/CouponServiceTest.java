package com.example.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ExecutorTest;
import com.example.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CouponServiceTest {
    @Qualifier("couponService")
    @Autowired
    private CouponCommand couponCommand;

    @Autowired
    private CouponRepository couponRepository;

    @BeforeEach
    void setUp() {
        couponRepository.deleteAll();
    }

    @Test
    void shouldIssueCoupon() {
        couponCommand.issueCoupon(1L);

        assertThat(couponRepository.count()).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(ints = {1000})
    void failToIssueManyCouponsInParallel(int threadCount) throws InterruptedException {

        ExecutorTest.testWithMultipleThreads(i -> couponCommand.issueCoupon((long) i), threadCount);

        assertThat(couponRepository.count()).isNotEqualTo(CouponCommand.MAX_COUPON_COUNT);
    }
}
