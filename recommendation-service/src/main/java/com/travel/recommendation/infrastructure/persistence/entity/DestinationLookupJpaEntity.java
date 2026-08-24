package com.travel.recommendation.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "destination_lookup")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DestinationLookupJpaEntity {

    @Id
    @Column(name = "resource_key", nullable = false, updatable = false)
    private String resourceKey;

    @Column(name = "city",    nullable = false) private String city;
    @Column(name = "country", nullable = false) private String country;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
