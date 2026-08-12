package com.travel.messaging.domain.aggregate;

import com.travel.messaging.domain.event.MessageDeletedEvent;
import com.travel.messaging.domain.event.MessageSentEvent;
import com.travel.messaging.domain.model.MessageStatus;
import com.travel.messaging.domain.valueobject.MessageContent;
import com.travel.messaging.domain.valueobject.MessageId;
import com.travel.shared.domain.AggregateRoot;
import com.travel.common.exception.BusinessRuleViolationException;

import java.time.Instant;
import java.util.Optional;

/**
 * Message Aggregate Root.
 *
 * Independent from Conversation — see Conversation's class Javadoc.
 * Owns its own small lifecycle: SENT → DELETED (soft-delete with
 * actual content redaction, not just a status flag — see delete()).
 */
public class Message extends AggregateRoot<MessageId> {

    private final String  conversationId;
    private final String  senderId;
    private final String  recipientId;
    private MessageContent content; // becomes null on delete — see getContent()
    private MessageStatus status;
    private final Instant sentAt;
    private Instant       deletedAt;

    // ── Private constructor ───────────────────────────────────────────────────

    private Message(MessageId id, String conversationId, String senderId, String recipientId,
                    MessageContent content, Instant sentAt) {
        super(id);
        this.conversationId = conversationId;
        this.senderId        = senderId;
        this.recipientId     = recipientId;
        this.content          = content;
        this.status            = MessageStatus.SENT;
        this.sentAt             = sentAt;
    }

    // ── Factory methods ───────────────────────────────────────────────────────

    public static Message send(String conversationId, String senderId,
                               String recipientId, MessageContent content) {
        MessageId id      = MessageId.generate();
        Message   message = new Message(id, conversationId, senderId, recipientId, content, Instant.now());

        message.registerEvent(new MessageSentEvent(
            id.getValue(), conversationId, senderId, recipientId, content.preview()));

        return message;
    }

    /** Reconstitutes correctly restoring sentAt — see Conversation's equivalent note. */
    public static Message reconstitute(MessageId id, String conversationId, String senderId,
                                       String recipientId, MessageContent content,
                                       MessageStatus status, Instant sentAt, Instant deletedAt) {
        Message m = new Message(id, conversationId, senderId, recipientId, content, sentAt);
        m.status    = status;
        m.deletedAt = deletedAt;
        return m;
    }

    // ── Deletion ───────────────────────────────────────────────────────────────

    /**
     * Redacts the message content outright (content set to null),
     * rather than merely flagging it deleted — a real removal, not a
     * cosmetic hide, consistent with how a chat participant would
     * expect "delete" to behave.
     */
    public void delete(String requestingUserId) {
        if (!senderId.equals(requestingUserId))
            throw new BusinessRuleViolationException("Only the sender can delete a message", "FORBIDDEN");
        if (status == MessageStatus.DELETED)
            throw new BusinessRuleViolationException("Message is already deleted", "ALREADY_DELETED");
        this.status    = MessageStatus.DELETED;
        this.content    = null;
        this.deletedAt = Instant.now();
        registerEvent(new MessageDeletedEvent(getId().getValue(), conversationId, requestingUserId));
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String                 getConversationId() { return conversationId; }
    public String                 getSenderId()        { return senderId; }
    public String                 getRecipientId()     { return recipientId; }
    public Optional<MessageContent> getContent()       { return Optional.ofNullable(content); }
    public MessageStatus           getStatus()          { return status; }
    public Instant                  getSentAt()          { return sentAt; }
    public Instant                  getDeletedAt()       { return deletedAt; }
}
