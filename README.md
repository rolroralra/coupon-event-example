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
