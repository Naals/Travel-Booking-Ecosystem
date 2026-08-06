package com.travel.user.domain;

import com.travel.user.domain.aggregate.UserProfile;
import com.travel.user.domain.event.SavedLocationAddedEvent;
import com.travel.user.domain.event.UserProfileCreatedEvent;
import com.travel.user.domain.model.*;
import com.travel.common.exception.BusinessRuleViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("UserProfile aggregate")
class UserProfileTest {

    static final UserId      USER_ID = UserId.of("user-123");
    static final com.travel.user.domain.model.DisplayName NAME    = com.travel.user.domain.model.DisplayName.of("Ada Lovelace");

    UserProfile profile;

    @BeforeEach
    void setUp() {
        profile = UserProfile.create(USER_ID, NAME);
        profile.clearDomainEvents();
    }

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test @DisplayName("raises UserProfileCreatedEvent")
        void raisesEvent() {
            UserProfile p = UserProfile.create(USER_ID, NAME);
            assertThat(p.getDomainEvents()).hasSize(1);
            assertThat(p.getDomainEvents().get(0)).isInstanceOf(UserProfileCreatedEvent.class);
        }

        @Test @DisplayName("starts with default travel preferences")
        void defaultsPreferences() {
            assertThat(profile.getTravelPreferences().getPreferredCurrency()).isEqualTo("USD");
            assertThat(profile.getTravelPreferences().getPreferredLanguage()).isEqualTo("en");
            assertThat(profile.getTravelPreferences().getSeatPreference())
                .isEqualTo(SeatPreference.NO_PREFERENCE);
        }

        @Test @DisplayName("starts with empty bio and no saved locations")
        void emptyDefaults() {
            assertThat(profile.getBio().isEmpty()).isTrue();
            assertThat(profile.getSavedLocations()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Profile updates")
    class ProfileUpdates {

        @Test @DisplayName("updateProfile changes name, bio, and avatar")
        void updatesFields() {
            profile.updateProfile(
                com.travel.user.domain.model.DisplayName.of("Grace Hopper"), Bio.of("Compiler pioneer"),
                "https://example.com/avatar.png");

            assertThat(profile.getDisplayName().getValue()).isEqualTo("Grace Hopper");
            assertThat(profile.getBio().getValue()).isEqualTo("Compiler pioneer");
            assertThat(profile.getAvatarUrl()).isEqualTo("https://example.com/avatar.png");
        }

        @Test @DisplayName("blank avatarUrl clears it to null")
        void blankAvatarClears() {
            profile.updateProfile(NAME, Bio.empty(), "  ");
            assertThat(profile.getAvatarUrl()).isNull();
        }
    }

    @Nested
    @DisplayName("Travel preferences")
    class TravelPreferencesTests {

        @Test @DisplayName("updateTravelPreferences replaces the value object")
        void updatesPreferences() {
            TravelPreferences newPrefs = TravelPreferences.of(
                "EUR", "de", SeatPreference.WINDOW, Set.of(DietaryRestriction.VEGAN));

            profile.updateTravelPreferences(newPrefs);

            assertThat(profile.getTravelPreferences().getPreferredCurrency()).isEqualTo("EUR");
            assertThat(profile.getTravelPreferences().getSeatPreference()).isEqualTo(SeatPreference.WINDOW);
            assertThat(profile.getTravelPreferences().getDietaryRestrictions())
                .containsExactly(DietaryRestriction.VEGAN);
        }

        @Test @DisplayName("rejects invalid currency code")
        void invalidCurrency() {
            assertThatThrownBy(() -> TravelPreferences.of(
                "usd", "en", SeatPreference.AISLE, Set.of()))
                .isInstanceOf(com.travel.common.exception.DomainException.class);
        }

        @Test @DisplayName("rejects invalid language code")
        void invalidLanguage() {
            assertThatThrownBy(() -> TravelPreferences.of(
                "USD", "ENG", SeatPreference.AISLE, Set.of()))
                .isInstanceOf(com.travel.common.exception.DomainException.class);
        }
    }

    @Nested
    @DisplayName("Saved locations")
    class SavedLocations {

        @Test @DisplayName("addSavedLocation raises SavedLocationAddedEvent")
        void addRaisesEvent() {
            profile.addSavedLocation("Home", "Istanbul", "TR", 41.0082, 28.9784);

            assertThat(profile.getSavedLocations()).hasSize(1);
            assertThat(profile.getDomainEvents().get(0)).isInstanceOf(SavedLocationAddedEvent.class);
        }

        @Test @DisplayName("rejects duplicate label")
        void duplicateLabel() {
            profile.addSavedLocation("Home", "Istanbul", "TR", null, null);
            assertThatThrownBy(() ->
                profile.addSavedLocation("home", "Ankara", "TR", null, null))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("already exists");
        }

        @Test @DisplayName("enforces max saved locations limit")
        void enforcesLimit() {
            for (int i = 0; i < 20; i++) {
                profile.addSavedLocation("Location " + i, "City", "US", null, null);
            }
            assertThatThrownBy(() ->
                profile.addSavedLocation("One too many", "City", "US", null, null))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("SAVED_LOCATION_LIMIT_REACHED");
        }

        @Test @DisplayName("removeSavedLocation removes by id")
        void remove() {
            var location = profile.addSavedLocation("Home", "Istanbul", "TR", null, null);
            profile.removeSavedLocation(location.getId());
            assertThat(profile.getSavedLocations()).isEmpty();
        }

        @Test @DisplayName("removing non-existent location throws")
        void removeNonExistent() {
            assertThatThrownBy(() ->
                profile.removeSavedLocation(SavedLocationId.generate()))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("SAVED_LOCATION_NOT_FOUND");
        }

        @Test @DisplayName("rejects invalid latitude")
        void invalidLatitude() {
            assertThatThrownBy(() ->
                profile.addSavedLocation("Bad", "City", "US", 200.0, 0.0))
                .isInstanceOf(com.travel.common.exception.DomainException.class);
        }
    }

    @Nested
    @DisplayName("Value objects")
    class ValueObjects {

        @Test @DisplayName("DisplayName rejects blank")
        void blankDisplayName() {
            assertThatThrownBy(() -> com.travel.user.domain.model.DisplayName.of(""))
                .isInstanceOf(com.travel.common.exception.DomainException.class);
        }

        @Test @DisplayName("DisplayName rejects single character")
        void tooShortDisplayName() {
            assertThatThrownBy(() -> com.travel.user.domain.model.DisplayName.of("A"))
                .isInstanceOf(com.travel.common.exception.DomainException.class);
        }

        @Test @DisplayName("Bio allows empty and null")
        void emptyBioAllowed() {
            assertThatCode(() -> Bio.of(null)).doesNotThrowAnyException();
            assertThatCode(() -> Bio.of("")).doesNotThrowAnyException();
        }

        @Test @DisplayName("Bio rejects text over 500 characters")
        void bioTooLong() {
            String tooLong = "x".repeat(501);
            assertThatThrownBy(() -> Bio.of(tooLong))
                .isInstanceOf(com.travel.common.exception.DomainException.class);
        }
    }
}
