package com.travel.notification.infrastructure.push;

import com.travel.notification.domain.model.Notification;
import com.travel.notification.domain.model.NotificationChannel;
import com.travel.notification.domain.port.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Push notification channel adapter.
 *
 * Production: integrates with Firebase Cloud Messaging (FCM).
 * Current implementation is a structured stub — logs the push payload
 * that would be sent. Replace send() with FCM SDK calls.
 *
 * recipient field = FCM device registration token.
 */
@Slf4j
@Component
public class PushNotificationSender implements NotificationSender {

    @Value("${notification.push.enabled:false}")
    private boolean enabled;

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.PUSH;
    }

    @Override
    public void send(Notification notification) {
        if (!enabled) {
            log.info("Push disabled — would send to={} type={}",
                notification.getRecipient(), notification.getType());
            notification.markSent();
            return;
        }

        try {
            PushPayload payload = buildPayload(notification);
            // TODO: replace with FCM SDK call
            // FirebaseMessaging.getInstance().send(
            //     Message.builder()
            //         .setToken(notification.getRecipient())
            //         .setNotification(FCMNotification.builder()
            //             .setTitle(payload.title())
            //             .setBody(payload.body())
            //             .build())
            //         .build());
            log.info("Push sent: to={} title={} body={}",
                notification.getRecipient(), payload.title(), payload.body());
            notification.markSent();
        } catch (Exception ex) {
            log.error("Push send failed: to={} error={}",
                notification.getRecipient(), ex.getMessage());
            notification.markFailed(ex.getMessage());
        }
    }

    private PushPayload buildPayload(Notification notification) {
        return switch (notification.getType()) {
            case BOOKING_CONFIRMED -> new PushPayload(
                "Booking Confirmed ✓",
                "Your booking has been confirmed. Have a great trip!");
            case BOOKING_CANCELLED -> new PushPayload(
                "Booking Cancelled",
                "Your booking has been cancelled.");
            case PAYMENT_FAILED -> new PushPayload(
                "Payment Failed",
                "Your payment could not be processed. Please try again.");
            case LOYALTY_POINTS_EARNED -> new PushPayload(
                "Points Earned! 🎉",
                "You earned " + notification.getTemplateVariables().get("points")
                    + " loyalty points.");
            default -> new PushPayload(
                "TravelPlatform",
                "You have a new notification.");
        };
    }

    record PushPayload(String title, String body) {}
}
