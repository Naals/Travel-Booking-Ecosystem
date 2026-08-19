package com.travel.messaging.infrastructure.web.controller;

import com.travel.common.response.ApiResponse;
import com.travel.common.response.PagedResponse;
import com.travel.messaging.application.dto.request.SendMessageRequest;
import com.travel.messaging.application.dto.request.StartConversationRequest;
import com.travel.messaging.application.dto.response.ConversationResponse;
import com.travel.messaging.application.dto.response.MessageResponse;
import com.travel.messaging.application.usecase.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
@Tag(name = "Messaging", description = "User-to-user chat and booking communication")
public class ConversationController {

    private final StartConversationUseCase   startUseCase;
    private final SendMessageUseCase          sendMessageUseCase;
    private final GetConversationUseCase      getConversationUseCase;
    private final GetMessagesUseCase          getMessagesUseCase;
    private final MarkConversationReadUseCase markReadUseCase;
    private final BlockConversationUseCase    blockUseCase;
    private final DeleteMessageUseCase        deleteMessageUseCase;

    @PostMapping
    @Operation(summary = "Start a conversation, or return the existing DIRECT thread with this recipient")
    public ResponseEntity<ApiResponse<ConversationResponse>> start(
        @RequestHeader("X-User-Id") String userId,
        @Valid @RequestBody StartConversationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(startUseCase.execute(userId, request)));
    }

    @GetMapping("/my")
    @Operation(summary = "List conversations for the authenticated user, most recently active first")
    public ResponseEntity<ApiResponse<PagedResponse<ConversationResponse>>> getMyConversations(
        @RequestHeader("X-User-Id") String userId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(getConversationUseCase.executeForUser(userId, page, size)));
    }

    @GetMapping("/{conversationId}")
    @Operation(summary = "Get a single conversation")
    public ResponseEntity<ApiResponse<ConversationResponse>> getById(
        @PathVariable String conversationId,
        @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(ApiResponse.ok(getConversationUseCase.execute(conversationId, userId)));
    }

    @PostMapping("/{conversationId}/read")
    @Operation(summary = "Mark a conversation as read up to now")
    public ResponseEntity<ApiResponse<ConversationResponse>> markRead(
        @PathVariable String conversationId,
        @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(ApiResponse.ok(markReadUseCase.execute(conversationId, userId)));
    }

    @PostMapping("/{conversationId}/block")
    @Operation(summary = "Block a conversation, preventing further messages")
    public ResponseEntity<ApiResponse<ConversationResponse>> block(
        @PathVariable String conversationId,
        @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(ApiResponse.ok(blockUseCase.execute(conversationId, userId)));
    }

    @PostMapping("/{conversationId}/messages")
    @Operation(summary = "Send a message in a conversation")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
        @PathVariable String conversationId,
        @RequestHeader("X-User-Id") String userId,
        @Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(sendMessageUseCase.execute(conversationId, userId, request)));
    }

    @GetMapping("/{conversationId}/messages")
    @Operation(summary = "List messages in a conversation, most recent first")
    public ResponseEntity<ApiResponse<PagedResponse<MessageResponse>>> getMessages(
        @PathVariable String conversationId,
        @RequestHeader("X-User-Id") String userId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "30") int size) {
        return ResponseEntity.ok(ApiResponse.ok(getMessagesUseCase.execute(conversationId, userId, page, size)));
    }

    @DeleteMapping("/{conversationId}/messages/{messageId}")
    @Operation(summary = "Delete your own message")
    public ResponseEntity<ApiResponse<MessageResponse>> deleteMessage(
        @PathVariable String conversationId,
        @PathVariable String messageId,
        @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(ApiResponse.ok(deleteMessageUseCase.execute(conversationId, messageId, userId)));
    }
}
