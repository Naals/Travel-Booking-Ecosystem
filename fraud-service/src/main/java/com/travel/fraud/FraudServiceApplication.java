package com.travel.fraud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Fraud Service.
 * Bounded context: Fraud (intelligence)
 * Tier: 4
 *
 * Auto-provisioned from identity.user-registered, the fourth service
 * to do so (user Day 15, wallet Day 18, loyalty Day 19). Evaluates a
 * small rule engine on every booking-created and payment-failed
 * event; a triggered rule publishes fraud.alert-raised, which
 * wallet-service (this day's cross-service commit) now consumes to
 * auto-freeze the account. FRAUD_CHECK_REQUESTED (declared since Day
 * 3) remains unused after today — no service currently requests an
 * on-demand check; every evaluation here is event-driven. See ADR-013.
 *
 * api-gateway's route table already includes /api/v1/fraud/** with a
 * circuit breaker (Day 5) — no gateway changes needed today.
 *
 * Boot order: discovery-server → config-server → fraud-service
 * Depends on: PostgreSQL (fraud_db), Kafka
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
public class FraudServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FraudServiceApplication.class, args);
    }
}
