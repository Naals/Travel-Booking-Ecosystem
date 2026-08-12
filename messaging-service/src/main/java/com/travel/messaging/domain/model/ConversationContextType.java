package com.travel.messaging.domain.model;

public enum ConversationContextType {
    DIRECT,   // general user-to-user chat, no linked booking
    BOOKING   // tied to a specific bookingId — post-booking host/guest communication
}
