package com.travel.fraud.domain.service;

import com.travel.fraud.domain.model.RiskSnapshot;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Collects every FraudRule bean via constructor injection — the same
 * "Spring gathers the polymorphic list" pattern NotificationDispatcher
 * (notification-service, Day 9) used for channel adapters. Unlike
 * RecommendationEngine (Day 20), which stayed static because its
 * inputs arrive as plain method parameters with no need for DI, this
 * class genuinely needs Spring to assemble the rule list — but remains
 * a pure computation (snapshot in, optional signal out) with no side
 * effects, so it stays in the domain layer rather than application.
 *
 * Rules run in whatever order Spring returns them and evaluation
 * short-circuits on the first match — rules are independent tripwires,
 * not meant to combine into a stronger signal.
 */
@Service
public class FraudRuleEngine {

    private final List<FraudRule> rules;

    public FraudRuleEngine(List<FraudRule> rules) {
        this.rules = List.copyOf(rules);
    }

    public Optional<TriggeredRule> evaluate(RiskSnapshot snapshot) {
        if (snapshot.alreadyFlagged()) return Optional.empty();

        for (FraudRule rule : rules) {
            Optional<String> reason = rule.evaluate(snapshot);
            if (reason.isPresent()) {
                return Optional.of(new TriggeredRule(rule.name(), reason.get()));
            }
        }
        return Optional.empty();
    }

    public record TriggeredRule(String ruleName, String reason) {}
}
