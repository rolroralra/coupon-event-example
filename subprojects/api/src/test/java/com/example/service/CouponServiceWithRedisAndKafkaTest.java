package com.example.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.example.ExecutorTest;
import com.example.container.RedisTest;
import com.example.repository.CouponRepository;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CouponServiceWithRedisAndKafkaTest extends RedisTest {
    @Qualifier("couponServiceWithRedisAndKafka")
    @Autowired
    private CouponCommand couponCommand;

    @Autowired
    private CouponRepository couponRepository;

    @ParameterizedTest
    @ValueSource(ints = {1000})
    void shouldIssueManyCouponsInParallel(int threadCount) throws InterruptedException {
        ExecutorTest.testWithMultipleThreads(i -> couponCommand.issueCoupon((long) i), threadCount);

        Thread.sleep(10000);

        assertThat(couponRepository.count()).isEqualTo(CouponCommand.MAX_COUPON_COUNT);
    }
}
