package com.travel.loyalty.domain.model;

/**
 * Direct structural mirror of WalletTransactionType (wallet-service,
 * Day 18) — EARNED/ADMIN_CREDIT are credits, REDEEMED/ADMIN_DEBIT are
 * debits, same isCredit() shape. Reused deliberately: it is the same
 * problem (a typed ledger entry with a credit/debit direction) solved
 * the same way, not a coincidence worth re-deriving from scratch.
 */
public enum LoyaltyTransactionType {
    EARNED,
    ADMIN_CREDIT,
    REDEEMED,
    ADMIN_DEBIT;

    public boolean isCredit() {
        return this == EARNED || this == ADMIN_CREDIT;
    }
}
