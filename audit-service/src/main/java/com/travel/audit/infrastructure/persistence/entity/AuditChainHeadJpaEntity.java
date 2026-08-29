package com.travel.audit.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * A single-row table — the chain has exactly one tail. The row is
 * seeded by Flyway's V1 migration (not lazily created by application
 * code) specifically to avoid a cold-start race: if two replicas both
 * found no head row and both tried to insert the singleton, one would
 * violate the PK constraint. Seeding via migration means the row
 * always exists before any application code runs, so lockById() can
 * assume it — see ADR-015.
 */
@Entity
@Table(name = "audit_chain_head")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditChainHeadJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "last_sequence_number", nullable = false)
    private long lastSequenceNumber;

    @Column(name = "last_hash", nullable = false, length = 64)
    private String lastHash;
}
