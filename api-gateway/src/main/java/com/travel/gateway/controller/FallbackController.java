package com.travel.gateway.controller;

import com.travel.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Circuit breaker fallback endpoints.
 *
 * When Resilience4j opens a circuit for a downstream service,
 * the gateway routes to these endpoints instead of propagating
 * the failure to the client.
 *
 * Returns a structured ApiResponse so clients always receive
 * the same response envelope regardless of whether the request
 * succeeded or hit a fallback.
 */
@Slf4j
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/identity")
    public Mono<ResponseEntity<ApiResponse<Void>>> identityFallback() {
        return fallback("identity-service");
    }

    @GetMapping("/booking")
    public Mono<ResponseEntity<ApiResponse<Void>>> bookingFallback() {
        return fallback("booking-service");
    }

    @GetMapping("/payment")
    public Mono<ResponseEntity<ApiResponse<Void>>> paymentFallback() {
        return fallback("payment-service");
    }

    @GetMapping("/notification")
    public Mono<ResponseEntity<ApiResponse<Void>>> notificationFallback() {
        return fallback("notification-service");
    }

    @GetMapping("/property")
    public Mono<ResponseEntity<ApiResponse<Void>>> propertyFallback() {
        return fallback("property-service");
    }

    @GetMapping("/search")
    public Mono<ResponseEntity<ApiResponse<Void>>> searchFallback() {
        return fallback("search-service");
    }

    @GetMapping("/user")
    public Mono<ResponseEntity<ApiResponse<Void>>> userFallback() {
        return fallback("user-service");
    }

    private Mono<ResponseEntity<ApiResponse<Void>>> fallback(String service) {
        log.warn("Circuit breaker fallback triggered for: {}", service);
        return Mono.just(ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(ApiResponse.error(
                service + " is currently unavailable. Please try again shortly.",
                "SERVICE_UNAVAILABLE")));
    }
}
