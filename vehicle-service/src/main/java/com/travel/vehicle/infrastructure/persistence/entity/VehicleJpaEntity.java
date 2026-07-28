package com.travel.vehicle.infrastructure.persistence.entity;

import com.travel.vehicle.domain.valueobject.*;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "vehicles", indexes = {
    @Index(name = "idx_vehicles_category",      columnList = "category"),
    @Index(name = "idx_vehicles_status",        columnList = "status"),
    @Index(name = "idx_vehicles_location_code", columnList = "current_location_code"),
    @Index(name = "idx_vehicles_category_loc",  columnList = "category, current_location_code"),
    @Index(name = "idx_vehicles_license_plate", columnList = "license_plate", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VehicleJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    // ── Spec ─────────────────────────────────────────────────────────────────
    @Column(name = "make",          nullable = false) private String           make;
    @Column(name = "model",         nullable = false) private String           model;
    @Column(name = "year",          nullable = false) private int              year;
    @Column(name = "license_plate", nullable = false, unique = true) private String licensePlate;
    @Column(name = "seats",         nullable = false) private int              seats;
    @Enumerated(EnumType.STRING)
    @Column(name = "transmission",  nullable = false) private TransmissionType transmission;
    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type",     nullable = false) private FuelType         fuelType;
    @Column(name = "air_conditioning", nullable = false) private boolean       airConditioning;

    // ── Category + status ─────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false) private VehicleCategory category;
    @Enumerated(EnumType.STRING)
    @Column(name = "status",   nullable = false) private VehicleStatus   status;

    // ── Home location ─────────────────────────────────────────────────────────
    @Column(name = "home_location_code",    nullable = false) private String homeLocationCode;
    @Column(name = "home_location_city",    nullable = false) private String homeLocationCity;
    @Column(name = "home_location_country", nullable = false) private String homeLocationCountry;
    @Column(name = "home_location_address")                   private String homeLocationAddress;

    // ── Current location ──────────────────────────────────────────────────────
    @Column(name = "current_location_code",    nullable = false) private String currentLocationCode;
    @Column(name = "current_location_city",    nullable = false) private String currentLocationCity;
    @Column(name = "current_location_country", nullable = false) private String currentLocationCountry;

    // ── Pricing ───────────────────────────────────────────────────────────────
    @Column(name = "daily_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyRate;
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    // ── Active rental (denormalised for performance) ──────────────────────────
    @Column(name = "rental_booking_id")            private String    rentalBookingId;
    @Column(name = "rental_user_id")               private String    rentalUserId;
    @Column(name = "rental_pickup_date")           private LocalDate rentalPickupDate;
    @Column(name = "rental_return_date")           private LocalDate rentalReturnDate;
    @Column(name = "rental_pickup_location_code")  private String    rentalPickupLocationCode;
    @Column(name = "rental_pickup_location_city")  private String    rentalPickupLocationCity;
    @Column(name = "rental_return_location_code")  private String    rentalReturnLocationCode;
    @Column(name = "rental_return_location_city")  private String    rentalReturnLocationCity;
    @Column(name = "rental_confirmed")             private Boolean   rentalConfirmed;

    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false)                    private Instant updatedAt;

    @PrePersist void prePersist() { createdAt = updatedAt = Instant.now(); }
    @PreUpdate  void preUpdate()  { updatedAt = Instant.now(); }
}
