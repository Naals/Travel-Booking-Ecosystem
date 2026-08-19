# ADR-009: Timestamp-Based Read Tracking for Conversations

## Status
Accepted

## Context
messaging-service (Day 17) needs to answer "does this user have
unread messages in this conversation?" for the conversation-list UI.
Two common approaches: (1) a per-participant unread COUNT, incremented
on every new message and reset on read, or (2) a per-participant
lastReadAt timestamp compared against the conversation's lastMessageAt.

## Decision
Store `lastReadAt` as a `Map<participantId, Instant>` on the
Conversation aggregate. `hasUnreadMessagesFor(participantId)` is a
pure comparison — `lastMessageAt.isAfter(lastReadAt.getOrDefault(...))`
— excluding the case where the participant is themselves the last
sender. No counter field exists anywhere in the schema.

## Consequences
Easier: marking a conversation read is a single field write with no
read-then-increment race — contrast a counter, where two concurrent
message sends both reading count=3 and both writing count=4 would
silently lose an unread notification, the same class of bug
`findAndModify` was introduced in ADR-008 specifically to avoid for
review eligibility. A boolean is also sufficient for every UI this
platform currently needs (a badge/dot indicator).

Harder: cannot answer "how many unread messages," only "any." If an
exact-count badge is ever required, that's a straightforward addition
(count messages with `sentAt > lastReadAt`) — `GetConversationUseCase`
already has repository access to make it — but is out of scope until a
concrete need for the exact number exists rather than a boolean.

## Alternatives Considered
- Per-participant unread counter — rejected for the race-condition
  reason above; a counter's failure mode (silent under-counting) is
  worse than a boolean's (an imprecise "how many" this platform
  doesn't expose anyway).
- Per-message readBy set — rejected as the most accurate but most
  expensive option: marking a conversation read would require a bulk
  update across every message in it, for precision no current feature uses.
