package com.example.consumer;

import com.example.consumer.domain.Coupon;
import com.example.consumer.domain.FailedEvent;
import com.example.consumer.repository.CouponRepository;
import com.example.consumer.repository.FailedEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CouponCreateConsumer {

    private static final String COUPON_CREATE_TOPIC = "coupon-create";

    private final CouponRepository couponRepository;

    private final FailedEventRepository failedEventRepository;

    public CouponCreateConsumer(CouponRepository couponRepository,
        FailedEventRepository failedEventRepository) {
        this.couponRepository = couponRepository;
        this.failedEventRepository = failedEventRepository;
    }

    @KafkaListener(topics = CouponCreateConsumer.COUPON_CREATE_TOPIC, groupId = "coupon-consumer-group-1")
    public void consume(Long userId) {
        try {
            log.info("Consumed message: {}", userId);
            couponRepository.save(new Coupon(userId));
        } catch (Exception e) {
            log.error("Failed to consume message: {}", userId);
            failedEventRepository.save(new FailedEvent(userId));
        }
    }
}
