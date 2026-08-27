package com.travel.analytics.infrastructure.persistence.entity;

import com.travel.analytics.domain.model.BookingType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "booking_type_lookup")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BookingTypeLookupJpaEntity {

    @Id
    @Column(name = "booking_id", nullable = false, updatable = false)
    private String bookingId;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_type", nullable = false)
    private BookingType bookingType;
}
