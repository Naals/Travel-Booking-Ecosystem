package com.travel.user.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "saved_locations", indexes = {
    @Index(name = "idx_saved_locations_user_id", columnList = "user_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SavedLocationJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserProfileJpaEntity userProfile;

    @Column(name = "label",   nullable = false, length = 50) private String label;
    @Column(name = "city",    nullable = false)               private String city;
    @Column(name = "country")                                 private String country;
    @Column(name = "latitude")                                private Double latitude;
    @Column(name = "longitude")                               private Double longitude;
}
