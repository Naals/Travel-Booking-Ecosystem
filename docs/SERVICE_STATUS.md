# Service implementation status

| # | Service | Tier | Status | Day |
|---|---|---|---|---|
| 1 | discovery-server | 0 | ✅ core complete | Day 4 |
| 2 | config-server | 0 | ✅ core complete | Day 4 |
| 3 | api-gateway | 0 | ✅ core complete | Day 5 |
| 4 | identity-service | 1 | ✅ core complete | Day 6 |
| 5 | booking-service | 1 | ✅ core complete | Day 7 (event enriched Day 15) |
| 6 | payment-service | 1 | ✅ core complete | Day 8 |
| 7 | notification-service | 1 | ✅ core complete | Day 9 (extended Days 17, 19) |
| 8 | property-service | 2 | ✅ core complete | Day 10 |
| 9 | hotel-service | 2 | ✅ core complete | Day 11 |
| 10 | flight-service | 2 | ✅ core complete | Day 12 |
| 11 | vehicle-service | 2 | ✅ core complete | Day 13 |
| 12 | search-service | 2 | ✅ core complete | Day 14 |
| 13 | user-service | 3 | ✅ core complete | Day 15 |
| 14 | review-service | 3 | ✅ core complete | Day 16 |
| 15 | messaging-service | 3 | ✅ core complete | Day 17 |
| 16 | wallet-service | 3 | ✅ core complete | Day 18 |
| 17 | loyalty-service | 3 | ✅ core complete | Day 19 |
| 18 | recommendation-service | 4 | not started | — |
| 19 | fraud-service | 4 | not started | — |
| 20 | analytics-service | 4 | not started | — |
| 21 | audit-service | 4 | not started | — |

## Shared modules
| Module | Status | Day |
|---|---|---|
| shared-kernel | ✅ core complete | Day 2 |
| common-lib | ✅ core complete | Day 3 (extended Days 15–19) |

## Known issues (tech debt)
- **Stale `createdAt`/`updatedAt` on reconstitution**: Booking (Day 7),
  Property (Day 10), Hotel (Day 11), Flight (Day 12), Vehicle (Day 13),
  UserProfile (Day 15), and Review (Day 16) still discard the true
  createdAt on reconstitute(). Fixed in every aggregate built since
  Conversation/Message (Day 17): Wallet (Day 18), LoyaltyAccount
  (Day 19). Back-porting to the seven listed above remains a future
  hardening pass.

## Intentional deferrals (see linked ADRs)
- **search-service's `rating` field** (Day 14, ADR-007): static per
  hotel, not yet wired to review-service's ResourceRatingUpdatedEvent
  (Day 16).
- **`PaymentMethod.WALLET`** (payment-service, Day 8): remains
  unimplemented after wallet-service (Day 18) — see ADR-010.
- **MFA** (identity-service, Day 6): domain model exists, no endpoint
  or live Twilio SMS integration.
- **Redeemed loyalty points have no destination** (loyalty-service,
  Day 19): RedeemPointsUseCase debits the ledger only; applying
  redeemed points to a wallet credit or a booking discount is future
  work, the same scope limit ADR-010 drew around wallet top-ups.

## Tier 3 complete ✅ (5 of 5)
user-service, review-service, messaging-service, wallet-service, and
loyalty-service all operational. Three services now auto-provision
reactively from identity.user-registered (user, wallet, loyalty).
loyalty-service closes a real Day-9 gap in notification-service:
NotificationType.LOYALTY_POINTS_EARNED and LOYALTY_TIER_UPGRADED had
existed as dead enum cases with no template and no producer until today.
