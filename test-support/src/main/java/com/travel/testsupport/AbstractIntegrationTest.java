package com.travel.testsupport;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Base class for every service's Kafka-driven integration tests.
 *
 * Containers are static, started once per JVM and shared across every
 * test class that extends this — the standard Testcontainers
 * "singleton container" pattern. A fresh Postgres + Kafka pair per
 * test class would make a saga suite (several classes, each needing
 * both) unworkably slow; the tradeoff is that tests must not assume
 * an empty database — each test generates its own unique aggregate
 * IDs rather than truncating shared tables between runs.
 *
 * Image versions match docker-compose.yml (Day 1) deliberately, so
 * integration tests exercise the same Postgres/Kafka versions the
 * platform actually runs in.
 */
@Testcontainers
@ExtendWith(SpringExtension.class)
public abstract class AbstractIntegrationTest {

    protected static final PostgreSQLContainer<?> POSTGRES =
        new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));

    protected static final KafkaContainer KAFKA =
        new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    static {
        POSTGRES.start();
        KAFKA.start();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
        registry.add("eureka.client.enabled", () -> "false");
        registry.add("spring.cloud.discovery.enabled", () -> "false");
        registry.add("spring.cloud.config.enabled", () -> "false");
    }
}
