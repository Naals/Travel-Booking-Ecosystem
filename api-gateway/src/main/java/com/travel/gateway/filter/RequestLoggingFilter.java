package com.travel.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Logs every inbound request and its response status + latency.
 * Runs before JWT filter (order = -200) so even rejected requests are logged.
 *
 * Log format:
 *   → GET /api/v1/bookings/abc [traceId=xyz]
 *   ← GET /api/v1/bookings/abc 200 43ms
 */
@Slf4j
@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        long startMs = Instant.now().toEpochMilli();
        String traceId = request.getHeaders().getFirst("X-B3-TraceId");

        log.info("→ {} {} [traceId={}]",
            request.getMethod(),
            request.getURI().getPath(),
            traceId);

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long duration = Instant.now().toEpochMilli() - startMs;
            log.info("← {} {} {} {}ms",
                request.getMethod(),
                request.getURI().getPath(),
                exchange.getResponse().getStatusCode(),
                duration);
        }));
    }

    @Override
    public int getOrder() {
        return -200;
    }
}
