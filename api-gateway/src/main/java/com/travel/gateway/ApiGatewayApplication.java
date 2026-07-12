package com.travel.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API Gateway — single entry point for all client traffic.
 *
 * Built on Spring Cloud Gateway (WebFlux / reactive).
 * All filters run on the Netty event loop — keep them non-blocking.
 *
 * Startup checklist (IntelliJ run configuration):
 *   1. discovery-server must be running first (port 8761)
 *   2. config-server must be running (port 8888)
 *   3. Redis must be available (rate limiting)
 *   4. Set env var: JWT_SECRET=<same value as identity-service>
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
