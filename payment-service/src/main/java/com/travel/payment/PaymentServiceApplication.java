package com.travel.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Payment Service.
 * Bounded context: Payment
 * Tier: 1 (core saga)
 *
 * Boot order: discovery-server → config-server → payment-service
 * Depends on: PostgreSQL (payment_db), Kafka, Stripe API key
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
public class PaymentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
