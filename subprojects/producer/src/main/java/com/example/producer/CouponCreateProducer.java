package com.example.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class CouponCreateProducer {

    public static final String COUPON_CREATE_TOPIC = "coupon-create";

    private final KafkaTemplate<String, Long> kafkaTemplate;

    public CouponCreateProducer(KafkaTemplate<String, Long> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void createCoupon(Long userId) {
        kafkaTemplate.send(COUPON_CREATE_TOPIC, userId);
    }
}
