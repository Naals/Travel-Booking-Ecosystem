package com.travel.user.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "travel_history", indexes = {
    @Index(name = "idx_travel_history_user_id", columnList = "user_id"),
    @Index(name = "idx_travel_history_completed_at", columnList = "completed_at")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TravelHistoryEntryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id",       nullable = false) private String  userId;
    @Column(name = "booking_id",    nullable = false) private String  bookingId;
    @Column(name = "resource_type", nullable = false) private String  resourceType;
    @Column(name = "resource_name", nullable = false) private String  resourceName;
    @Column(name = "completed_at",  nullable = false) private Instant completedAt;
}
