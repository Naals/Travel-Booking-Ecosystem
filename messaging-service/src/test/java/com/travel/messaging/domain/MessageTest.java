package com.travel.messaging.domain;

import com.travel.common.exception.BusinessRuleViolationException;
import com.travel.messaging.domain.aggregate.Message;
import com.travel.messaging.domain.event.MessageDeletedEvent;
import com.travel.messaging.domain.event.MessageSentEvent;
import com.travel.messaging.domain.valueobject.MessageContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Message aggregate")
class MessageTest {

    static final String CONVERSATION_ID = "conv-1";
    static final String SENDER_ID       = "user-a";
    static final String RECIPIENT_ID    = "user-b";

    Message message;

    @BeforeEach
    void setUp() {
        message = Message.send(CONVERSATION_ID, SENDER_ID, RECIPIENT_ID,
            MessageContent.of("Is this property still available for June?"));
    }

    @Nested
    @DisplayName("Sending")
    class Sending {

        @Test @DisplayName("raises MessageSentEvent")
        void raisesEvent() {
            assertThat(message.getDomainEvents()).hasSize(1);
            assertThat(message.getDomainEvents().get(0)).isInstanceOf(MessageSentEvent.class);
        }

        @Test @DisplayName("content is retrievable before deletion")
        void contentPresent() {
            assertThat(message.getContent()).isPresent();
            assertThat(message.getContent().get().getValue())
                .isEqualTo("Is this property still available for June?");
        }
    }

    @Nested
    @DisplayName("Deletion")
    class Deletion {

        @Test @DisplayName("sender can delete their own message, content is redacted")
        void senderCanDelete() {
            message.clearDomainEvents();
            message.delete(SENDER_ID);

            assertThat(message.getContent()).isEmpty();
            assertThat(message.getDomainEvents().get(0)).isInstanceOf(MessageDeletedEvent.class);
        }

        @Test @DisplayName("non-sender cannot delete")
        void nonSenderCannotDelete() {
            assertThatThrownBy(() -> message.delete(RECIPIENT_ID))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Only the sender");
        }

        @Test @DisplayName("cannot delete twice")
        void cannotDeleteTwice() {
            message.delete(SENDER_ID);
            assertThatThrownBy(() -> message.delete(SENDER_ID))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("already deleted");
        }
    }
}
