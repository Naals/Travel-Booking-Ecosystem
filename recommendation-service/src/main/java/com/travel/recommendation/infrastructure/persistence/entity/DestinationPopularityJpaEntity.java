package com.travel.recommendation.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "destination_popularity", indexes = {
    @Index(name = "idx_destination_popularity_count", columnList = "completed_trip_count")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uq_destination", columnNames = {"city", "country"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DestinationPopularityJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "city",    nullable = false) private String city;
    @Column(name = "country", nullable = false) private String country;

    @Column(name = "completed_trip_count", nullable = false)
    private long completedTripCount;

    @Column(name = "last_updated_at", nullable = false)
    private Instant lastUpdatedAt;
}
