package com.travel.messaging.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record StartConversationRequest(
    @NotBlank(message = "recipientId is required")
    String recipientId,

    @NotBlank
    @Pattern(regexp = "DIRECT|BOOKING", message = "contextType must be DIRECT or BOOKING")
    String contextType,

    // Required only when contextType = BOOKING — validated by
    // ConversationContext's constructor, not Bean Validation here.
    String bookingId
) {}
