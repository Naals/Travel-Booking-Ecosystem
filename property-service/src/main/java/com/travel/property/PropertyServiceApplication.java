package com.travel.property;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Property Service.
 * Bounded context: Property
 * Tier: 2 (Inventory + Search)
 *
 * Boot order: discovery-server → config-server → property-service
 * Depends on: PostgreSQL (property_db), Kafka
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
public class PropertyServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PropertyServiceApplication.class, args);
    }
}
