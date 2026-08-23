package com.travel.loyalty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Loyalty Service.
 * Bounded context: Loyalty (engagement)
 * Tier: 3 — final Tier 3 service
 *
 * Auto-provisioned from identity.user-registered, like user-service
 * (Day 15) and wallet-service (Day 18). Points are earned at booking
 * completion, not confirmation, via a local SpendRecord bridging model
 * that survives an in-between cancellation — see that class's Javadoc.
 * Tier is computed from lifetime points earned, never current balance
 * (ADR-011): redeeming points can never demote a member.
 *
 * api-gateway's route table already includes /api/v1/loyalty/** with a
 * circuit breaker (Day 5) — no gateway changes needed today.
 *
 * Boot order: discovery-server → config-server → loyalty-service
 * Depends on: PostgreSQL (loyalty_db), Kafka
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
public class LoyaltyServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(LoyaltyServiceApplication.class, args);
    }
}
