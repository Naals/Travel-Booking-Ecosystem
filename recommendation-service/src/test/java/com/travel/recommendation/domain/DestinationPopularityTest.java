package com.travel.recommendation.domain;

import com.travel.recommendation.domain.model.DestinationPopularity;
import com.travel.recommendation.domain.valueobject.DestinationKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DestinationPopularity")
class DestinationPopularityTest {

    @Test @DisplayName("starts at zero completed trips")
    void startsAtZero() {
        var popularity = DestinationPopularity.initial(DestinationKey.of("Paris", "FR"));
        assertThat(popularity.getCompletedTripCount()).isZero();
    }

    @Test @DisplayName("increment increases the count by one")
    void incrementIncreases() {
        var popularity = DestinationPopularity.initial(DestinationKey.of("Paris", "FR"));
        popularity.increment();
        popularity.increment();
        assertThat(popularity.getCompletedTripCount()).isEqualTo(2L);
    }
}
