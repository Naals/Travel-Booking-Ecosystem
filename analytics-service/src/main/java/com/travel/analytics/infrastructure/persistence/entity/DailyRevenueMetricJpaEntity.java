package com.travel.analytics.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "daily_revenue_metrics",
    uniqueConstraints = @UniqueConstraint(name = "uq_date_currency", columnNames = {"metric_date", "currency"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DailyRevenueMetricJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "metric_date", nullable = false)
    private LocalDate date;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "gross_revenue", nullable = false, precision = 14, scale = 2)
    private BigDecimal grossRevenue;

    @Column(name = "refunded_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal refundedAmount;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
