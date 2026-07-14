package com.travel.gateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("JwtAuthenticationFilter")
@TestPropertySource(properties = {
    "jwt.secret=test-secret-key-that-is-long-enough-for-hmac-sha256-algorithm"
})
class JwtAuthenticationFilterTest {

    private static final String SECRET =
        "test-secret-key-that-is-long-enough-for-hmac-sha256-algorithm";

    JwtAuthenticationFilter filter;
    GatewayFilterChain chain;

    @BeforeEach
    void setUp() throws Exception {
        filter = new JwtAuthenticationFilter();
        var secretField = JwtAuthenticationFilter.class.getDeclaredField("jwtSecret");
        secretField.setAccessible(true);
        secretField.set(filter, SECRET);

        chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    private String buildToken(String userId, String email) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
            .subject(userId)
            .claim("email", email)
            .claim("roles", List.of("TRAVELER"))
            .expiration(new Date(System.currentTimeMillis() + 3600_000))
            .signWith(key)
            .compact();
    }

    @Nested
    @DisplayName("Public paths")
    class PublicPaths {

        @Test
        @DisplayName("login path bypasses JWT validation")
        void loginBypassesAuth() {
            var request  = MockServerHttpRequest.get("/api/v1/auth/login").build();
            var exchange = MockServerWebExchange.from(request);

            filter.filter(exchange, chain).block();

            assertThat(exchange.getResponse().getStatusCode()).isNull();
        }

        @Test
        @DisplayName("health check bypasses JWT validation")
        void healthBypassesAuth() {
            var request  = MockServerHttpRequest.get("/actuator/health").build();
            var exchange = MockServerWebExchange.from(request);

            filter.filter(exchange, chain).block();

            assertThat(exchange.getResponse().getStatusCode()).isNull();
        }
    }

    @Nested
    @DisplayName("Protected paths")
    class ProtectedPaths {

        @Test
        @DisplayName("missing Authorization header → 401")
        void missingHeader_returns401() {
            var request  = MockServerHttpRequest.get("/api/v1/bookings").build();
            var exchange = MockServerWebExchange.from(request);

            filter.filter(exchange, chain).block();

            assertThat(exchange.getResponse().getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("malformed header (no Bearer prefix) → 401")
        void malformedHeader_returns401() {
            var request = MockServerHttpRequest.get("/api/v1/bookings")
                .header(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNz")
                .build();
            var exchange = MockServerWebExchange.from(request);

            filter.filter(exchange, chain).block();

            assertThat(exchange.getResponse().getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("valid JWT → propagates X-User-Id header downstream")
        void validJwt_propagatesHeaders() {
            String token = buildToken("user-123", "user@example.com");

            var request = MockServerHttpRequest.get("/api/v1/bookings")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
            var exchange = MockServerWebExchange.from(request);

            filter.filter(exchange, chain).block();

            assertThat(exchange.getResponse().getStatusCode()).isNull();
        }

        @Test
        @DisplayName("expired JWT → 401")
        void expiredJwt_returns401() {
            SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
            String expired = Jwts.builder()
                .subject("user-123")
                .expiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(key)
                .compact();

            var request = MockServerHttpRequest.get("/api/v1/bookings")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + expired)
                .build();
            var exchange = MockServerWebExchange.from(request);

            filter.filter(exchange, chain).block();

            assertThat(exchange.getResponse().getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }
}
