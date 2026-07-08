package com.travel.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka Service Discovery Server.
 *
 * Every microservice in the travel platform registers itself here on
 * startup. The API Gateway queries Eureka to resolve the current live
 * instance addresses for booking-service, identity-service, etc.,
 * enabling client-side load balancing and zero-downtime restarts.
 *
 * Does NOT register itself as a Eureka client (standalone mode).
 * Secured with HTTP Basic — credentials injected via environment variables.
 */
@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServerApplication.class, args);
    }
}
