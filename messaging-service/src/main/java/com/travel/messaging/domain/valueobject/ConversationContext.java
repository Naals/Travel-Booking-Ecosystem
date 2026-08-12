package com.travel.messaging.domain.valueobject;

import com.travel.messaging.domain.model.ConversationContextType;
import com.travel.shared.domain.ValueObject;
import com.travel.common.exception.DomainException;
import java.util.Objects;

/**
 * bookingId is caller-supplied display metadata, not verified against
 * booking-service — see the main application class Javadoc for why
 * this service makes no synchronous cross-service calls.
 */
public final class ConversationContext implements ValueObject {

    private final ConversationContextType type;
    private final String                  bookingId; // null unless type == BOOKING

    private ConversationContext(ConversationContextType type, String bookingId) {
        if (type == ConversationContextType.BOOKING && (bookingId == null || bookingId.isBlank()))
            throw new DomainException("bookingId is required for BOOKING context", "INVALID_CONVERSATION_CONTEXT");
        if (type == ConversationContextType.DIRECT && bookingId != null)
            throw new DomainException("bookingId must not be set for DIRECT context", "INVALID_CONVERSATION_CONTEXT");
        this.type      = type;
        this.bookingId = bookingId;
    }

    public static ConversationContext direct()                    { return new ConversationContext(ConversationContextType.DIRECT, null); }
    public static ConversationContext forBooking(String bookingId) { return new ConversationContext(ConversationContextType.BOOKING, bookingId); }

    public ConversationContextType getType()      { return type; }
    public String                  getBookingId() { return bookingId; }

    @Override public boolean equals(Object o) {
        return o instanceof ConversationContext c
            && type == c.type && Objects.equals(bookingId, c.bookingId);
    }
    @Override public int hashCode() { return Objects.hash(type, bookingId); }
}
