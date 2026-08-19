package com.travel.messaging;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Messaging Service.
 * Bounded context: Messaging (engagement)
 * Tier: 3 — second MongoDB-backed service (see ADR-008)
 *
 * Unlike every other Tier 3 service so far (user-service and
 * review-service both consume booking.booking-completed),
 * messaging-service has NO Kafka consumers of its own — only the
 * MessagingEventPublisher producer. Conversations are entirely
 * user-initiated rather than reactively created from an upstream
 * event: a guest chooses to message a host, with recipientId supplied
 * by the caller (typically sourced from property-service's hostId
 * field), and a bookingId attached to a BOOKING-context conversation
 * is accepted as caller-provided display metadata, never verified
 * against booking-service. This keeps the service a pure producer,
 * with no coupling to upstream event schemas to track.
 *
 * Boot order: discovery-server → config-server → messaging-service
 * Depends on: MongoDB (messaging_db — a second logical database on
 * the same shared Mongo server review_db uses, per ADR-008), Kafka
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
public class MessagingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MessagingServiceApplication.class, args);
    }
}
