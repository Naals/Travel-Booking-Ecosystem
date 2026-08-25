package com.travel.fraud.domain.service;

import com.travel.fraud.domain.model.RiskSnapshot;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Catches card-testing patterns — many declines in a short window. */
@Component
public class PaymentFailureVelocityRule implements FraudRule {

    private static final int THRESHOLD = 3;

    @Override
    public Optional<String> evaluate(RiskSnapshot snapshot) {
        if (snapshot.recentPaymentFailureCount() >= THRESHOLD) {
            return Optional.of(snapshot.recentPaymentFailureCount() + " payment failures within the last hour");
        }
        return Optional.empty();
    }

    @Override public String name() { return "PAYMENT_FAILURE_VELOCITY"; }
}
