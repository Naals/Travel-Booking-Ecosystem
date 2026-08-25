package com.travel.recommendation.domain;

import com.travel.recommendation.domain.model.AffinitySignalType;
import com.travel.recommendation.domain.model.DestinationPopularity;
import com.travel.recommendation.domain.model.DestinationScore;
import com.travel.recommendation.domain.model.UserAffinity;
import com.travel.recommendation.domain.service.RecommendationEngine;
import com.travel.recommendation.domain.valueobject.DestinationKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RecommendationEngine")
class RecommendationEngineTest {

    static final DestinationKey ISTANBUL = DestinationKey.of("Istanbul", "TR");
    static final DestinationKey PARIS    = DestinationKey.of("Paris", "FR");
    static final DestinationKey TOKYO    = DestinationKey.of("Tokyo", "JP");

    @Nested
    @DisplayName("New user (no affinity signals)")
    class NewUser {

        @Test @DisplayName("falls back to pure popularity ranking")
        void fallsBackToPopularity() {
            var paris = DestinationPopularity.initial(PARIS);
            paris.increment(); paris.increment(); paris.increment(); // 3

            var tokyo = DestinationPopularity.initial(TOKYO);
            tokyo.increment(); // 1

            List<DestinationScore> ranked = RecommendationEngine.rank(
                List.of(), List.of(paris, tokyo), 10);

            assertThat(ranked).hasSize(2);
            assertThat(ranked.get(0).destination()).isEqualTo(PARIS);
            assertThat(ranked.get(1).destination()).isEqualTo(TOKYO);
        }
    }

    @Nested
    @DisplayName("Returning user with affinity")
    class ReturningUser {

        @Test @DisplayName("personal affinity can outrank a more globally popular destination")
        void affinityCanOutrankPopularity() {
            // Tokyo is more globally popular...
            var tokyo = DestinationPopularity.initial(TOKYO);
            for (int i = 0; i < 5; i++) tokyo.increment(); // 5

            // ...but the user has personally completed a trip to Istanbul
            var istanbulAffinity = UserAffinity.initial("user-1", ISTANBUL);
            istanbulAffinity.recordSignal(AffinitySignalType.COMPLETED_TRIP); // 10 * weight(2) = 20

            List<DestinationScore> ranked = RecommendationEngine.rank(
                List.of(istanbulAffinity), List.of(tokyo), 10);

            assertThat(ranked.get(0).destination()).isEqualTo(ISTANBUL);
        }

        @Test @DisplayName("respects the limit parameter")
        void respectsLimit() {
            var paris = DestinationPopularity.initial(PARIS); paris.increment();
            var tokyo = DestinationPopularity.initial(TOKYO); tokyo.increment();

            List<DestinationScore> ranked = RecommendationEngine.rank(
                List.of(), List.of(paris, tokyo), 1);

            assertThat(ranked).hasSize(1);
        }

        @Test @DisplayName("affinity and popularity for the same destination combine")
        void combinesAffinityAndPopularity() {
            var parisPopularity = DestinationPopularity.initial(PARIS);
            parisPopularity.increment(); parisPopularity.increment(); // 2

            var parisAffinity = UserAffinity.initial("user-1", PARIS);
            parisAffinity.recordSignal(AffinitySignalType.SAVED_LOCATION); // 3 * weight(2) = 6

            List<DestinationScore> ranked = RecommendationEngine.rank(
                List.of(parisAffinity), List.of(parisPopularity), 10);

            assertThat(ranked).hasSize(1);
            assertThat(ranked.get(0).score()).isEqualTo(8L); // 2 + 6
        }
    }
}
