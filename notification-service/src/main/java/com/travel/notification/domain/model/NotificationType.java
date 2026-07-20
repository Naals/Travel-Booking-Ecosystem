package com.travel.notification.domain.model;

/**
 * Notification type determines which template is rendered
 * and which channel(s) receive it.
 */
public enum NotificationType {
    WELCOME,
    BOOKING_CONFIRMED,
    BOOKING_CANCELLED,
    PAYMENT_FAILED,
    PAYMENT_REFUNDED,
    REVIEW_REQUEST,
    LOYALTY_POINTS_EARNED,
    LOYALTY_TIER_UPGRADED,
    PASSWORD_RESET,
    EMAIL_VERIFICATION
}
