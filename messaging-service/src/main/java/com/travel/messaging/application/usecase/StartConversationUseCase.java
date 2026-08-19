package com.travel.messaging.application.usecase;

import com.travel.messaging.application.dto.request.StartConversationRequest;
import com.travel.messaging.application.dto.response.ConversationResponse;
import com.travel.messaging.domain.aggregate.Conversation;
import com.travel.messaging.domain.repository.ConversationRepository;
import com.travel.messaging.domain.valueobject.ConversationContext;
import com.travel.messaging.infrastructure.messaging.producer.MessagingEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StartConversationUseCase {

    private final ConversationRepository    repository;
    private final MessagingEventPublisher   eventPublisher;

    public ConversationResponse execute(String userId, StartConversationRequest request) {
        ConversationContext context = "BOOKING".equalsIgnoreCase(request.contextType())
            ? ConversationContext.forBooking(request.bookingId())
            : ConversationContext.direct();

        // Only DIRECT threads are deduplicated — a guest may have
        // several separate BOOKING conversations with the same host
        // across different stays, and each deserves its own thread.
        if (context.getType() == com.travel.messaging.domain.model.ConversationContextType.DIRECT) {
            Optional<Conversation> existing =
                repository.findDirectConversationBetween(userId, request.recipientId());
            if (existing.isPresent()) {
                log.debug("Reusing existing DIRECT conversation between {} and {}",
                    userId, request.recipientId());
                return ConversationResponse.from(existing.get(), userId);
            }
        }

        Conversation conversation = Conversation.start(userId, request.recipientId(), context);
        Conversation saved        = repository.save(conversation);

        eventPublisher.publishEvents(saved.getDomainEvents());
        saved.clearDomainEvents();

        log.info("Conversation started: {} between {} and {}",
            saved.getId().getValue(), userId, request.recipientId());
        return ConversationResponse.from(saved, userId);
    }
}
