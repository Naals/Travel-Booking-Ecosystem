package com.travel.flight;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Flight Service.
 * Bounded context: Flight
 * Tier: 2 (Inventory + Search)
 *
 * Boot order: discovery-server → config-server → flight-service
 * Depends on: PostgreSQL (flight_db), Kafka
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
public class FlightServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FlightServiceApplication.class, args);
    }
}
