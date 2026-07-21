package com.travel.notification.domain.port;

import com.travel.notification.domain.model.Notification;
import com.travel.notification.domain.model.NotificationChannel;

/**
 * Port (domain interface) for sending notifications.
 *
 * Each channel (EMAIL, SMS, PUSH) has its own adapter implementing
 * this interface. The NotificationDispatcher resolves the correct
 * adapter at runtime based on Notification.getChannel().
 *
 * Adding a new provider (e.g. switching from SendGrid to Mailgun)
 * means creating a new adapter — zero changes to domain or consumers.
 */
public interface NotificationSender {

    /**
     * Sends the notification via this adapter's channel.
     * Implementations must set notification.markSent() on success
     * or notification.markFailed(reason) on failure.
     */
    void send(Notification notification);

    /**
     * Returns the channel this sender handles.
     * Used by NotificationDispatcher for routing.
     */
    NotificationChannel channel();
}
