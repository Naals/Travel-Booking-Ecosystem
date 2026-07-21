package com.travel.notification.infrastructure.email;

import com.travel.notification.domain.model.Notification;
import com.travel.notification.domain.model.NotificationChannel;
import com.travel.notification.domain.port.NotificationSender;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Email channel adapter.
 * Renders the Thymeleaf HTML template for the notification type,
 * then sends via Spring's JavaMailSender (SMTP / SendGrid).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationSender implements NotificationSender {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    private final EmailProperties properties;

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public void send(Notification notification) {
        try {
            String templateName = resolveTemplate(notification);
            String subject      = resolveSubject(notification);

            Context context = new Context();
            notification.getTemplateVariables().forEach(context::setVariable);

            String htmlBody = templateEngine.process(
                "email/" + templateName, context);

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                message, true, "UTF-8");

            helper.setFrom(properties.getFromAddress(),
                properties.getFromName());
            helper.setTo(notification.getRecipient());
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            javaMailSender.send(message);
            notification.markSent();

            log.info("Email sent: type={} to={}",
                notification.getType(), notification.getRecipient());

        } catch (Exception ex) {
            log.error("Email send failed: type={} to={} error={}",
                notification.getType(),
                notification.getRecipient(),
                ex.getMessage());
            notification.markFailed(ex.getMessage());
        }
    }

    private String resolveTemplate(Notification notification) {
        return switch (notification.getType()) {
            case WELCOME               -> "welcome";
            case BOOKING_CONFIRMED     -> "booking-confirmed";
            case BOOKING_CANCELLED     -> "booking-cancelled";
            case PAYMENT_FAILED        -> "payment-failed";
            case PAYMENT_REFUNDED      -> "payment-refunded";
            case REVIEW_REQUEST        -> "review-request";
            case LOYALTY_POINTS_EARNED -> "loyalty-points-earned";
            case LOYALTY_TIER_UPGRADED -> "loyalty-tier-upgraded";
            case PASSWORD_RESET        -> "password-reset";
            case EMAIL_VERIFICATION ->  "email-verification";
        };
    }

    private String resolveSubject(Notification notification) {
        return switch (notification.getType()) {
            case WELCOME               -> "Welcome to TravelPlatform!";
            case BOOKING_CONFIRMED     -> "Your booking is confirmed";
            case BOOKING_CANCELLED     -> "Your booking has been cancelled";
            case PAYMENT_FAILED        -> "Payment failed for your booking";
            case PAYMENT_REFUNDED      -> "Your refund has been processed";
            case REVIEW_REQUEST        -> "How was your stay? Leave a review";
            case LOYALTY_POINTS_EARNED -> "You earned loyalty points!";
            case LOYALTY_TIER_UPGRADED -> "Congratulations on your tier upgrade!";
            case PASSWORD_RESET        -> "Reset your password";
            case EMAIL_VERIFICATION    -> "Verify your email address";
        };
    }
}
