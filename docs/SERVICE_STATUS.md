# Service implementation status

| # | Service | Tier | Status | Day |
|---|---|---|---|---|
| 1 | discovery-server | 0 | ✅ core complete | Day 4 |
| 2 | config-server | 0 | ✅ core complete | Day 4 |
| 3 | api-gateway | 0 | ✅ core complete | Day 5 |
| 4 | identity-service | 1 | ✅ core complete | Day 6 |
| 5 | booking-service | 1 | ✅ core complete | Day 7 (event enriched Day 15) |
| 6 | payment-service | 1 | ✅ core complete | Day 8 |
| 7 | notification-service | 1 | ✅ core complete | Day 9 (extended Day 17) |
| 8 | property-service | 2 | ✅ core complete | Day 10 |
| 9 | hotel-service | 2 | ✅ core complete | Day 11 |
| 10 | flight-service | 2 | ✅ core complete | Day 12 |
| 11 | vehicle-service | 2 | ✅ core complete | Day 13 |
| 12 | search-service | 2 | ✅ core complete | Day 14 |
| 13 | user-service | 3 | ✅ core complete | Day 15 |
| 14 | review-service | 3 | ✅ core complete | Day 16 |
| 15 | messaging-service | 3 | ✅ core complete | Day 17 |
| 16 | wallet-service | 3 | ✅ core complete | Day 18 |
| 17 | loyalty-service | 3 | not started | — |
| 18 | recommendation-service | 4 | not started | — |
| 19 | fraud-service | 4 | not started | — |
| 20 | analytics-service | 4 | not started | — |
| 21 | audit-service | 4 | not started | — |

## Shared modules
| Module | Status | Day |
|---|---|---|
| shared-kernel | ✅ core complete | Day 2 |
| common-lib | ✅ core complete | Day 3 (extended Days 15–18) |

## Known issues (tech debt)
- **Stale `createdAt`/`updatedAt` on reconstitution**: Booking (Day 7),
  Property (Day 10), Hotel (Day 11), Flight (Day 12), Vehicle (Day 13),
  UserProfile (Day 15), and Review (Day 16) all unconditionally set
  `createdAt = Instant.now()` in their private constructors;
  `reconstitute()` accepts the true value but never applies it. Fixed
  going forward starting with Conversation/Message (Day 17) and
  continued in Wallet (Day 18). Back-porting to the services above is
  a future hardening pass.

## Intentional deferrals (see linked ADRs)
- **search-service's `rating` field** (Day 14, ADR-007) is populated
  only from hotel-service's static star rating, never from actual
  review activity — `ResourceRatingUpdatedEvent` (review-service, Day
    16) is the natural signal to close this gap, not yet consumed.
- **`PaymentMethod.WALLET`** (payment-service, Day 8) remains
  unimplemented after wallet-service (Day 18) exists — see ADR-010 for
  why wiring wallet debit into the booking saga is real future work,
  not an oversight.
- **MFA** (identity-service, Day 6): `MfaConfiguration` and
  `User.enableMfa()` exist in the domain model, but no REST endpoint
  or use case exposes MFA enrollment, and `SmsNotificationSender`
  (notification-service, Day 9) is a structured stub with no live
  Twilio integration.

## Tier 3 in progress (4 of 5)
wallet-service returns to PostgreSQL after two MongoDB services (Days
16–17). Auto-provisioned from identity.user-registered like
user-service (Day 15); balance is a ledger, never a bare mutable
field — every credit/debit appends a WalletTransaction in the same
call, backed by a duplicate-reference guard at both the aggregate and
database level.
