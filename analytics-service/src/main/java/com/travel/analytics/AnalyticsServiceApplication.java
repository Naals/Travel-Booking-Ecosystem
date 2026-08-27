package com.travel.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Analytics Service.
 * Bounded context: Analytics (intelligence)
 * Tier: 4
 *
 * Platform-wide daily metrics, not per-user — no
 * identity.user-registered consumer, unlike every other Tier 3/4
 * service with a per-account aggregate. Publishes nothing; the third
 * consumer-only service after search-service (Day 14) and
 * recommendation-service (Day 20). KafkaTopics.ANALYTICS_EVENT
 * (declared Day 3) is formally retired rather than implemented — see
 * ADR-014, which also documents the first real use of
 * DomainEvent.eventId (shared-kernel, Day 2) for consumer-side
 * deduplication.
 *
 * api-gateway's route table already includes /api/v1/analytics/**
 * (Day 5) — no gateway changes needed today.
 *
 * Boot order: discovery-server → config-server → analytics-service
 * Depends on: PostgreSQL (analytics_db), Kafka
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
public class AnalyticsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AnalyticsServiceApplication.class, args);
    }
}
