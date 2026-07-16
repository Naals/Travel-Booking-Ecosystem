package com.travel.identity.domain.service;

import com.travel.identity.domain.model.Role;
import com.travel.identity.domain.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Domain service for JWT generation and validation.
 *
 * Access token lifetime:  1 hour  (configurable)
 * Refresh token lifetime: 30 days (configurable)
 *
 * Claims embedded in access token:
 *   sub   → userId (used by api-gateway as X-User-Id)
 *   email → user email
 *   roles → list of role names (e.g. ["TRAVELER", "HOST"])
 *
 * Refresh token carries only sub + tokenType — minimizes exposure
 * if a refresh token is intercepted.
 */
@Slf4j
@Service
public class JwtTokenService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiry-seconds:3600}")
    private long accessTokenExpiry;

    @Value("${jwt.refresh-token-expiry-seconds:2592000}")
    private long refreshTokenExpiry;

    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail().getValue());
        claims.put("firstName", user.getFullName().getFirstName());
        claims.put("lastName", user.getFullName().getLastName());
        claims.put("roles", user.getRoles().stream()
            .map(Role::name)
            .collect(Collectors.toList()));
        claims.put("tokenType", "ACCESS");
        return buildToken(claims, user.getId().getValue(), accessTokenExpiry);
    }

    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType", "REFRESH");
        return buildToken(claims, user.getId().getValue(), refreshTokenExpiry);
    }

    public Claims validateAndExtractClaims(String token) {
        return Jwts.parser()
            .verifyWith(signingKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public boolean isAccessToken(Claims claims) {
        return "ACCESS".equals(claims.get("tokenType", String.class));
    }

    public boolean isRefreshToken(Claims claims) {
        return "REFRESH".equals(claims.get("tokenType", String.class));
    }

    public long getAccessTokenExpiry() { return accessTokenExpiry; }

    private String buildToken(Map<String, Object> extraClaims,
                              String subject, long expirySeconds) {
        Instant now = Instant.now();
        return Jwts.builder()
            .claims(extraClaims)
            .subject(subject)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(expirySeconds)))
            .signWith(signingKey())
            .compact();
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}
