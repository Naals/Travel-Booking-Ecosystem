package com.travel.recommendation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Recommendation Service.
 * Bounded context: Recommendation (intelligence)
 * Tier: 4 — first Tier 4 service
 *
 * Consumer-only, like search-service (Day 14) — no domain events of
 * its own, no publisher. Heuristic scoring, no ML, no synchronous
 * calls to any other service; every signal is built from a locally
 * consumed event. See ADR-012 for the full design rationale,
 * including the deliberate FLIGHT-destination gap.
 *
 * api-gateway's route table already includes /api/v1/recommendations/**
 * (Day 5) — no gateway changes needed today.
 *
 * Boot order: discovery-server → config-server → recommendation-service
 * Depends on: PostgreSQL (recommendation_db), Kafka
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
public class RecommendationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RecommendationServiceApplication.class, args);
    }
}
