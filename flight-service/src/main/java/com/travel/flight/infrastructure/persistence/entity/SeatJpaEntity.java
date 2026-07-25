package com.travel.flight.infrastructure.persistence.entity;

import com.travel.flight.domain.valueobject.SeatClass;
import com.travel.flight.domain.valueobject.SeatStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "flight_seats", indexes = {
    @Index(name = "idx_seats_flight_id",    columnList = "flight_id"),
    @Index(name = "idx_seats_class_status", columnList = "seat_class, status")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SeatJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id", nullable = false)
    private FlightJpaEntity flight;

    @Column(name = "seat_number", nullable = false) private String     seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_class",  nullable = false) private SeatClass  seatClass;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",      nullable = false) private SeatStatus status;

    @Column(name = "price",       nullable = false,
        precision = 10, scale = 2)              private BigDecimal price;

    @Column(name = "currency",    nullable = false,
        length = 3)                              private String     currency;

    @Column(name = "booking_id")                    private String     bookingId;
    @Column(name = "user_id")                       private String     userId;
    @Column(name = "reservation_confirmed")         private Boolean    reservationConfirmed;
}
