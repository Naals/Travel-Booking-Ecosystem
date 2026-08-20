package com.travel.wallet.domain.model;

/**
 * TOPUP and ADMIN_CREDIT are credit transactions; DEBIT and
 * ADMIN_DEBIT are debit transactions — see isCredit(). Wallet.credit()
 * and Wallet.debit() each assert the type matches the operation being
 * performed, so a caller can never accidentally record a debit-typed
 * transaction through credit() or vice versa.
 */
public enum WalletTransactionType {
    TOPUP,
    ADMIN_CREDIT,
    DEBIT,
    ADMIN_DEBIT;

    public boolean isCredit() {
        return this == TOPUP || this == ADMIN_CREDIT;
    }
}
