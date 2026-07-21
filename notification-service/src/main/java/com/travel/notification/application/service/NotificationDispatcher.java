package com.travel.notification.application.service;

import com.travel.notification.domain.model.Notification;
import com.travel.notification.domain.model.NotificationChannel;
import com.travel.notification.domain.port.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Routes notifications to the correct channel adapter.
 *
 * Spring injects all NotificationSender beans at startup.
 * The dispatcher builds a map of channel → sender and uses it
 * to route at runtime. Adding a new channel means adding a new
 * adapter bean — this class never changes.
 */
@Slf4j
@Service
public class NotificationDispatcher {

    private final Map<NotificationChannel, NotificationSender> senders;

    public NotificationDispatcher(List<NotificationSender> senderList) {
        this.senders = senderList.stream()
            .collect(Collectors.toMap(
                NotificationSender::channel,
                Function.identity()));
        log.info("NotificationDispatcher initialized with channels: {}",
            senders.keySet());
    }

    /**
     * Dispatches a notification to its channel adapter.
     * Logs a warning if no adapter is registered for the channel.
     */
    public void dispatch(Notification notification) {
        NotificationSender sender = senders.get(notification.getChannel());

        if (sender == null) {
            log.warn("No sender registered for channel: {}", notification.getChannel());
            notification.markFailed("No sender registered for channel: "
                + notification.getChannel());
            return;
        }

        log.info("Dispatching {} notification type={} to={}",
            notification.getChannel(),
            notification.getType(),
            notification.getRecipient());

        sender.send(notification);
    }
}
