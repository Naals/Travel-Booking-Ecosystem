package com.travel.fraud.domain.service;

import com.travel.fraud.domain.model.RiskSnapshot;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Illustrative threshold, not tuned against real data — see ADR-013. */
@Component
public class RapidBookingVelocityRule implements FraudRule {

    private static final int THRESHOLD = 5; // bookings within RiskProfile's rolling window

    @Override
    public Optional<String> evaluate(RiskSnapshot snapshot) {
        if (snapshot.recentBookingCount() >= THRESHOLD) {
            return Optional.of(snapshot.recentBookingCount() + " bookings created within the last hour");
        }
        return Optional.empty();
    }

    @Override public String name() { return "RAPID_BOOKING_VELOCITY"; }
}
