package com.travel.audit.domain.model;

/**
 * Deliberately narrow — four categories, not "one per service." See
 * ADR-015: wallet-service and loyalty-service already maintain their
 * own append-only transaction ledgers (WalletTransaction, Day 18;
 * LoyaltyTransaction, Day 19), so this service focuses on the events
 * that have no dedicated immutable trail anywhere else — identity
 * lifecycle, booking lifecycle, payment lifecycle, and fraud actions.
 */
public enum AuditCategory {
    IDENTITY,
    BOOKING,
    PAYMENT,
    FRAUD
}
