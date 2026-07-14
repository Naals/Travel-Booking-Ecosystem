package com.travel.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * Gateway infrastructure beans.
 *
 * Rate limit key strategy: per authenticated user (X-User-Id header).
 * Falls back to client IP if the header is absent (unauthenticated requests).
 *
 * This means each user gets their own rate limit bucket in Redis,
 * not a shared bucket per IP — fairer for users behind NAT.
 */
@Configuration
public class GatewayConfig {

    /**
     * Resolves the rate limit key per request.
     * Authenticated requests are keyed by userId.
     * Unauthenticated requests are keyed by remote IP.
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest()
                .getHeaders()
                .getFirst("X-User-Id");

            if (userId != null && !userId.isBlank()) {
                return Mono.just(userId);
            }

            String ip = exchange.getRequest()
                .getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "anonymous";

            return Mono.just(ip);
        };
    }
}
