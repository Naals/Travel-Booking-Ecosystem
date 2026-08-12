package com.travel.messaging.domain.aggregate;

import com.travel.messaging.domain.event.ConversationBlockedEvent;
import com.travel.messaging.domain.event.ConversationStartedEvent;
import com.travel.messaging.domain.model.ConversationStatus;
import com.travel.messaging.domain.valueobject.ConversationContext;
import com.travel.messaging.domain.valueobject.ConversationId;
import com.travel.shared.domain.AggregateRoot;
import com.travel.common.exception.BusinessRuleViolationException;

import java.time.Instant;
import java.util.*;

/**
 * Conversation Aggregate Root.
 *
 * Owns participant membership, moderation status, and denormalized
 * "last message" / "last read" state used by the conversation-list
 * UI. Deliberately does NOT own the message collection itself — an
 * unbounded, ever-growing list loaded on every fetch would be the
 * same aggregate-size problem review-service avoided by keeping
 * ReviewEligibility and RatingSummary as separate read-models (Day 16).
 * Message is its own aggregate root instead (see Message.java).
 *
 * Read tracking uses a per-participant lastReadAt timestamp compared
 * against lastMessageAt, not a per-message read flag or an unread
 * counter — see ADR-009 for why.
 */
public class Conversation extends AggregateRoot<ConversationId> {

    private final Set<String>         participantIds;
    private final ConversationContext context;
    private ConversationStatus        status;
    private String                    lastMessagePreview;
    private String                    lastMessageSenderId;
    private Instant                   lastMessageAt;
    private final Map<String, Instant> lastReadAt;
    private final Instant             createdAt;
    private Instant                   updatedAt;

    // ── Private constructor ───────────────────────────────────────────────────

    private Conversation(ConversationId id, Set<String> participantIds,
                         ConversationContext context, Instant createdAt) {
        super(id);
        this.participantIds = participantIds;
        this.context         = context;
        this.status           = ConversationStatus.ACTIVE;
        this.lastReadAt       = new HashMap<>();
        this.createdAt        = createdAt;
        this.updatedAt        = createdAt;
    }

    // ── Factory methods ───────────────────────────────────────────────────────

    public static Conversation start(String initiatorId, String recipientId, ConversationContext context) {
        if (initiatorId.equals(recipientId))
            throw new BusinessRuleViolationException(
                "Cannot start a conversation with yourself", "INVALID_PARTICIPANTS");

        Set<String>    participants = new HashSet<>(Set.of(initiatorId, recipientId));
        ConversationId id           = ConversationId.generate();
        Conversation   conversation = new Conversation(id, participants, context, Instant.now());

        conversation.registerEvent(new ConversationStartedEvent(
            id.getValue(), initiatorId, recipientId,
            context.getType().name(), context.getBookingId()));

        return conversation;
    }

    /**
     * Reconstitutes from persistence, correctly restoring createdAt and
     * updatedAt from the stored values — see this commit's note on the
     * Day-7-onward bug this pattern avoids.
     */
    public static Conversation reconstitute(ConversationId id, Set<String> participantIds,
                                            ConversationContext context, ConversationStatus status,
                                            String lastMessagePreview, String lastMessageSenderId,
                                            Instant lastMessageAt, Map<String, Instant> lastReadAt,
                                            Instant createdAt, Instant updatedAt) {
        Conversation c = new Conversation(id, participantIds, context, createdAt);
        c.status              = status;
        c.lastMessagePreview  = lastMessagePreview;
        c.lastMessageSenderId = lastMessageSenderId;
        c.lastMessageAt       = lastMessageAt;
        c.lastReadAt.putAll(lastReadAt != null ? lastReadAt : Map.of());
        c.updatedAt           = updatedAt;
        return c;
    }

    // ── Access control ────────────────────────────────────────────────────────

    public void assertParticipant(String userId) {
        if (!participantIds.contains(userId))
            throw new BusinessRuleViolationException(
                "User is not a participant in this conversation", "NOT_A_PARTICIPANT");
    }

    /**
     * Combined check used by SendMessageUseCase before creating a
     * Message — participant membership AND not-blocked in one call.
     */
    public void assertCanReceiveMessageFrom(String senderId) {
        assertParticipant(senderId);
        if (status == ConversationStatus.BLOCKED)
            throw new BusinessRuleViolationException(
                "Cannot send messages in a blocked conversation", "CONVERSATION_BLOCKED");
    }

    public String otherParticipant(String userId) {
        return participantIds.stream()
            .filter(id -> !id.equals(userId))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "Conversation must have exactly two distinct participants"));
    }

    // ── Message denormalization ───────────────────────────────────────────────

    /**
     * Updates denormalized "last message" state. Called by
     * SendMessageUseCase immediately after a Message aggregate is
     * created and saved — trusts that assertCanReceiveMessageFrom()
     * was already checked in that same use case call, so it is not
     * re-validated here.
     */
    public void recordNewMessage(String senderId, String preview) {
        this.lastMessagePreview  = preview;
        this.lastMessageSenderId = senderId;
        this.lastMessageAt       = Instant.now();
        this.updatedAt           = Instant.now();
    }

    // ── Read tracking (see ADR-009) ───────────────────────────────────────────

    public void markReadBy(String userId) {
        assertParticipant(userId);
        this.lastReadAt.put(userId, Instant.now());
        this.updatedAt = Instant.now();
    }

    public boolean hasUnreadMessagesFor(String userId) {
        assertParticipant(userId);
        if (lastMessageAt == null) return false;
        if (userId.equals(lastMessageSenderId)) return false; // your own last message isn't "unread" to you
        Instant lastRead = lastReadAt.getOrDefault(userId, Instant.EPOCH);
        return lastMessageAt.isAfter(lastRead);
    }

    // ── Moderation ─────────────────────────────────────────────────────────────

    public void block(String byUserId) {
        assertParticipant(byUserId);
        if (status == ConversationStatus.BLOCKED)
            throw new BusinessRuleViolationException("Conversation is already blocked", "ALREADY_BLOCKED");
        this.status    = ConversationStatus.BLOCKED;
        this.updatedAt = Instant.now();
        registerEvent(new ConversationBlockedEvent(getId().getValue(), byUserId));
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public Set<String>          getParticipantIds()     { return Collections.unmodifiableSet(participantIds); }
    public ConversationContext  getContext()             { return context; }
    public ConversationStatus   getStatus()               { return status; }
    public String               getLastMessagePreview()  { return lastMessagePreview; }
    public String               getLastMessageSenderId() { return lastMessageSenderId; }
    public Instant               getLastMessageAt()       { return lastMessageAt; }
    public Map<String, Instant>  getLastReadAt()          { return Collections.unmodifiableMap(lastReadAt); }
    public Instant                getCreatedAt()           { return createdAt; }
    public Instant                getUpdatedAt()           { return updatedAt; }
}
