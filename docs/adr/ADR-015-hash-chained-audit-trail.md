# ADR-015: Hash-Chained Audit Trail with Serialized Append

## Status
Accepted

## Context
audit-service (Day 23, final service) must implement "audit logs" and
"compliance tracking" per the original spec. Two forks: (1) what makes
an audit log meaningfully different from any other append-only
projection already in this platform (DailyBookingMetric, Day 22;
WalletTransaction, Day 18), and (2) which events actually belong here,
given 21 services worth of candidates.

## Decision

**Tamper-evidence via a lightweight hash chain.** Each AuditLogEntry
stores the SHA-256 hash of its own content (sequence number, category,
source event type/id, subject, summary, occurredAt) concatenated with
the *previous* entry's hash. HashChainService computes it;
ChainIntegrityVerifier — a pure function taking the full chain in
sequence order — recomputes every hash and confirms the chain of
`previousHash` references is unbroken. Changing any field on any past
entry, however small, changes that entry's hash and therefore every
hash computed after it; `VerifyChainIntegrityUseCase` (exposed via a
dedicated ADMIN-only endpoint) makes this checkable on demand, not
just a theoretical property. This is not a blockchain — no consensus,
no distributed ledger, no proof-of-work — just the one idea from that
space actually relevant to a single-writer compliance log: a hash
chain proves *sequence and content* haven't been altered after the
fact.

**Serialized append via a locked singleton row.** Computing a new
entry's hash requires knowing the current chain tail, so every writer
must agree on that tail without racing. `AuditChainRepository
.lockHeadForAppend()` acquires a Postgres `SELECT ... FOR UPDATE` on a
single `audit_chain_head` row before anything else happens in
`RecordAuditEntryUseCase`. This is a genuine, deliberate throughput
ceiling — every append across all four Kafka consumers, platform-wide,
serializes through one lock. Accepted because it is the correct
tradeoff for what a hash chain fundamentally is: a strictly sequential
structure. A production system at far higher volume would replace this
with an append-only log store designed for the purpose (e.g. a
Kafka-backed event log with a single designated writer, or a database
purpose-built for ledgers) rather than a hand-rolled Postgres lock —
noted as the natural evolution path, not built today.

**The chain-head row is seeded by Flyway, not lazily created.** A
lazy "if no row exists, create one" fallback would race under
concurrent cold-start replicas — two instances could both observe no
row and both attempt to insert the singleton. Seeding it directly in
the V1 migration means the row is guaranteed to exist before any
application code runs; `lockById()` can assume it and simply fail loud
(`IllegalStateException`) if it's ever missing, rather than silently
recovering from a state that migrations should have prevented.

**Deduplication is inlined into `RecordAuditEntryUseCase`, not a
separate pre-check.** analytics-service's Inbox pattern (ADR-014, Day
22) checks `EventDeduplicationRepository` *before* invoking its use
    case, because there "is this a duplicate" and "how do I bucket this
    metric" are independent questions. Here they are not: determining
    whether `sourceEventId` has already been recorded and determining this
    entry's position in the chain both require the same lock, held for the
    same reason. Checking for duplicates before acquiring that lock would
    let two concurrent deliveries of the same event both pass the check
    before either commits — precisely the race the lock exists to prevent.
    `sourceEventId` is enforced UNIQUE in the schema regardless, as a
    backstop.

**Scope: identity, booking, payment, fraud — not everything.** The
single most important design insight of this service: wallet-service
and loyalty-service already maintain their own dedicated, append-only,
balance-tracking ledgers (`WalletTransaction`, Day 18;
`LoyaltyTransaction`, Day 19, each with its own `balanceAfter`
snapshot). Duplicating them into audit-service would add a second,
lower-fidelity copy of information that already has a authoritative
home. audit-service instead covers exactly the categories with no
existing dedicated trail — who registered or left, what happened to a
booking, what happened to a payment, what fraud actions were taken —
and documents the boundary explicitly rather than silently omitting
wallet/loyalty and leaving a reviewer to wonder why.

**Entity, not AggregateRoot.** `AuditLogEntry` is the platform's first
model to extend shared-kernel's `Entity<ID>` directly (Day 2) rather
than either `AggregateRoot` (every mutable domain object since Day 6)
or a plain unannotated class (every prior read-only projection —
`SearchDocument` Day 14, `UserAffinity`/`DestinationPopularity` Day 20,
`DailyBookingMetric`/`DailyRevenueMetric` Day 22). It has real,
permanent, single-column identity like `Booking` or `Payment` — unlike
those composite-keyed projections — but it never mutates and never
calls `registerEvent()`, so `AggregateRoot`'s event-accumulation
machinery buys nothing here. `AuditLogCreatedEvent` is constructed
directly by `RecordAuditEntryUseCase` instead — the first time this
platform has needed exactly this combination, and exactly why
shared-kernel split `Entity` from `AggregateRoot` in the first place.

**The last Day-3 topic, closed on the last day.**
`KafkaTopics.AUDIT_LOG_CREATED` was declared in common-lib's very
first version, immediately before `DLQ_SUFFIX` — the final real
constant in that file. It is the sixth and last instance of the
"seeded ahead of its producer" pattern recurring throughout this
build (`REVIEW_CREATED` Day 3→16, `MESSAGE_SENT` Day 3→17,
`WALLET_CREDITED` Day 3→18, `LOYALTY_POINTS_EARNED` Day 3→19,
`FRAUD_ALERT_RAISED` Day 3→21). The very last thing declared on Day 3
is the very last thing implemented, on Day 23.

## Consequences
Easier: tamper-evidence is a small, fully unit-testable domain
concern (`HashChainService` + `ChainIntegrityVerifier`, both pure
functions, no mocks needed) rather than infrastructure bolted on
after the fact; the scope boundary (4 categories, not 21) is
principled and explainable rather than arbitrary.

Harder: the single global lock is a real, accepted bottleneck —
platform-wide audit-entry throughput is capped by however fast
Postgres can serialize `SELECT ... FOR UPDATE` round trips, which is
fine at this platform's scale and would need re-architecting at
meaningfully higher volume. `findAllOrderedBySequence()` for
integrity verification is a full, unpaginated table scan by design —
acceptable today, a real scale concern for a production deployment
with millions of entries, where incremental/checkpointed verification
would be the natural next step.

## Alternatives Considered
- A real append-only ledger database (e.g. QLDB-style, or a dedicated
  event-sourcing store) instead of Postgres + a hand-rolled lock —
  rejected as new infrastructure this platform doesn't otherwise use,
  disproportionate to demonstrating the pattern at portfolio scale.
- Per-partition or per-category hash chains (four independent chains
  instead of one global one) to relieve the single-lock bottleneck —
  rejected for v1: a single global chain gives the strongest possible
  guarantee (a total, verifiable order across every compliance-relevant
  event platform-wide), which is more valuable to demonstrate than the
  throughput this would recover; a real high-volume system might
  reasonably make this tradeoff differently.
