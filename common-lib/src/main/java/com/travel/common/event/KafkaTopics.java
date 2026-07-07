package com.travel.common.event;

/**
 * Centralized Kafka topic name constants for the travel platform.
 *
 * Naming convention: {domain}.{event-type}
 *
 * Partition key: always the aggregate ID — guarantees ordering per entity.
 *
 * Every producer and consumer in the platform references this class.
 * Changing a topic name here is a single-file change (plus Kafka Admin
 * re-creation); there is no duplication across service configs.
 */
public final class KafkaTopics {

    private KafkaTopics() {}

    // ── Identity ─────────────────────────────────────────────────────────────
    public static final String USER_REGISTERED   = "identity.user-registered";
    public static final String USER_UPDATED      = "identity.user-updated";
    public static final String USER_DEACTIVATED  = "identity.user-deactivated";

    // ── Booking ──────────────────────────────────────────────────────────────
    public static final String BOOKING_CREATED   = "booking.booking-created";
    public static final String BOOKING_CONFIRMED = "booking.booking-confirmed";
    public static final String BOOKING_CANCELLED = "booking.booking-cancelled";
    public static final String BOOKING_COMPLETED = "booking.booking-completed";

    // ── Payment ──────────────────────────────────────────────────────────────
    public static final String PAYMENT_REQUESTED = "payment.payment-requested";
    public static final String PAYMENT_COMPLETED = "payment.payment-completed";
    public static final String PAYMENT_FAILED    = "payment.payment-failed";
    public static final String REFUND_INITIATED  = "payment.refund-initiated";
    public static final String REFUND_COMPLETED  = "payment.refund-completed";

    // ── Inventory ────────────────────────────────────────────────────────────
    public static final String INVENTORY_RESERVATION_CONFIRMED = "inventory.reservation-confirmed";
    public static final String INVENTORY_RESERVATION_FAILED    = "inventory.reservation-failed";
    public static final String INVENTORY_RESERVATION_RELEASED  = "inventory.reservation-released";

    // ── Property ─────────────────────────────────────────────────────────────
    public static final String PROPERTY_CREATED              = "property.property-created";
    public static final String PROPERTY_AVAILABILITY_UPDATED = "property.availability-updated";

    // ── Hotel ────────────────────────────────────────────────────────────────
    public static final String HOTEL_ROOM_AVAILABILITY_UPDATED = "hotel.room-availability-updated";

    // ── Flight ───────────────────────────────────────────────────────────────
    public static final String FLIGHT_SEAT_AVAILABILITY_UPDATED = "flight.seat-availability-updated";

    // ── Vehicle ──────────────────────────────────────────────────────────────
    public static final String VEHICLE_AVAILABILITY_UPDATED = "vehicle.availability-updated";

    // ── Review ───────────────────────────────────────────────────────────────
    public static final String REVIEW_CREATED    = "review.review-created";
    public static final String REVIEW_MODERATED  = "review.review-moderated";

    // ── Notification ─────────────────────────────────────────────────────────
    public static final String NOTIFICATION_REQUESTED = "notification.notification-requested";

    // ── Search (indexing triggers) ────────────────────────────────────────────
    public static final String SEARCH_INDEX_PROPERTY = "search.index-property";
    public static final String SEARCH_INDEX_HOTEL    = "search.index-hotel";
    public static final String SEARCH_INDEX_FLIGHT   = "search.index-flight";
    public static final String SEARCH_INDEX_VEHICLE  = "search.index-vehicle";

    // ── Wallet ───────────────────────────────────────────────────────────────
    public static final String WALLET_CREDITED = "wallet.credited";
    public static final String WALLET_DEBITED  = "wallet.debited";

    // ── Loyalty ──────────────────────────────────────────────────────────────
    public static final String LOYALTY_POINTS_EARNED   = "loyalty.points-earned";
    public static final String LOYALTY_POINTS_REDEEMED = "loyalty.points-redeemed";
    public static final String LOYALTY_TIER_CHANGED    = "loyalty.tier-changed";

    // ── Messaging ────────────────────────────────────────────────────────────
    public static final String MESSAGE_SENT = "messaging.message-sent";

    // ── Fraud ────────────────────────────────────────────────────────────────
    public static final String FRAUD_CHECK_REQUESTED = "fraud.check-requested";
    public static final String FRAUD_ALERT_RAISED    = "fraud.alert-raised";

    // ── Analytics ────────────────────────────────────────────────────────────
    public static final String ANALYTICS_EVENT = "analytics.platform-event";

    // ── Audit ────────────────────────────────────────────────────────────────
    public static final String AUDIT_LOG_CREATED = "audit.log-created";

    // ── Dead Letter Queue suffix ──────────────────────────────────────────────
    public static final String DLQ_SUFFIX = ".dlq";

    public static String dlq(String topic) {
        return topic + DLQ_SUFFIX;
    }
}
