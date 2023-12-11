package com.example.container;

import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestComponent
public class TestContainer {

    private final static String MYSQL_TEST_CONTAINER_IMAGE_TAG = "mysql:8.0.26";
    private final static String KAFKA_TEST_CONTAINER_IMAGE_TAG = "confluentinc/cp-kafka:6.2.1";
    private final static String REDIS_TEST_CONTAINER_IMAGE_TAG = "redis:7.2.3-alpine";

    private final static int REDIS_PORT = 6379;

    static MySQLContainer<?> mysql = new MySQLContainer<>(MYSQL_TEST_CONTAINER_IMAGE_TAG);

    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse(KAFKA_TEST_CONTAINER_IMAGE_TAG));

    static GenericContainer<?> redis = new GenericContainer<>(REDIS_TEST_CONTAINER_IMAGE_TAG)
        .withExposedPorts(REDIS_PORT);


    @BeforeAll
    static void beforeAll() {
        mysql.start();
        redis.start();

        if (!kafka.isRunning()) {
            kafka.start();
        }
    }

    @AfterAll
    static void afterAll() {
        mysql.stop();
        redis.stop();
        kafka.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(REDIS_PORT));

        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.producer.key-serializer", StringSerializer.class::getName);
        registry.add("spring.kafka.producer.value-serializer", LongSerializer.class::getName);
    }
}
