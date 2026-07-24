package com.travel.hotel.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "room_reservations", indexes = {
    @Index(name = "idx_room_res_room_id",    columnList = "room_id"),
    @Index(name = "idx_room_res_booking_id", columnList = "booking_id"),
    @Index(name = "idx_room_res_dates",      columnList = "check_in_date, check_out_date")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RoomReservationJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private RoomJpaEntity room;

    @Column(name = "booking_id",     nullable = false) private String    bookingId;
    @Column(name = "user_id",        nullable = false) private String    userId;
    @Column(name = "check_in_date",  nullable = false) private LocalDate checkInDate;
    @Column(name = "check_out_date", nullable = false) private LocalDate checkOutDate;
    @Column(name = "confirmed",      nullable = false) private boolean   confirmed;
}
