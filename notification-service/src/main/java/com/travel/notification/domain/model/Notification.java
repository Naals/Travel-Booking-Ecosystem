package com.travel.notification.domain.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Notification domain model.
 *
 * Intentionally not an aggregate root — notification-service is a
 * pure consumer. It does not write to any saga state and does not
 * raise domain events that other services react to.
 *
 * templateVariables holds the dynamic values injected into the
 * Thymeleaf template at render time (e.g. userName, bookingId).
 */
public class Notification {

    private final String              id;
    private final String              userId;
    private final String              recipient;         // email address or phone number
    private final NotificationChannel channel;
    private final NotificationType    type;
    private final Map<String, Object> templateVariables;
    private NotificationStatus        status;
    private String                    errorMessage;
    private final Instant             createdAt;

    private Notification(String userId, String recipient,
                         NotificationChannel channel, NotificationType type,
                         Map<String, Object> templateVariables) {
        this.id                = UUID.randomUUID().toString();
        this.userId            = userId;
        this.recipient         = recipient;
        this.channel           = channel;
        this.type              = type;
        this.templateVariables = templateVariables;
        this.status            = NotificationStatus.PENDING;
        this.createdAt         = Instant.now();
    }

    // ── Factory methods ───────────────────────────────────────────────────────

    public static Notification email(String userId, String email,
                                     NotificationType type,
                                     Map<String, Object> variables) {
        return new Notification(userId, email, NotificationChannel.EMAIL, type, variables);
    }

    public static Notification sms(String userId, String phoneNumber,
                                   NotificationType type,
                                   Map<String, Object> variables) {
        return new Notification(userId, phoneNumber, NotificationChannel.SMS, type, variables);
    }

    public static Notification push(String userId, String deviceToken,
                                    NotificationType type,
                                    Map<String, Object> variables) {
        return new Notification(userId, deviceToken, NotificationChannel.PUSH, type, variables);
    }

    // ── State transitions ─────────────────────────────────────────────────────

    public void markSent() {
        this.status = NotificationStatus.SENT;
    }

    public void markFailed(String error) {
        this.status       = NotificationStatus.FAILED;
        this.errorMessage = error;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String              getId()                { return id; }
    public String              getUserId()            { return userId; }
    public String              getRecipient()         { return recipient; }
    public NotificationChannel getChannel()           { return channel; }
    public NotificationType    getType()              { return type; }
    public Map<String, Object> getTemplateVariables() { return templateVariables; }
    public NotificationStatus  getStatus()            { return status; }
    public String              getErrorMessage()      { return errorMessage; }
    public Instant             getCreatedAt()         { return createdAt; }
}
