package com.travel.recommendation.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "user_affinity", indexes = {
    @Index(name = "idx_user_affinity_user_id", columnList = "user_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uq_user_destination", columnNames = {"user_id", "city", "country"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserAffinityJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false) private String userId;
    @Column(name = "city",    nullable = false) private String city;
    @Column(name = "country", nullable = false) private String country;
    @Column(name = "score",   nullable = false) private long   score;

    @Column(name = "last_signal_at", nullable = false)
    private Instant lastSignalAt;
}
