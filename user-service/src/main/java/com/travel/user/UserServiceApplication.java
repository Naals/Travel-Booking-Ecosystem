package com.travel.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * User Service.
 * Bounded context: User Profile (engagement)
 * Tier: 3 — first Tier 3 service
 *
 * Owns display name, bio, avatar, travel preferences, saved locations,
 * and a travel-history projection. Does NOT own authentication,
 * password, MFA, or account status — see identity-service for that.
 *
 * Boot order: discovery-server → config-server → user-service
 * Depends on: PostgreSQL (user_db), Kafka
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
