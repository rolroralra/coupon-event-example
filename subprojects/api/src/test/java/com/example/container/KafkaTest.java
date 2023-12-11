package com.example.container;

import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class KafkaTest {
    private final static String TEST_CONTAINER_IMAGE_TAG = "confluentinc/cp-kafka:6.2.1";

    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse(TEST_CONTAINER_IMAGE_TAG))
        .withExposedPorts(9092);

    @BeforeAll
    static void beforeAll() {
        if (kafka.isRunning()) {
            return;
        }

        kafka.start();
    }

    @AfterAll
    static void afterAll() {
        kafka.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.producer.key-serializer", StringSerializer.class::getName);
        registry.add("spring.kafka.producer.value-serializer", LongSerializer.class::getName);
    }
}

