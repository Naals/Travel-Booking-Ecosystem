package com.travel.audit.domain.service;

import com.travel.audit.domain.model.AuditCategory;
import com.travel.audit.domain.valueobject.ChainHash;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

/**
 * Pure, static hash computation — no Spring dependency, no state, the
 * same shape as TierCalculationPolicy (loyalty-service, Day 19) and
 * RecommendationEngine (recommendation-service, Day 20).
 *
 * Every field that identifies or describes an entry feeds the hash
 * (sequence number, category, source event type and id, subject,
 * summary, occurredAt) alongside the previous entry's own hash —
 * changing any single one of them, on any entry in the chain, changes
 * that entry's hash and, by extension, every hash after it. This is
 * the entire tamper-evidence mechanism: ChainIntegrityVerifier simply
 * recomputes and compares.
 */
public final class HashChainService {

    private HashChainService() {}

    public static ChainHash computeHash(long sequenceNumber, AuditCategory category,
                                        String sourceEventType, String sourceEventId,
                                        String subjectId, String summary,
                                        Instant occurredAt, ChainHash previousHash) {
        String canonical = String.join("|",
            String.valueOf(sequenceNumber),
            category.name(),
            sourceEventType,
            sourceEventId,
            subjectId,
            summary,
            occurredAt.toString(),
            previousHash.getValue());
        return ChainHash.of(sha256Hex(canonical));
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is mandated by the Java platform spec on every
            // JVM — this branch is unreachable in practice.
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
