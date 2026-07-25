package com.travel.hotel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Hotel Service.
 * Bounded context: Hotel
 * Tier: 2 (Inventory + Search)
 *
 * Boot order: discovery-server → config-server → hotel-service
 * Depends on: PostgreSQL (hotel_db), Kafka
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
public class HotelServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(HotelServiceApplication.class, args);
    }
}
