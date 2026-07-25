package com.travel.flight.infrastructure.persistence.entity;

import com.travel.flight.domain.valueobject.FlightStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "flights", indexes = {
    @Index(name = "idx_flights_route",       columnList = "origin_code, destination_code"),
    @Index(name = "idx_flights_departure",   columnList = "departure_time"),
    @Index(name = "idx_flights_status",      columnList = "status"),
    @Index(name = "idx_flights_flight_number",columnList = "flight_number")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FlightJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "airline_code",    nullable = false) private String       airlineCode;
    @Column(name = "flight_number",   nullable = false) private String       flightNumber;
    @Column(name = "origin_code",     nullable = false) private String       originCode;
    @Column(name = "destination_code",nullable = false) private String       destinationCode;
    @Column(name = "origin_city",     nullable = false) private String       originCity;
    @Column(name = "destination_city",nullable = false) private String       destinationCity;
    @Column(name = "departure_time",  nullable = false) private ZonedDateTime departureTime;
    @Column(name = "arrival_time",    nullable = false) private ZonedDateTime arrivalTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",          nullable = false) private FlightStatus  status;

    @Column(name = "delay_reason")                      private String        delayReason;

    @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL,
        orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<SeatJpaEntity> seats = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false)                    private Instant updatedAt;

    @PrePersist void prePersist() { createdAt = updatedAt = Instant.now(); }
    @PreUpdate  void preUpdate()  { updatedAt = Instant.now(); }
}
