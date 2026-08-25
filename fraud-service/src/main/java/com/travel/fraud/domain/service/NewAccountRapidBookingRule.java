package com.travel.fraud.domain.service;

import com.travel.fraud.domain.model.RiskSnapshot;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/** Catches fraud rings that register and immediately start booking. */
@Component
public class NewAccountRapidBookingRule implements FraudRule {

    private static final Duration NEW_ACCOUNT_WINDOW = Duration.ofMinutes(10);
    private static final int      THRESHOLD           = 3;

    @Override
    public Optional<String> evaluate(RiskSnapshot snapshot) {
        boolean isNewAccount = snapshot.accountAge().compareTo(NEW_ACCOUNT_WINDOW) < 0;
        if (isNewAccount && snapshot.recentBookingCount() >= THRESHOLD) {
            return Optional.of("Account under 10 minutes old with " +
                snapshot.recentBookingCount() + " bookings already created");
        }
        return Optional.empty();
    }

    @Override public String name() { return "NEW_ACCOUNT_RAPID_BOOKING"; }
}
