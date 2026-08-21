package com.travel.loyalty.domain.service;

import com.travel.loyalty.domain.valueobject.Points;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Converts money spent into points earned. A Spring bean (unlike
 * TierCalculationPolicy) purely so the earn rate could later become
 * configurable via application.yml without changing call sites — not
 * needed today, but a cheap consistency choice to leave open.
 * Rounds down: a $19.99 spend at 10 pts/$ earns 199 points, not 200.
 */
@Component
public class PointsCalculationPolicy {

    private static final int POINTS_PER_DOLLAR = 10;

    public Points calculatePoints(BigDecimal amountSpent) {
        long points = amountSpent
            .multiply(BigDecimal.valueOf(POINTS_PER_DOLLAR))
            .setScale(0, RoundingMode.DOWN)
            .longValueExact();
        return Points.of(points);
    }
}
