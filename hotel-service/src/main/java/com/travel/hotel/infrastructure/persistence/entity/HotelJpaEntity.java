package com.travel.hotel.infrastructure.persistence.entity;

import com.travel.hotel.domain.valueobject.HotelStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hotels", indexes = {
    @Index(name = "idx_hotels_manager_id", columnList = "manager_id"),
    @Index(name = "idx_hotels_status",     columnList = "status"),
    @Index(name = "idx_hotels_city",       columnList = "city")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HotelJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "manager_id",  nullable = false) private String      managerId;
    @Column(name = "name",        nullable = false) private String      name;
    @Column(name = "description", nullable = false,
        columnDefinition = "TEXT")              private String      description;
    @Column(name = "street",      nullable = false) private String      street;
    @Column(name = "city",        nullable = false) private String      city;
    @Column(name = "country",     nullable = false) private String      country;
    @Column(name = "latitude",    nullable = false) private double      latitude;
    @Column(name = "longitude",   nullable = false) private double      longitude;
    @Column(name = "star_rating", nullable = false) private int         starRating;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private HotelStatus status;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL,
        orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<RoomJpaEntity> rooms = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false)                    private Instant updatedAt;

    @PrePersist void prePersist() { createdAt = updatedAt = Instant.now(); }
    @PreUpdate  void preUpdate()  { updatedAt = Instant.now(); }
}
