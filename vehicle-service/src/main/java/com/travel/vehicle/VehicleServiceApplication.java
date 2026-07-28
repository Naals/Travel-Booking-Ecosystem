package com.travel.vehicle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Vehicle Service.
 * Bounded context: Vehicle
 * Tier: 2 (Inventory + Search)
 *
 * Boot order: discovery-server → config-server → vehicle-service
 * Depends on: PostgreSQL (vehicle_db), Kafka
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
public class VehicleServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(VehicleServiceApplication.class, args);
    }
}
