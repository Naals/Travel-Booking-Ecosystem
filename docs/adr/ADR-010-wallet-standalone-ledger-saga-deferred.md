# ADR-010: Wallet as a Standalone Ledger — Saga Integration Deliberately Deferred

## Status
Accepted

## Context
payment-service declared `PaymentMethod.WALLET` back on Day 8, alongside
`STRIPE` and `PAYPAL`, but `ProcessPaymentUseCase` has only ever
implemented the Stripe path. Building wallet-service today (Day 18) is
the natural moment to either finally wire WALLET up as an active choice
in the booking saga, or explicitly decide not to and say why.

Wiring it up means: when a customer chooses to pay via wallet,
payment-service needs to debit the customer's wallet balance instead
of charging a card. But payment-service does not own the Wallet
aggregate — wallet-service does. Two ways to bridge that:

1. A synchronous REST call from payment-service into wallet-service at
   charge time.
2. A new two-step sub-saga: payment-service publishes
   WalletDebitRequested, wallet-service consumes it and attempts
   wallet.debit(), then publishes WalletDebited or WalletDebitFailed,
   and ProcessPaymentUseCase's flow branches to wait for that response
   before calling Payment.complete() or Payment.fail().

## Decision
Neither is done today. Option 1 is rejected outright — every
cross-service interaction in this platform goes through Kafka
(ADR-001, and every saga participant since booking-service on Day 7),
and a synchronous call here would be the first exception to that rule,
undermining the platform's failure-isolation story for a single
convenience. Option 2 is real, valid future work, but means reopening
and re-testing payment-service's ProcessPaymentUseCase and
PaymentSagaConsumer (Day 8) to add a genuinely new branch to an
already-shipped, saga-critical code path — not something to rush
through as a side effect of building an unrelated new service on the
same day.

wallet-service is therefore built today as a correct, self-contained
ledger: balance is a cached field that can never drift from its
transaction history because every mutation goes through
credit()/debit() in the same call that appends the transaction (see
Wallet's Javadoc). The non-negative invariant is enforced both in the
aggregate (Money.subtract() rejects going below zero) and, redundantly,
at the database level via a CHECK constraint — the same
belt-and-suspenders pattern review-service's ReviewEligibility used
(an in-aggregate duplicate check backed by a unique Mongo index, Day
16), applied here as an in-aggregate reference check backed by a
partial unique Postgres index on (user_id, reference_id).

Top-up is implemented as a direct credit (TopUpWalletUseCase), not
routed through payment-service's Stripe integration — see that class's
Javadoc for why, which is the same synchronous-call problem above in
miniature: crediting a wallet only after a card charge succeeded would
need wallet-service to wait on a Stripe outcome it doesn't own.

## Consequences
Easier: wallet-service ships today as a complete, independently
testable, independently deployable unit with no dependency on
payment-service's internals. The two services can evolve at different
paces; wiring wallet-as-payment-method later is additive (a new
consumer + a new saga branch), not a rewrite of what exists today.

Harder: today's top-up is not backed by a real charge — a user can
credit their own wallet for free. Acceptable for a portfolio build
demonstrating the ledger's correctness rather than running a real
payment business; flagged explicitly here and in TopUpWalletUseCase's
Javadoc rather than silently shipped as if production-ready.
`PaymentMethod.WALLET` remains a dead enum constant in payment-service
— the second time this platform has knowingly shipped an enum value
ahead of its implementation, the first being
`MfaConfiguration.MfaType.SMS` (identity-service, Day 6), which
`SmsNotificationSender`'s Twilio stub (notification-service, Day 9)
still hasn't wired up either.

## Alternatives Considered
- Implement the full WalletDebitRequested sub-saga today — rejected as
  too large and too risky to fold into the same day as standing up a
  brand-new service; a saga change deserves its own focused day with
  its own dedicated regression pass over payment-service's existing
  saga tests (Day 8).
- Route top-up through a synchronous call to payment-service —
  rejected for breaking the platform's Kafka-only communication
  principle, the same reasoning that rules out option 1 above.
