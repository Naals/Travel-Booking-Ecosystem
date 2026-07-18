package com.travel.booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Booking Service.
 * Bounded context: Booking
 * Tier: 1 (core saga)
 *
 * Boot order: discovery-server → config-server → booking-service
 * Depends on: PostgreSQL (booking_db), Kafka
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
public class BookingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookingServiceApplication.class, args);
    }
}
