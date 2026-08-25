# ADR-013: Closed-Loop Fraud Detection via Local Rule Engine and Automated Wallet Freeze

## Status
Accepted

## Context
fraud-service (Day 21) must implement "suspicious activity detection"
and a "rule engine" per the original spec. Two forks: (1) heuristic
rules vs. a real ML pipeline, and (2) what a triggered rule should
*do* — detection with no consequence is not useful.

wallet-service's WalletFrozenEvent (Day 18) was published with an
explicit note that no consumer existed yet, "intended for a future
fraud-service ... to potentially react to." Building fraud-service is
the natural moment to both honor that and complete the other direction
of the loop: fraud-service needs a way to *cause* a freeze, not just
observe one.

## Decision
**Heuristic rules, not ML** — the same choice ADR-012 made for
recommendation-service the day before. FraudRuleEngine collects every
FraudRule bean via constructor injection, reusing the "Spring
assembles the polymorphic list" pattern NotificationDispatcher
(notification-service, Day 9) established for channel adapters. Three
illustrative rules ship today (booking velocity, payment-failure
velocity, new-account rapid booking) — thresholds are not tuned
against real data, the same honest scope limit as loyalty-service's
point/tier thresholds (Day 19).

**A closed, three-hop loop, entirely over Kafka:**

fraud-service wallet-service
│ │
├─ raises FraudAlertRaised ─────►│
│ ├─ Wallet.freeze()
│ ├─ raises WalletFrozenEvent
│◄─────── WalletFrozenEvent ─────┤
├─ RiskProfile.onWalletFrozen()
│ (flagged=true, no new event — loop terminates)

No synchronous call in either direction, consistent with every
inter-service decision since ADR-001. wallet-service's new
FraudEventConsumer reuses FreezeWalletUseCase (Day 18) unchanged — an
automated freeze and a staff-triggered one are indistinguishable to
the Wallet aggregate, which is the correct behavior: both are "this
wallet needs to stop moving money until someone reviews it."

**Windowed velocity counting lives in PostgreSQL**, via
@ElementCollection tables of raw timestamps, pruned and re-filtered on
every read. This is not how a real high-volume fraud system would
track rolling windows — Redis sorted sets or a stream-processing
engine (Kafka Streams, Flink) would avoid the read-modify-write-per-
event cost this design has at scale. Accepted as sufficient to
demonstrate the rule-engine pattern itself at this project's scope,
consistent with the same class of tradeoff ADR-007 made for
search-service's data completeness.

**No idempotency guard on individual signal recording.** A redelivered
BookingCreated would add a duplicate timestamp, slightly over-counting
velocity. Accepted for the same reason ADR-012 accepted it for
recommendation-service's signals: these are soft, self-correcting
detection inputs, not a financial ledger — unlike WalletTransaction
(Day 18) or SpendRecord (Day 19), which do need and have a hard
duplicate guard because money is actually at stake there.

**FRAUD_CHECK_REQUESTED remains unused** after today. The topic name
implies some other service would request an on-demand check, but every
evaluation in this design is event-driven (fires automatically on
booking-created / payment-failed) rather than requested. No producer
exists for it anywhere in the platform. Documented here rather than
forced into existence to close the gap artificially — the same
treatment given to PaymentMethod.WALLET (ADR-010) and MFA's missing
Twilio wiring.

## Consequences
Easier: the entire detection surface is two small domain classes
(RiskProfile, FraudRuleEngine) plus three trivial rule
implementations — auditable and unit-testable with no external
dependency. Adding a fourth rule is a new `@Component` with no other
code changes, the same extensibility NotificationDispatcher's channel
adapters demonstrated for notification-service.

Harder: detection quality is coarse — fixed thresholds, no per-user
baseline, no negative/trust signals that would lower risk over time.
A cleared flag resets both windows to zero, meaning a user who was
correctly cleared and then immediately resumes the exact behavior that
got them flagged has to cross the threshold fresh — reasonable for a
portfolio system, generous compared to what a production fraud team
would likely want (some systems retain a "prior offender" weighting).

## Alternatives Considered
- A real rules engine library (Drools, Easy Rules) — rejected as
  disproportionate infrastructure for three illustrative rules; the
  hand-rolled FraudRuleEngine is a few dozen lines and fully
  type-safe, with no XML/DSL layer to maintain.
- Redis-backed sliding-window counters instead of Postgres timestamp
  tables — genuinely the better production choice, deliberately not
  built today; would need a new infrastructure dependency this
  platform doesn't otherwise use for counting (Redis appears
  elsewhere only for API-gateway rate limiting, Day 5, and session
  caching — never wired into any domain service's own persistence).
