package com.travel.property.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "property_reservations", indexes = {
    @Index(name = "idx_reservations_property_id", columnList = "property_id"),
    @Index(name = "idx_reservations_booking_id",  columnList = "booking_id"),
    @Index(name = "idx_reservations_dates",       columnList = "check_in_date, check_out_date")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReservationJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "property_id",   nullable = false) private String    propertyId;
    @Column(name = "booking_id",    nullable = false) private String    bookingId;
    @Column(name = "user_id",       nullable = false) private String    userId;
    @Column(name = "check_in_date", nullable = false) private LocalDate checkInDate;
    @Column(name = "check_out_date",nullable = false) private LocalDate checkOutDate;
    @Column(name = "confirmed",     nullable = false) private boolean   confirmed;
}
