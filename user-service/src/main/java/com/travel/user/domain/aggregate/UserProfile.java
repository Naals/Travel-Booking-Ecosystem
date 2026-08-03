package com.travel.user.domain.aggregate;

import com.travel.user.domain.event.SavedLocationAddedEvent;
import com.travel.user.domain.event.UserProfileCreatedEvent;
import com.travel.user.domain.model.*;
import com.travel.shared.domain.AggregateRoot;
import com.travel.common.exception.BusinessRuleViolationException;

import java.time.Instant;
import java.util.*;

/**
 * UserProfile Aggregate Root — engagement bounded context.
 *
 * Deliberately separate from identity-service's User aggregate:
 * identity-service owns authentication (password, MFA, roles, account
 * status); this aggregate owns everything about how a person presents
 * and configures themselves for travel. The two share the same id
 * value (see UserId's Javadoc) but are otherwise fully independent —
 * a password reset never touches this aggregate, and a display name
 * change never touches identity-service.
 *
 * Created reactively: see CreateUserProfileUseCase, triggered by
 * identity.user-registered rather than by a direct API call.
 */
public class UserProfile extends AggregateRoot<UserId> {

    private static final int MAX_SAVED_LOCATIONS = 20;

    private DisplayName               displayName;
    private Bio                       bio;
    private String                    avatarUrl;
    private TravelPreferences         travelPreferences;
    private final List<SavedLocation> savedLocations;
    private final Instant             createdAt;
    private Instant                   updatedAt;

    private UserProfile(UserId id, DisplayName displayName) {
        super(id);
        this.displayName       = displayName;
        this.bio               = Bio.empty();
        this.avatarUrl         = null;
        this.travelPreferences = TravelPreferences.defaults();
        this.savedLocations    = new ArrayList<>();
        this.createdAt         = Instant.now();
        this.updatedAt         = Instant.now();
    }

    // ── Factory methods ───────────────────────────────────────────────────────

    /**
     * Creates a profile in response to identity-service's UserRegisteredEvent.
     * userId is supplied externally (see UserId Javadoc) — never generated here.
     */
    public static UserProfile create(UserId userId, DisplayName initialDisplayName) {
        UserProfile profile = new UserProfile(userId, initialDisplayName);
        profile.registerEvent(new UserProfileCreatedEvent(
            userId.getValue(), initialDisplayName.getValue()));
        return profile;
    }

    public static UserProfile reconstitute(UserId id, DisplayName displayName, Bio bio,
                                           String avatarUrl, TravelPreferences preferences,
                                           List<SavedLocation> savedLocations,
                                           Instant createdAt, Instant updatedAt) {
        UserProfile profile = new UserProfile(id, displayName);
        profile.bio               = bio;
        profile.avatarUrl         = avatarUrl;
        profile.travelPreferences = preferences;
        profile.savedLocations.addAll(
            savedLocations != null ? savedLocations : Collections.emptyList());
        return profile;
    }

    // ── Profile updates ───────────────────────────────────────────────────────

    /**
     * Full replace of the basic profile fields. bio/avatarUrl may be
     * blank/null to clear them — this is a full-record PATCH, not a
     * partial merge, keeping "clear this field" unambiguous for callers.
     */
    public void updateProfile(DisplayName newDisplayName, Bio newBio, String newAvatarUrl) {
        this.displayName = newDisplayName;
        this.bio         = newBio;
        this.avatarUrl   = (newAvatarUrl != null && !newAvatarUrl.isBlank()) ? newAvatarUrl : null;
        this.updatedAt   = Instant.now();
    }

    public void updateTravelPreferences(TravelPreferences newPreferences) {
        this.travelPreferences = newPreferences;
        this.updatedAt         = Instant.now();
    }

    // ── Saved locations ───────────────────────────────────────────────────────

    /**
     * Adds a saved location. Raises SavedLocationAddedEvent — a signal
     * useful to a future recommendation-service (Tier 4).
     */
    public SavedLocation addSavedLocation(String label, String city, String country,
                                          Double latitude, Double longitude) {
        if (savedLocations.size() >= MAX_SAVED_LOCATIONS)
            throw new BusinessRuleViolationException(
                "Cannot save more than " + MAX_SAVED_LOCATIONS + " locations",
                "SAVED_LOCATION_LIMIT_REACHED");

        boolean duplicateLabel = savedLocations.stream()
            .anyMatch(l -> l.getLabel().equalsIgnoreCase(label));
        if (duplicateLabel)
            throw new BusinessRuleViolationException(
                "A saved location with label '" + label + "' already exists",
                "DUPLICATE_SAVED_LOCATION_LABEL");

        SavedLocation location = new SavedLocation(
            SavedLocationId.generate(), label, city, country, latitude, longitude);
        savedLocations.add(location);
        this.updatedAt = Instant.now();

        registerEvent(new SavedLocationAddedEvent(
            getId().getValue(), location.getId().getValue(),
            location.getLabel(), location.getCity(), location.getCountry()));

        return location;
    }

    public void removeSavedLocation(SavedLocationId savedLocationId) {
        boolean removed = savedLocations.removeIf(l -> l.getId().equals(savedLocationId));
        if (!removed)
            throw new BusinessRuleViolationException(
                "No saved location found with id: " + savedLocationId.getValue(),
                "SAVED_LOCATION_NOT_FOUND");
        this.updatedAt = Instant.now();
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public DisplayName         getDisplayName()       { return displayName; }
    public Bio                 getBio()               { return bio; }
    public String              getAvatarUrl()         { return avatarUrl; }
    public TravelPreferences   getTravelPreferences() { return travelPreferences; }
    public List<SavedLocation> getSavedLocations()    { return Collections.unmodifiableList(savedLocations); }
    public Instant             getCreatedAt()         { return createdAt; }
    public Instant             getUpdatedAt()         { return updatedAt; }
}
