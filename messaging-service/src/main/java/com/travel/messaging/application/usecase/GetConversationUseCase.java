package com.travel.messaging.application.usecase;

import com.travel.common.response.PagedResponse;
import com.travel.messaging.application.dto.response.ConversationResponse;
import com.travel.messaging.domain.repository.ConversationRepository;
import com.travel.messaging.domain.valueobject.ConversationId;
import com.travel.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetConversationUseCase {

    private final ConversationRepository repository;

    public ConversationResponse execute(String conversationId, String requestingUserId) {
        var conversation = repository.findById(ConversationId.of(conversationId))
            .orElseThrow(() -> new ResourceNotFoundException("Conversation", conversationId));
        conversation.assertParticipant(requestingUserId);
        return ConversationResponse.from(conversation, requestingUserId);
    }

    public PagedResponse<ConversationResponse> executeForUser(String userId, int page, int size) {
        var conversations = repository.findByParticipantId(userId, page, size).stream()
            .map(c -> ConversationResponse.from(c, userId))
            .toList();
        long total = repository.countByParticipantId(userId);
        return PagedResponse.of(conversations, page, size, total);
    }
}
