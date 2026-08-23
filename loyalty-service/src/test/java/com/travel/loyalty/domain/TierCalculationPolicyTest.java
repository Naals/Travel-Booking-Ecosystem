package com.travel.loyalty.domain;

import com.travel.loyalty.domain.model.LoyaltyTier;
import com.travel.loyalty.domain.service.TierCalculationPolicy;
import com.travel.loyalty.domain.valueobject.Points;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TierCalculationPolicy")
class TierCalculationPolicyTest {

    @Test @DisplayName("zero lifetime points is BRONZE")
    void bronze() {
        assertThat(TierCalculationPolicy.calculateTier(Points.of(0))).isEqualTo(LoyaltyTier.BRONZE);
    }

    @Test @DisplayName("just below SILVER threshold is still BRONZE")
    void justBelowSilver() {
        assertThat(TierCalculationPolicy.calculateTier(Points.of(4_999))).isEqualTo(LoyaltyTier.BRONZE);
    }

    @Test @DisplayName("exactly at SILVER threshold is SILVER")
    void exactlySilver() {
        assertThat(TierCalculationPolicy.calculateTier(Points.of(5_000))).isEqualTo(LoyaltyTier.SILVER);
    }

    @Test @DisplayName("exactly at GOLD threshold is GOLD")
    void exactlyGold() {
        assertThat(TierCalculationPolicy.calculateTier(Points.of(20_000))).isEqualTo(LoyaltyTier.GOLD);
    }

    @Test @DisplayName("exactly at PLATINUM threshold is PLATINUM")
    void exactlyPlatinum() {
        assertThat(TierCalculationPolicy.calculateTier(Points.of(50_000))).isEqualTo(LoyaltyTier.PLATINUM);
    }

    @Test @DisplayName("well above PLATINUM threshold is still PLATINUM")
    void wayAbovePlatinum() {
        assertThat(TierCalculationPolicy.calculateTier(Points.of(1_000_000))).isEqualTo(LoyaltyTier.PLATINUM);
    }
}
