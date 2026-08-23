package com.travel.loyalty.domain;

import com.travel.loyalty.domain.service.PointsCalculationPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PointsCalculationPolicy")
class PointsCalculationPolicyTest {

    PointsCalculationPolicy policy = new PointsCalculationPolicy();

    @Test @DisplayName("calculates 10 points per whole dollar")
    void wholeDollar() {
        assertThat(policy.calculatePoints(new BigDecimal("100.00")).getValue()).isEqualTo(1000L);
    }

    @Test @DisplayName("rounds down on a fractional dollar amount")
    void roundsDown() {
        // 19.99 * 10 = 199.9 → floors to 199
        assertThat(policy.calculatePoints(new BigDecimal("19.99")).getValue()).isEqualTo(199L);
    }

    @Test @DisplayName("zero spend earns zero points")
    void zeroSpend() {
        assertThat(policy.calculatePoints(BigDecimal.ZERO).getValue()).isZero();
    }
}
