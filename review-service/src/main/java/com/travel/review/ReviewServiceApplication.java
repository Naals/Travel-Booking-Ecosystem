package com.travel.review;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Review Service.
 * Bounded context: Review (engagement)
 * Tier: 3 — first MongoDB-backed service (see ADR-008)
 *
 * No Flyway, no relational schema. Collection indexes are declared
 * via @Indexed/@CompoundIndex on the document classes and created
 * automatically on startup (spring.data.mongodb.auto-index-creation).
 *
 * Boot order: discovery-server → config-server → review-service
 * Depends on: MongoDB, Kafka
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
public class ReviewServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReviewServiceApplication.class, args);
    }
}
