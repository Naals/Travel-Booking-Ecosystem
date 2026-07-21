package com.travel.notification.application;

import com.travel.notification.application.service.NotificationDispatcher;
import com.travel.notification.domain.model.*;
import com.travel.notification.domain.port.NotificationSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("NotificationDispatcher")
class NotificationDispatcherTest {

    NotificationSender emailSender;
    NotificationSender smsSender;
    NotificationDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        emailSender = mock(NotificationSender.class);
        smsSender   = mock(NotificationSender.class);

        when(emailSender.channel()).thenReturn(NotificationChannel.EMAIL);
        when(smsSender.channel()).thenReturn(NotificationChannel.SMS);

        dispatcher = new NotificationDispatcher(List.of(emailSender, smsSender));
    }

    @Test
    @DisplayName("routes EMAIL notification to email sender")
    void routesEmail() {
        Notification n = Notification.email("u1", "a@a.com",
            NotificationType.WELCOME, Map.of());

        dispatcher.dispatch(n);

        verify(emailSender).send(n);
        verify(smsSender, never()).send(any());
    }

    @Test
    @DisplayName("routes SMS notification to sms sender")
    void routesSms() {
        Notification n = Notification.sms("u1", "+1234567890",
            NotificationType.BOOKING_CONFIRMED, Map.of());

        dispatcher.dispatch(n);

        verify(smsSender).send(n);
        verify(emailSender, never()).send(any());
    }

    @Test
    @DisplayName("marks FAILED when no sender registered for channel")
    void noSenderRegistered() {
        Notification n = Notification.push("u1", "fcm-token",
            NotificationType.LOYALTY_POINTS_EARNED, Map.of());

        dispatcher.dispatch(n);

        assertThat(n.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(n.getErrorMessage()).contains("No sender registered");
    }
}
