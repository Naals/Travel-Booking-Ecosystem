package com.travel.identity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Identity Service.
 * Bounded context: Identity
 * Tier: 1 (core saga)
 *
 * Boot order: discovery-server → config-server → identity-service
 * Flyway runs schema migrations automatically on startup.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
public class IdentityServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(IdentityServiceApplication.class, args);
    }
}
