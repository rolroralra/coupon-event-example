# Environment
- Java 17
- Gradle 8.2
- Spring Boot 3.2.0
- MySQL 8.0.27
- Docker 
- Kafka

# Setting
```bash
cd docker

docker-compose -f docker-compose-mysql.yaml up -d
docker-compose -f docker-compose-kafka.yaml up -d

cd ..

./gradlew :consumer:bootRun
```

# Kafka
## How to create a topic
```bash
docker exec -it kafka kafka-topics.sh --create --topic coupon-create --bootstrap-server localhost:9092
```

## How to produce a message
```bash
docker exec -it kafka kafka-console-producer.sh --topic coupon-create --bootstrap-server localhost:9092
```

## How to consume a message
```bash
docker exec -it kafka kafka-console-consumer.sh --topic coupon-create --from-beginning --bootstrap-server localhost:9092 --key-deserializer org.apache.kafka.common.serialization.StringDeserializer --value-deserializer org.apache.kafka.common.serialization.LongDeserializer
```

# 선착순 쿠폰 발급 이벤트
## 1. 오직 DB만 사용하여 구현
[구현 코드](./subprojects/api/src/main/java/com/example/service/CouponService.java)

<details>
  <summary>코드 예시</summary>
  <p>

```java
@Service
public class CouponService implements CouponCommand {

    private final CouponRepository couponRepository;

    public CouponService(
        CouponRepository couponRepository
    ) {
        this.couponRepository = couponRepository;
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
```

  </p>
</details>

## 2. Redis를 사용하여 구현
[구현 코드](./subprojects/api/src/main/java/com/example/service/CouponServiceWithRedis.java)

<details>
  <summary>코드 예시</summary>
  <p>

```java
@Service
public class CouponServiceWithRedis implements CouponCommand {
    private final CouponRepository couponRepository;

    private final CouponCountRepository couponCountRepository;

    public CouponServiceWithRedis(
        CouponRepository couponRepository,
        CouponCountRepository couponCountRepository
    ) {
        this.couponRepository = couponRepository;
        this.couponCountRepository = couponCountRepository;
    }

    @Override
    public void issueCoupon(Long userId) {
        long count = couponCountRepository.increment();

        if (count > MAX_COUPON_COUNT) {
            return;
        }

        couponRepository.save(new Coupon(userId));
    }
}
```

  </p>
</details>

## 3. Redis + Kafka를 사용하여 구현
[구현 코드](./subprojects/api/src/main/java/com/example/service/CouponServiceWithRedisAndKafka.java)

<details>
  <summary>코드 예시</summary>
  <p>

```java
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
```
### Producer
```java
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

```

### Consumer 
```java
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
```

  </p>
</details>

## 4. 추가적으로 1인당 1개의 쿠폰만 발급되도록 구현
[구현 코드](./subprojects/api/src/main/java/com/example/service/CouponLimitedServiceWithRedisAndKafka.java)

<details>
  <summary>코드 예시</summary>
  <p>

```java
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
```

### Redis에서 제공하는 타입 Set을 사용하여 구현
```java
@Repository
public class CouponAppliedUserRepository {
    private final RedisTemplate<String, String> redisTemplate;

    public CouponAppliedUserRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void add(Long userId) throws IllegalStateException {
        Long result = redisTemplate
            .opsForSet()
            .add("coupon-applied-user", userId.toString());

        if (result == null || result != 1) {
            throw new IllegalStateException("이미 쿠폰을 받은 사용자입니다.");
        }
    }
}
```

  </p>
</details>
