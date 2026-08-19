package com.travel.messaging.application.usecase;

import com.travel.messaging.application.dto.request.SendMessageRequest;
import com.travel.messaging.application.dto.response.MessageResponse;
import com.travel.messaging.domain.aggregate.Conversation;
import com.travel.messaging.domain.aggregate.Message;
import com.travel.messaging.domain.repository.ConversationRepository;
import com.travel.messaging.domain.repository.MessageRepository;
import com.travel.messaging.domain.valueobject.ConversationId;
import com.travel.messaging.domain.valueobject.MessageContent;
import com.travel.messaging.infrastructure.messaging.producer.MessagingEventPublisher;
import com.travel.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendMessageUseCase {

    private final ConversationRepository  conversationRepository;
    private final MessageRepository       messageRepository;
    private final MessagingEventPublisher eventPublisher;

    public MessageResponse execute(String conversationId, String senderId, SendMessageRequest request) {
        Conversation conversation = conversationRepository.findById(ConversationId.of(conversationId))
            .orElseThrow(() -> new ResourceNotFoundException("Conversation", conversationId));

        conversation.assertCanReceiveMessageFrom(senderId);
        String recipientId = conversation.otherParticipant(senderId);

        MessageContent content = MessageContent.of(request.content());
        Message        message = Message.send(conversationId, senderId, recipientId, content);
        Message        savedMessage = messageRepository.save(message);

        // Message and Conversation are separate MongoDB documents,
        // written non-transactionally — see ADR-008 on why this
        // platform avoids multi-document Mongo transactions locally.
        // If the process crashed between these two saves, Conversation's
        // denormalized preview/lastMessageAt would briefly lag; the
        // Message collection itself remains complete and correct
        // regardless, and the next successful send re-syncs it. This is
        // acceptable because that denormalized state exists purely to
        // optimize the conversation-list UI, unlike ReviewEligibility's
        // findAndModify (Day 16), which guards an actual double-spend.
        conversation.recordNewMessage(senderId, content.preview());
        conversationRepository.save(conversation);

        eventPublisher.publishEvents(savedMessage.getDomainEvents());
        savedMessage.clearDomainEvents();

        log.info("Message sent: {} in conversation {}", savedMessage.getId().getValue(), conversationId);
        return MessageResponse.from(savedMessage);
    }
}
