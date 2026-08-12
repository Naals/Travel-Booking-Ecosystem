package com.travel.messaging.domain.valueobject;

import com.travel.shared.domain.ValueObject;
import com.travel.common.exception.DomainException;
import java.util.Objects;

public final class MessageContent implements ValueObject {

    private static final int MAX_LENGTH     = 2000;
    private static final int PREVIEW_LENGTH = 100;

    private final String value;

    private MessageContent(String value) {
        if (value == null || value.isBlank())
            throw new DomainException("Message content must not be empty", "INVALID_MESSAGE_CONTENT");
        String trimmed = value.trim();
        if (trimmed.length() > MAX_LENGTH)
            throw new DomainException(
                "Message must not exceed " + MAX_LENGTH + " characters", "INVALID_MESSAGE_CONTENT");
        this.value = trimmed;
    }

    public static MessageContent of(String value) { return new MessageContent(value); }

    public String getValue() { return value; }

    /**
     * Truncated form used for denormalized display (Conversation's
     * lastMessagePreview, MessageSentEvent's contentPreview). Kept
     * deliberately short so downstream consumers (e.g. notification
     * emails) never carry the full message body — see MessageSentEvent.
     */
    public String preview() {
        return value.length() <= PREVIEW_LENGTH ? value : value.substring(0, PREVIEW_LENGTH) + "...";
    }

    @Override public boolean equals(Object o) {
        return o instanceof MessageContent c && value.equals(c.value);
    }
    @Override public int hashCode() { return Objects.hash(value); }
}
