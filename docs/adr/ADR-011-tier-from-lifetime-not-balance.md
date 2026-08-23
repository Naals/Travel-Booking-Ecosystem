# ADR-011: Loyalty Tier Computed from Lifetime Earned Points, Not Current Balance

## Status
Accepted

## Context
loyalty-service (Day 19) needs a rule for what determines a member's
tier (BRONZE/SILVER/GOLD/PLATINUM). The naive approach — base it on
`balance`, the same field redemption decreases — has a real, common
industry pitfall: a member could earn their way to GOLD, redeem points
for a reward, and immediately be demoted back to BRONZE. That is not
how any credible loyalty program behaves, and would make redemption
actively punishing.

## Decision
LoyaltyAccount tracks two separate figures: `balance` (spendable, moves
both directions) and `lifetimePointsEarned` (monotonically
non-decreasing, only ever increased by earnPoints()). Tier is computed
solely from `lifetimePointsEarned` via TierCalculationPolicy. A
database CHECK constraint (`balance <= lifetime_points_earned`) backs
this at the schema level as a sanity guard, not because the domain
layer could otherwise violate it — redeemPoints() never touches
lifetimePointsEarned, so the invariant holds by construction; the
constraint exists as a second line of defense against a future bug
that bypasses the aggregate.

## Consequences
Easier: redemption is a pure decision the customer controls — there is
no version of "should I redeem, or will it hurt my status" tension.
Tier only ever moves upward, which is also simpler to explain to a
member than a rule with edge cases.

Harder: an admin ADMIN_CREDIT adjustment (support goodwill) also
counts toward lifetime earned and can therefore push a tier up — a
deliberate choice (see AdminAdjustPointsUseCase's Javadoc), but worth
being explicit that "lifetime earned" includes non-booking-driven
credits, not purely organic spend.

## Alternatives Considered
- Tier from current balance — rejected for the redemption-punishes-you
  problem described above.
- A rolling 12-month earned window (typical of real airline programs,
  which recalculate tier annually) — rejected as unnecessary complexity
  for this platform's scope; a straightforward lifetime total is
  sufficient to demonstrate the underlying domain insight without
  building a full expiration/recalculation scheduler.
