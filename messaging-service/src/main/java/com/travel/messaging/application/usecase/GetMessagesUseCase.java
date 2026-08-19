package com.travel.messaging.application.usecase;

import com.travel.common.response.PagedResponse;
import com.travel.messaging.application.dto.response.MessageResponse;
import com.travel.messaging.domain.repository.ConversationRepository;
import com.travel.messaging.domain.repository.MessageRepository;
import com.travel.messaging.domain.valueobject.ConversationId;
import com.travel.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetMessagesUseCase {

    private final ConversationRepository conversationRepository;
    private final MessageRepository      messageRepository;

    public PagedResponse<MessageResponse> execute(String conversationId, String requestingUserId,
                                                  int page, int size) {
        // Participant check first — prevents reading messages in a
        // conversation you don't belong to, just by guessing its ID.
        var conversation = conversationRepository.findById(ConversationId.of(conversationId))
            .orElseThrow(() -> new ResourceNotFoundException("Conversation", conversationId));
        conversation.assertParticipant(requestingUserId);

        var messages = messageRepository.findByConversationId(conversationId, page, size).stream()
            .map(MessageResponse::from)
            .toList();
        long total = messageRepository.countByConversationId(conversationId);
        return PagedResponse.of(messages, page, size, total);
    }
}
