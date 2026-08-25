package com.travel.recommendation.domain;

import com.travel.recommendation.domain.valueobject.DestinationKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DestinationKey value object")
class DestinationKeyTest {

    @Test @DisplayName("normalizes country to uppercase")
    void normalizesCountry() {
        var key = DestinationKey.of("Istanbul", "tr");
        assertThat(key.getCountry()).isEqualTo("TR");
    }

    @Test @DisplayName("does NOT case-fold city — known limitation, see ADR-012")
    void doesNotCaseFoldCity() {
        var a = DestinationKey.of("istanbul", "TR");
        var b = DestinationKey.of("Istanbul", "TR");
        assertThat(a).isNotEqualTo(b);
    }

    @Test @DisplayName("rejects blank city")
    void rejectsBlankCity() {
        assertThatThrownBy(() -> DestinationKey.of("  ", "TR"))
            .isInstanceOf(com.travel.common.exception.DomainException.class);
    }

    @Test @DisplayName("rejects blank country")
    void rejectsBlankCountry() {
        assertThatThrownBy(() -> DestinationKey.of("Istanbul", ""))
            .isInstanceOf(com.travel.common.exception.DomainException.class);
    }

    @Test @DisplayName("equal city and country produce equal keys")
    void equality() {
        assertThat(DestinationKey.of("Paris", "FR")).isEqualTo(DestinationKey.of("Paris", "fr"));
    }
}
