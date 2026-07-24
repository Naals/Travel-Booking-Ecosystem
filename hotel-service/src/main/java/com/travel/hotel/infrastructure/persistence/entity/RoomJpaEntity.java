package com.travel.hotel.infrastructure.persistence.entity;

import com.travel.hotel.domain.valueobject.RoomStatus;
import com.travel.hotel.domain.valueobject.RoomType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hotel_rooms", indexes = {
    @Index(name = "idx_rooms_hotel_id",    columnList = "hotel_id"),
    @Index(name = "idx_rooms_type_status", columnList = "room_type, status")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RoomJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private HotelJpaEntity hotel;

    @Column(name = "room_number",    nullable = false) private String     roomNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type",      nullable = false) private RoomType   roomType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",         nullable = false) private RoomStatus status;

    @Column(name = "rate_per_night", nullable = false,
        precision = 10, scale = 2)                 private BigDecimal ratePerNight;

    @Column(name = "currency",       nullable = false,
        length = 3)                                 private String     currency;

    @Column(name = "max_occupancy",  nullable = false) private int        maxOccupancy;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL,
        orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<RoomReservationJpaEntity> reservations = new ArrayList<>();
}
