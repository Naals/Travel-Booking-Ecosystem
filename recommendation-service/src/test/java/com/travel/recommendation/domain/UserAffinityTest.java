package com.travel.recommendation.domain;

import com.travel.recommendation.domain.model.AffinitySignalType;
import com.travel.recommendation.domain.model.UserAffinity;
import com.travel.recommendation.domain.valueobject.DestinationKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserAffinity")
class UserAffinityTest {

    static final DestinationKey ISTANBUL = DestinationKey.of("Istanbul", "TR");

    @Test @DisplayName("starts at zero score")
    void startsAtZero() {
        var affinity = UserAffinity.initial("user-1", ISTANBUL);
        assertThat(affinity.getScore()).isZero();
    }

    @Test @DisplayName("a saved-location signal adds its weight")
    void savedLocationSignal() {
        var affinity = UserAffinity.initial("user-1", ISTANBUL);
        affinity.recordSignal(AffinitySignalType.SAVED_LOCATION);
        assertThat(affinity.getScore()).isEqualTo(3L);
    }

    @Test @DisplayName("a completed-trip signal outweighs a saved-location signal")
    void completedTripOutweighsSaved() {
        var withSave = UserAffinity.initial("user-1", ISTANBUL);
        withSave.recordSignal(AffinitySignalType.SAVED_LOCATION);

        var withTrip = UserAffinity.initial("user-2", ISTANBUL);
        withTrip.recordSignal(AffinitySignalType.COMPLETED_TRIP);

        assertThat(withTrip.getScore()).isGreaterThan(withSave.getScore());
    }

    @Test @DisplayName("multiple signals accumulate")
    void accumulates() {
        var affinity = UserAffinity.initial("user-1", ISTANBUL);
        affinity.recordSignal(AffinitySignalType.SAVED_LOCATION);
        affinity.recordSignal(AffinitySignalType.COMPLETED_TRIP);
        assertThat(affinity.getScore()).isEqualTo(13L); // 3 + 10
    }
}
