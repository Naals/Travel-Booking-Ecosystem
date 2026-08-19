package com.travel.messaging.application.usecase;

import com.travel.messaging.application.dto.response.ConversationResponse;
import com.travel.messaging.domain.repository.ConversationRepository;
import com.travel.messaging.domain.valueobject.ConversationId;
import com.travel.messaging.infrastructure.messaging.producer.MessagingEventPublisher;
import com.travel.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BlockConversationUseCase {

    private final ConversationRepository  repository;
    private final MessagingEventPublisher eventPublisher;

    public ConversationResponse execute(String conversationId, String userId) {
        var conversation = repository.findById(ConversationId.of(conversationId))
            .orElseThrow(() -> new ResourceNotFoundException("Conversation", conversationId));
        conversation.block(userId);
        var saved = repository.save(conversation);

        eventPublisher.publishEvents(saved.getDomainEvents());
        saved.clearDomainEvents();

        return ConversationResponse.from(saved, userId);
    }
}
