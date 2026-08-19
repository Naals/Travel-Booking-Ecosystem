package com.travel.messaging.domain;

import com.travel.common.exception.BusinessRuleViolationException;
import com.travel.messaging.domain.aggregate.Conversation;
import com.travel.messaging.domain.event.ConversationBlockedEvent;
import com.travel.messaging.domain.event.ConversationStartedEvent;
import com.travel.messaging.domain.valueobject.ConversationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Conversation aggregate")
class ConversationTest {

    static final String USER_A = "user-a";
    static final String USER_B = "user-b";

    Conversation conversation;

    @BeforeEach
    void setUp() {
        conversation = Conversation.start(USER_A, USER_B, ConversationContext.direct());
        conversation.clearDomainEvents();
    }

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test @DisplayName("raises ConversationStartedEvent")
        void raisesEvent() {
            Conversation c = Conversation.start(USER_A, USER_B, ConversationContext.direct());
            assertThat(c.getDomainEvents()).hasSize(1);
            assertThat(c.getDomainEvents().get(0)).isInstanceOf(ConversationStartedEvent.class);
        }

        @Test @DisplayName("starts ACTIVE with both participants")
        void startsActive() {
            assertThat(conversation.getParticipantIds()).containsExactlyInAnyOrder(USER_A, USER_B);
        }

        @Test @DisplayName("rejects starting a conversation with yourself")
        void rejectsSelfConversation() {
            assertThatThrownBy(() -> Conversation.start(USER_A, USER_A, ConversationContext.direct()))
                .isInstanceOf(BusinessRuleViolationException.class);
        }
    }

    @Nested
    @DisplayName("Message denormalization")
    class MessageDenormalization {

        @Test @DisplayName("recordNewMessage updates last-message fields")
        void recordsNewMessage() {
            conversation.recordNewMessage(USER_A, "Hey there!");
            assertThat(conversation.getLastMessagePreview()).isEqualTo("Hey there!");
            assertThat(conversation.getLastMessageSenderId()).isEqualTo(USER_A);
            assertThat(conversation.getLastMessageAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Read tracking (see ADR-009)")
    class ReadTracking {

        @Test @DisplayName("no unread messages before any message is sent")
        void noUnreadInitially() {
            assertThat(conversation.hasUnreadMessagesFor(USER_B)).isFalse();
        }

        @Test @DisplayName("recipient sees unread after a message; sender does not")
        void unreadForRecipientOnly() {
            conversation.recordNewMessage(USER_A, "Hello");
            assertThat(conversation.hasUnreadMessagesFor(USER_B)).isTrue();
            assertThat(conversation.hasUnreadMessagesFor(USER_A)).isFalse();
        }

        @Test @DisplayName("markReadBy clears unread status")
        void markReadClearsUnread() {
            conversation.recordNewMessage(USER_A, "Hello");
            conversation.markReadBy(USER_B);
            assertThat(conversation.hasUnreadMessagesFor(USER_B)).isFalse();
        }

        @Test @DisplayName("a later message re-marks as unread even after a prior read")
        void laterMessageReUnreads() {
            conversation.recordNewMessage(USER_A, "First");
            conversation.markReadBy(USER_B);
            conversation.recordNewMessage(USER_A, "Second");
            assertThat(conversation.hasUnreadMessagesFor(USER_B)).isTrue();
        }
    }

    @Nested
    @DisplayName("Blocking")
    class Blocking {

        @Test @DisplayName("block raises ConversationBlockedEvent")
        void blockRaisesEvent() {
            conversation.block(USER_A);
            assertThat(conversation.getDomainEvents()).hasSize(1);
            assertThat(conversation.getDomainEvents().get(0)).isInstanceOf(ConversationBlockedEvent.class);
        }

        @Test @DisplayName("cannot block twice")
        void cannotBlockTwice() {
            conversation.block(USER_A);
            assertThatThrownBy(() -> conversation.block(USER_B))
                .isInstanceOf(BusinessRuleViolationException.class);
        }

        @Test @DisplayName("blocked conversation rejects new messages")
        void blockedRejectsMessages() {
            conversation.block(USER_A);
            assertThatThrownBy(() -> conversation.assertCanReceiveMessageFrom(USER_B))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("blocked");
        }
    }

    @Nested
    @DisplayName("Access control")
    class AccessControl {

        @Test @DisplayName("assertParticipant rejects a non-participant")
        void rejectsNonParticipant() {
            assertThatThrownBy(() -> conversation.assertParticipant("stranger"))
                .isInstanceOf(BusinessRuleViolationException.class);
        }

        @Test @DisplayName("otherParticipant returns the correct counterpart")
        void otherParticipant() {
            assertThat(conversation.otherParticipant(USER_A)).isEqualTo(USER_B);
            assertThat(conversation.otherParticipant(USER_B)).isEqualTo(USER_A);
        }
    }
}
