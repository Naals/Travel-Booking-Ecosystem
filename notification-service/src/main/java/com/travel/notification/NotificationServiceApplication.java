package com.travel.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Notification Service.
 * Bounded context: Notification
 * Tier: 1 (core saga)
 *
 * Pure consumer — no database, no saga state.
 * Boot order: discovery-server → config-server → notification-service
 * Depends on: Kafka, SMTP server (MailHog in local dev)
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
public class NotificationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
