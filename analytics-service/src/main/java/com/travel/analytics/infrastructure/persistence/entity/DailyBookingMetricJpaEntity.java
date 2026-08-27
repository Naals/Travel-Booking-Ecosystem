package com.travel.analytics.infrastructure.persistence.entity;

import com.travel.analytics.domain.model.BookingType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "daily_booking_metrics",
    uniqueConstraints = @UniqueConstraint(name = "uq_date_booking_type", columnNames = {"metric_date", "booking_type"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DailyBookingMetricJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "metric_date", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_type", nullable = false)
    private BookingType bookingType;

    @Column(name = "created_count",   nullable = false) private long createdCount;
    @Column(name = "confirmed_count", nullable = false) private long confirmedCount;
    @Column(name = "completed_count", nullable = false) private long completedCount;
    @Column(name = "cancelled_count", nullable = false) private long cancelledCount;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
