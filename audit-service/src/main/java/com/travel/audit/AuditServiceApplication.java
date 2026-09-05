package com.travel.audit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Audit Service.
 * Bounded context: Audit (intelligence)
 * Tier: 4 — the 21st and final service in this platform.
 *
 * Owns a hash-chained, append-only compliance trail across identity,
 * booking, payment, and fraud events. Every append is serialized
 * through a single locked "chain head" row (AuditChainRepository),
 * a deliberate correctness-over-throughput tradeoff appropriate to a
 * compliance log. VerifyChainIntegrityUseCase walks the full chain on
 * demand and detects tampering by recomputing every hash. See ADR-015
 * for the complete design rationale, including why this is the first
 * service to use shared-kernel's Entity without AggregateRoot, and
 * how it closes out KafkaTopics.AUDIT_LOG_CREATED — the last topic
 * declared on Day 3, finally implemented on the last day of this build.
 *
 * api-gateway's route table already includes /api/v1/audit/** (Day 5)
 * — no gateway changes needed today.
 *
 * Boot order: discovery-server → config-server → audit-service
 * Depends on: PostgreSQL (audit_db), Kafka
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
public class AuditServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuditServiceApplication.class, args);
    }
}
