package com.travel.loyalty.domain;

import com.travel.common.exception.BusinessRuleViolationException;
import com.travel.loyalty.domain.aggregate.LoyaltyAccount;
import com.travel.loyalty.domain.event.*;
import com.travel.loyalty.domain.model.LoyaltyTier;
import com.travel.loyalty.domain.model.LoyaltyTransactionType;
import com.travel.loyalty.domain.valueobject.LoyaltyAccountId;
import com.travel.loyalty.domain.valueobject.Points;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("LoyaltyAccount aggregate")
class LoyaltyAccountTest {

    static final LoyaltyAccountId USER_ID = LoyaltyAccountId.of("user-123");

    LoyaltyAccount account;

    @BeforeEach
    void setUp() {
        account = LoyaltyAccount.provision(USER_ID);
        account.clearDomainEvents();
    }

    @Nested
    @DisplayName("Provisioning")
    class Provisioning {

        @Test @DisplayName("starts at zero, BRONZE tier")
        void startsAtZero() {
            assertThat(account.getBalance().getValue()).isZero();
            assertThat(account.getLifetimePointsEarned().getValue()).isZero();
            assertThat(account.getTier()).isEqualTo(LoyaltyTier.BRONZE);
        }

        @Test @DisplayName("raises LoyaltyAccountCreatedEvent")
        void raisesEvent() {
            LoyaltyAccount a = LoyaltyAccount.provision(LoyaltyAccountId.of("user-456"));
            assertThat(a.getDomainEvents()).hasSize(1);
            assertThat(a.getDomainEvents().get(0)).isInstanceOf(LoyaltyAccountCreatedEvent.class);
        }
    }

    @Nested
    @DisplayName("Earning points")
    class EarningPoints {

        @Test @DisplayName("increases both balance and lifetime earned")
        void earnIncreasesBoth() {
            account.earnPoints(Points.of(1000), LoyaltyTransactionType.EARNED, "booking-1", "Trip reward");

            assertThat(account.getBalance().getValue()).isEqualTo(1000L);
            assertThat(account.getLifetimePointsEarned().getValue()).isEqualTo(1000L);
        }

        @Test @DisplayName("raises only LoyaltyPointsEarnedEvent when tier doesn't change")
        void noTierChange() {
            account.earnPoints(Points.of(100), LoyaltyTransactionType.EARNED, "booking-1", "Small trip");

            assertThat(account.getDomainEvents()).hasSize(1);
            assertThat(account.getDomainEvents().get(0)).isInstanceOf(LoyaltyPointsEarnedEvent.class);
            assertThat(account.getTier()).isEqualTo(LoyaltyTier.BRONZE);
        }

        @Test @DisplayName("raises both events when a tier threshold is crossed")
        void raisesTierChangeEvent() {
            account.earnPoints(Points.of(5_000), LoyaltyTransactionType.EARNED, "booking-1", "Big trip");

            assertThat(account.getTier()).isEqualTo(LoyaltyTier.SILVER);
            assertThat(account.getDomainEvents()).hasSize(2);
            assertThat(account.getDomainEvents().get(1)).isInstanceOf(LoyaltyTierChangedEvent.class);

            LoyaltyTierChangedEvent event = (LoyaltyTierChangedEvent) account.getDomainEvents().get(1);
            assertThat(event.getPreviousTier()).isEqualTo("BRONZE");
            assertThat(event.getNewTier()).isEqualTo("SILVER");
        }

        @Test @DisplayName("rejects a debit-typed transaction through earnPoints()")
        void rejectsDebitType() {
            assertThatThrownBy(() -> account.earnPoints(
                Points.of(100), LoyaltyTransactionType.REDEEMED, null, "wrong type"))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("not a credit transaction type");
        }
    }

    @Nested
    @DisplayName("Redeeming points")
    class RedeemingPoints {

        @BeforeEach
        void seedBalance() {
            account.earnPoints(Points.of(1000), LoyaltyTransactionType.EARNED, "booking-1", "Seed");
            account.clearDomainEvents();
        }

        @Test @DisplayName("decreases balance but NOT lifetime earned")
        void redeemDoesNotAffectLifetime() {
            account.redeemPoints(Points.of(300), LoyaltyTransactionType.REDEEMED, null, "Redeemed");

            assertThat(account.getBalance().getValue()).isEqualTo(700L);
            assertThat(account.getLifetimePointsEarned().getValue()).isEqualTo(1000L); // unchanged
        }

        @Test @DisplayName("redeeming never lowers tier (ADR-011)")
        void redeemDoesNotLowerTier() {
            account.earnPoints(Points.of(4_100), LoyaltyTransactionType.EARNED, "booking-2", "Reach SILVER");
            assertThat(account.getTier()).isEqualTo(LoyaltyTier.SILVER);

            account.redeemPoints(Points.of(5_000), LoyaltyTransactionType.REDEEMED, null, "Spend it all");

            assertThat(account.getBalance().getValue()).isEqualTo(100L);
            assertThat(account.getTier()).isEqualTo(LoyaltyTier.SILVER); // still SILVER
        }

        @Test @DisplayName("rejects redemption exceeding balance")
        void rejectsInsufficientBalance() {
            assertThatThrownBy(() -> account.redeemPoints(
                Points.of(5_000), LoyaltyTransactionType.REDEEMED, null, "Too much"))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Insufficient points balance");
        }

        @Test @DisplayName("rejects a credit-typed transaction through redeemPoints()")
        void rejectsCreditType() {
            assertThatThrownBy(() -> account.redeemPoints(
                Points.of(100), LoyaltyTransactionType.EARNED, null, "wrong type"))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("not a debit transaction type");
        }
    }

    @Nested
    @DisplayName("Points value object")
    class PointsTests {

        @Test @DisplayName("rejects negative points")
        void rejectsNegative() {
            assertThatThrownBy(() -> Points.of(-1))
                .isInstanceOf(com.travel.common.exception.DomainException.class);
        }

        @Test @DisplayName("subtract below zero throws")
        void subtractBelowZero() {
            assertThatThrownBy(() -> Points.of(10).subtract(Points.of(20)))
                .isInstanceOf(com.travel.common.exception.DomainException.class);
        }
    }
}
