package com.travel.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Centralized Configuration Server.
 *
 * Serves configuration to all 21 microservices at startup.
 * In local dev, configuration is loaded from classpath:/config/.
 * In production, configuration is loaded from a private Git repository.
 *
 * Services call this server on bootstrap before their own context starts.
 * A /actuator/busrefresh call (Spring Cloud Bus) can push config changes
 * to all running services without restart.
 *
 * Registers with Eureka so services can discover it by name
 * rather than requiring a hardcoded host.
 */
@SpringBootApplication
@EnableConfigServer
@EnableDiscoveryClient
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
