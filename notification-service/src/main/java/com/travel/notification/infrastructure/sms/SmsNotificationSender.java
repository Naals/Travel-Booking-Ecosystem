package com.travel.notification.infrastructure.sms;

import com.travel.notification.domain.model.Notification;
import com.travel.notification.domain.model.NotificationChannel;
import com.travel.notification.domain.port.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * SMS channel adapter.
 *
 * Production: integrates with Twilio REST API.
 * Current implementation is a structured stub — logs the message
 * that would be sent. Replace the send() body with Twilio SDK calls
 * and inject TWILIO_ACCOUNT_SID / TWILIO_AUTH_TOKEN from config.
 *
 * Kept as a stub to avoid pulling in the Twilio SDK dependency
 * before the feature is fully spec'd.
 */
@Slf4j
@Component
public class SmsNotificationSender implements NotificationSender {

    @Value("${notification.sms.from-number:+10000000000}")
    private String fromNumber;

    @Value("${notification.sms.enabled:false}")
    private boolean enabled;

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.SMS;
    }

    @Override
    public void send(Notification notification) {
        if (!enabled) {
            log.info("SMS disabled — would send to={} type={}",
                notification.getRecipient(), notification.getType());
            notification.markSent();
            return;
        }

        try {
            String body = resolveSmsBody(notification);
            // TODO: replace with Twilio SDK call
            // Message.creator(
            //     new PhoneNumber(notification.getRecipient()),
            //     new PhoneNumber(fromNumber),
            //     body).create();
            log.info("SMS sent: to={} type={} body={}",
                notification.getRecipient(), notification.getType(), body);
            notification.markSent();
        } catch (Exception ex) {
            log.error("SMS send failed: to={} error={}",
                notification.getRecipient(), ex.getMessage());
            notification.markFailed(ex.getMessage());
        }
    }

    private String resolveSmsBody(Notification notification) {
        return switch (notification.getType()) {
            case BOOKING_CONFIRMED -> "Your TravelPlatform booking is confirmed! " +
                "Booking ID: " + notification.getTemplateVariables().get("bookingId");
            case BOOKING_CANCELLED -> "Your TravelPlatform booking has been cancelled.";
            case PAYMENT_FAILED    -> "Payment failed for your TravelPlatform booking. " +
                "Please update your payment method.";
            case PASSWORD_RESET    -> "Your TravelPlatform password reset code: " +
                notification.getTemplateVariables().get("resetCode");
            default -> "You have a new notification from TravelPlatform.";
        };
    }
}
