package com.travel.messaging.application.usecase;

import com.travel.messaging.application.dto.response.MessageResponse;
import com.travel.messaging.domain.repository.MessageRepository;
import com.travel.messaging.domain.valueobject.MessageId;
import com.travel.messaging.infrastructure.messaging.producer.MessagingEventPublisher;
import com.travel.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteMessageUseCase {

    private final MessageRepository       repository;
    private final MessagingEventPublisher eventPublisher;

    public MessageResponse execute(String conversationId, String messageId, String requestingUserId) {
        var message = repository.findById(MessageId.of(messageId))
            .orElseThrow(() -> new ResourceNotFoundException("Message", messageId));

        // Treat a conversationId/messageId mismatch as not-found rather
        // than exposing that the message exists in a different thread.
        if (!message.getConversationId().equals(conversationId))
            throw new ResourceNotFoundException("Message", messageId);

        message.delete(requestingUserId);
        var saved = repository.save(message);

        eventPublisher.publishEvents(saved.getDomainEvents());
        saved.clearDomainEvents();

        return MessageResponse.from(saved);
    }
}
