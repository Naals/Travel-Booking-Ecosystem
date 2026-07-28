# ADR-006: Inventory Service Patterns Across Tier 2

## Status
Accepted

## Context
Four inventory services (property, hotel, flight, vehicle) all participate
in the same booking saga but have different domain models:
- Property: date-range availability, one resource per listing
- Hotel: date-range availability, multiple rooms per hotel by type
- Flight: fixed datetime, seats by class, no date range
- Vehicle: date-range rental, one vehicle per aggregate, search by category

All four must respond to the same saga events (BookingCreated,
PaymentFailed, BookingConfirmed) and publish to the same inventory
topics (inventory.reservation-confirmed, inventory.reservation-failed,
inventory.reservation-released).

## Decision
Each inventory service is an independent bounded context with its own
aggregate design suited to its domain:

Property  → Property aggregate with Reservation collection (date-range overlap)
Hotel     → Hotel aggregate with Room entities, each with reservations
Flight    → Flight aggregate with Seat entities, point-in-time (no date range)
Vehicle   → Vehicle aggregate (one car = one aggregate), FleetQueryService
crosses aggregates to find first available

All four follow the same saga event contract:
- Consume: booking.booking-created (filter on bookingType)
- Publish on success: inventory.reservation-confirmed
- Publish on failure: inventory.reservation-failed
- Consume: payment.payment-failed → release → inventory.reservation-released
- Consume: booking.booking-confirmed → make hold permanent

resourceId encoding per type:
- PROPERTY:  "<propertyId>"
- HOTEL:     "<hotelId>:<roomType>"  e.g. "uuid:SUITE"
- FLIGHT:    "<flightId>:<seatClass>" e.g. "uuid:BUSINESS"
- VEHICLE:   "<locationCode>:<category>" e.g. "IST:SUV"

## Consequences
Easier: booking-service knows nothing about how each inventory type
works — it only knows saga event topics. Adding a new bookable type
(e.g. experience-service) follows the same contract.
Harder: resourceId encoding is a convention, not enforced by a type
system — must be documented and tested at the saga integration level.

## Alternatives Considered
- Single inventory-service owning all types — rejected, couples
  very different domains and makes independent scaling impossible.
- Shared inventory port/interface in common-lib — rejected, the
  differences between date-range and point-in-time availability
  mean a shared interface would be a leaky abstraction.
