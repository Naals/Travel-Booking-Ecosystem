package com.travel.user.infrastructure.persistence.entity;

import com.travel.user.domain.model.DietaryRestriction;
import com.travel.user.domain.model.SeatPreference;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "user_profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserProfileJpaEntity {

    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    private String userId;

    @Column(name = "display_name", nullable = false, length = 50)
    private String displayName;

    @Column(name = "bio", length = 500)
    private String bio;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "preferred_currency", nullable = false, length = 3)
    private String preferredCurrency;

    @Column(name = "preferred_language", nullable = false, length = 2)
    private String preferredLanguage;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_preference", nullable = false)
    private SeatPreference seatPreference;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_dietary_restrictions",
        joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "restriction")
    @Builder.Default
    private Set<DietaryRestriction> dietaryRestrictions = Set.of();

    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.ALL,
        orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<SavedLocationJpaEntity> savedLocations = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist void prePersist() { createdAt = updatedAt = Instant.now(); }
    @PreUpdate  void preUpdate()  { updatedAt = Instant.now(); }
}
