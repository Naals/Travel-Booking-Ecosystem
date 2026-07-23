package com.travel.property.infrastructure.persistence.entity;

import com.travel.property.domain.valueobject.Amenity;
import com.travel.property.domain.valueobject.PropertyStatus;
import com.travel.property.domain.valueobject.PropertyType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

@Entity
@Table(name = "properties", indexes = {
    @Index(name = "idx_properties_host_id", columnList = "host_id"),
    @Index(name = "idx_properties_status",  columnList = "status"),
    @Index(name = "idx_properties_city",    columnList = "city")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PropertyJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "host_id",    nullable = false) private String hostId;
    @Column(name = "title",      nullable = false) private String title;
    @Column(name = "description",nullable = false, columnDefinition = "TEXT") private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false) private PropertyType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false) private PropertyStatus status;

    @Column(name = "street",      nullable = false) private String street;
    @Column(name = "city",        nullable = false) private String city;
    @Column(name = "state")                         private String state;
    @Column(name = "country",     nullable = false) private String country;
    @Column(name = "postal_code")                   private String postalCode;
    @Column(name = "latitude",    nullable = false) private double latitude;
    @Column(name = "longitude",   nullable = false) private double longitude;

    @Column(name = "nightly_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal nightlyRate;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "max_guests", nullable = false) private int maxGuests;
    @Column(name = "bedrooms",   nullable = false) private int bedrooms;
    @Column(name = "bathrooms",  nullable = false) private int bathrooms;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "property_amenities",
        joinColumns = @JoinColumn(name = "property_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "amenity")
    private Set<Amenity> amenities;

    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false)                    private Instant updatedAt;

    @PrePersist void prePersist() { createdAt = updatedAt = Instant.now(); }
    @PreUpdate  void preUpdate()  { updatedAt = Instant.now(); }
}
