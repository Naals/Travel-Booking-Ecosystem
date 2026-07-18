package com.travel.booking.infrastructure.persistence.entity;

import com.travel.booking.domain.valueobject.BookingStatus;
import com.travel.booking.domain.valueobject.BookingType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "bookings", indexes = {
    @Index(name = "idx_bookings_user_id",    columnList = "user_id"),
    @Index(name = "idx_bookings_status",     columnList = "status"),
    @Index(name = "idx_bookings_resource_id",columnList = "resource_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BookingJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "user_id",       nullable = false) private String userId;
    @Enumerated(EnumType.STRING)
    @Column(name = "booking_type",  nullable = false) private BookingType  bookingType;
    @Column(name = "resource_id",   nullable = false) private String resourceId;
    @Column(name = "resource_name", nullable = false) private String resourceName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookingStatus status;

    @Column(name = "check_in_date",  nullable = false) private LocalDate checkInDate;
    @Column(name = "check_out_date", nullable = false) private LocalDate checkOutDate;
    @Column(name = "guest_count",    nullable = false) private int       guestCount;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "payment_id")          private String paymentId;
    @Column(name = "cancellation_reason") private String cancellationReason;

    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false)                    private Instant updatedAt;

    @PrePersist void prePersist() { createdAt = updatedAt = Instant.now(); }
    @PreUpdate  void preUpdate()  { updatedAt = Instant.now(); }
}
