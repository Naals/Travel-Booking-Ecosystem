package com.travel.wallet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Wallet Service.
 * Bounded context: Wallet (engagement)
 * Tier: 3 — back to PostgreSQL after two MongoDB services (Days 16-17)
 *
 * Owns per-user balance as an append-only, non-negative ledger (see
 * Wallet aggregate's Javadoc). Auto-provisioned reactively from
 * identity.user-registered, the same pattern user-service established
 * on Day 15 — this is the second service to do so.
 *
 * PaymentMethod.WALLET was declared in payment-service back on Day 8
 * and has never been implemented — ProcessPaymentUseCase only ever
 * charges via Stripe. That remains true after today. Wiring WALLET up
 * as an active choice in the booking saga would require either a
 * synchronous call from payment-service into this service (breaking
 * this platform's Kafka-only inter-service communication) or a new
 * two-step sub-saga (WalletDebitRequested → WalletDebited/Failed).
 * Both are real, valid future work — deliberately out of scope today.
 * See ADR-010 for the full reasoning; this Javadoc exists so the gap
 * is visible from the entry point, not just in a docs file.
 *
 * Boot order: discovery-server → config-server → wallet-service
 * Depends on: PostgreSQL (wallet_db), Kafka
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
public class WalletServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(WalletServiceApplication.class, args);
    }
}
