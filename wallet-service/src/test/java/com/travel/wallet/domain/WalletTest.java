package com.travel.wallet.domain;

import com.travel.common.exception.BusinessRuleViolationException;
import com.travel.wallet.domain.aggregate.Wallet;
import com.travel.wallet.domain.event.*;
import com.travel.wallet.domain.model.WalletStatus;
import com.travel.wallet.domain.model.WalletTransactionType;
import com.travel.wallet.domain.valueobject.Money;
import com.travel.wallet.domain.valueobject.WalletId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Wallet aggregate")
class WalletTest {

    static final WalletId USER_ID = WalletId.of("user-123");

    Wallet wallet;

    @BeforeEach
    void setUp() {
        wallet = Wallet.provision(USER_ID);
        wallet.clearDomainEvents();
    }

    @Nested
    @DisplayName("Provisioning")
    class Provisioning {

        @Test @DisplayName("starts with zero balance and ACTIVE status")
        void startsAtZero() {
            assertThat(wallet.getBalance().getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(wallet.getStatus()).isEqualTo(WalletStatus.ACTIVE);
            assertThat(wallet.getCurrency()).isEqualTo("USD");
        }

        @Test @DisplayName("raises WalletCreatedEvent")
        void raisesEvent() {
            Wallet w = Wallet.provision(WalletId.of("user-456"));
            assertThat(w.getDomainEvents()).hasSize(1);
            assertThat(w.getDomainEvents().get(0)).isInstanceOf(WalletCreatedEvent.class);
        }
    }

    @Nested
    @DisplayName("Credit")
    class Credit {

        @Test @DisplayName("increases balance and raises WalletCreditedEvent")
        void creditIncreasesBalance() {
            wallet.credit(Money.of(new BigDecimal("50.00"), "USD"),
                WalletTransactionType.TOPUP, "ref-1", "Top-up");

            assertThat(wallet.getBalance().getAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
            assertThat(wallet.getTransactions()).hasSize(1);
            assertThat(wallet.getDomainEvents().get(0)).isInstanceOf(WalletCreditedEvent.class);
        }

        @Test @DisplayName("records balanceAfter on the transaction")
        void recordsBalanceAfter() {
            wallet.credit(Money.of(new BigDecimal("30.00"), "USD"), WalletTransactionType.TOPUP, "ref-1", "Top-up");
            wallet.credit(Money.of(new BigDecimal("20.00"), "USD"), WalletTransactionType.TOPUP, "ref-2", "Top-up");

            assertThat(wallet.getTransactions().get(1).getBalanceAfter().getAmount())
                .isEqualByComparingTo(new BigDecimal("50.00"));
        }

        @Test @DisplayName("rejects a duplicate idempotency reference")
        void rejectsDuplicateReference() {
            wallet.credit(Money.of(new BigDecimal("10.00"), "USD"), WalletTransactionType.TOPUP, "same-ref", "Top-up");

            assertThatThrownBy(() -> wallet.credit(
                Money.of(new BigDecimal("10.00"), "USD"), WalletTransactionType.TOPUP, "same-ref", "Top-up"))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("already been recorded");
        }

        @Test @DisplayName("rejects a DEBIT-typed transaction through credit()")
        void rejectsDebitTypeThroughCredit() {
            assertThatThrownBy(() -> wallet.credit(
                Money.of(new BigDecimal("10.00"), "USD"), WalletTransactionType.DEBIT, null, "wrong type"))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("not a credit transaction type");
        }

        @Test @DisplayName("cannot credit a frozen wallet")
        void cannotCreditFrozen() {
            wallet.freeze("Suspected fraud");
            assertThatThrownBy(() -> wallet.credit(
                Money.of(new BigDecimal("10.00"), "USD"), WalletTransactionType.TOPUP, null, "Top-up"))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("frozen");
        }
    }

    @Nested
    @DisplayName("Debit")
    class Debit {

        @BeforeEach
        void fundWallet() {
            wallet.credit(Money.of(new BigDecimal("100.00"), "USD"), WalletTransactionType.TOPUP, "seed", "Initial funds");
            wallet.clearDomainEvents();
        }

        @Test @DisplayName("decreases balance and raises WalletDebitedEvent")
        void debitDecreasesBalance() {
            wallet.debit(Money.of(new BigDecimal("40.00"), "USD"), WalletTransactionType.ADMIN_DEBIT, null, "Correction");

            assertThat(wallet.getBalance().getAmount()).isEqualByComparingTo(new BigDecimal("60.00"));
            assertThat(wallet.getDomainEvents().get(0)).isInstanceOf(WalletDebitedEvent.class);
        }

        @Test @DisplayName("rejects a debit exceeding the balance")
        void rejectsInsufficientBalance() {
            assertThatThrownBy(() -> wallet.debit(
                Money.of(new BigDecimal("500.00"), "USD"), WalletTransactionType.ADMIN_DEBIT, null, "Too much"))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Insufficient wallet balance");
        }

        @Test @DisplayName("rejects a CREDIT-typed transaction through debit()")
        void rejectsCreditTypeThroughDebit() {
            assertThatThrownBy(() -> wallet.debit(
                Money.of(new BigDecimal("10.00"), "USD"), WalletTransactionType.TOPUP, null, "wrong type"))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("not a debit transaction type");
        }

        @Test @DisplayName("exact-balance debit succeeds, leaving zero")
        void exactBalanceDebit() {
            wallet.debit(Money.of(new BigDecimal("100.00"), "USD"), WalletTransactionType.ADMIN_DEBIT, null, "Drain");
            assertThat(wallet.getBalance().getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("Freeze / unfreeze")
    class FreezeUnfreeze {

        @Test @DisplayName("freeze raises WalletFrozenEvent")
        void freezeRaisesEvent() {
            wallet.freeze("Suspicious activity");
            assertThat(wallet.getStatus()).isEqualTo(WalletStatus.FROZEN);
            assertThat(wallet.getDomainEvents().get(0)).isInstanceOf(WalletFrozenEvent.class);
        }

        @Test @DisplayName("cannot freeze twice")
        void cannotFreezeTwice() {
            wallet.freeze("First");
            assertThatThrownBy(() -> wallet.freeze("Second"))
                .isInstanceOf(BusinessRuleViolationException.class);
        }

        @Test @DisplayName("unfreeze restores ACTIVE status")
        void unfreezeRestoresActive() {
            wallet.freeze("Reason");
            wallet.unfreeze();
            assertThat(wallet.getStatus()).isEqualTo(WalletStatus.ACTIVE);
        }

        @Test @DisplayName("cannot unfreeze an active wallet")
        void cannotUnfreezeActive() {
            assertThatThrownBy(wallet::unfreeze)
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("not frozen");
        }
    }

    @Nested
    @DisplayName("Money value object")
    class MoneyTests {

        @Test @DisplayName("rejects negative amount")
        void rejectsNegative() {
            assertThatThrownBy(() -> Money.of(new BigDecimal("-5.00"), "USD"))
                .isInstanceOf(com.travel.common.exception.DomainException.class);
        }

        @Test @DisplayName("subtract below zero throws")
        void subtractBelowZero() {
            Money ten    = Money.of(new BigDecimal("10.00"), "USD");
            Money twenty = Money.of(new BigDecimal("20.00"), "USD");
            assertThatThrownBy(() -> ten.subtract(twenty))
                .isInstanceOf(com.travel.common.exception.DomainException.class);
        }

        @Test @DisplayName("isLessThan compares correctly")
        void isLessThan() {
            Money ten    = Money.of(new BigDecimal("10.00"), "USD");
            Money twenty = Money.of(new BigDecimal("20.00"), "USD");
            assertThat(ten.isLessThan(twenty)).isTrue();
            assertThat(twenty.isLessThan(ten)).isFalse();
        }
    }
}
