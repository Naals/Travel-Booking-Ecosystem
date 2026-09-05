# Travel Platform

Enterprise-grade distributed travel and booking ecosystem — a portfolio
project demonstrating production-level microservices architecture, DDD,
event-driven design, and cloud-native engineering practices.

## Architecture
- Microservices, DDD, Event-Driven Architecture, Hexagonal Architecture
- Java 21 / Spring Boot 3 / Spring Cloud
- Apache Kafka (Saga pattern, Outbox pattern, DLQ)
- PostgreSQL, MongoDB, Redis, Elasticsearch
- Docker, Kubernetes, Eureka, Spring Cloud Gateway
- Prometheus, Grafana, Zipkin
- GitHub Actions CI/CD

## Full service scope (21 modules)

Infrastructure (3):
| Module | Purpose |
|---|---|
| discovery-server | Eureka service registry |
| config-server | Centralized Spring Cloud Config |
| api-gateway | Routing, JWT auth, rate limiting, circuit breaking |

Core domain (18):
| Module | Purpose |
|---|---|
| identity-service | Registration, login, OAuth2, JWT, MFA, RBAC |
| user-service | Profile, preferences, travel history, saved locations |
| property-service | Listings, availability, pricing, amenities |
| hotel-service | Hotels, rooms, inventory, dynamic pricing |
| flight-service | Flights, routes, seat management, reservations |
| vehicle-service | Car inventory, booking, pricing |
| booking-service | Reservation workflow, lifecycle, saga orchestration |
| payment-service | Stripe/PayPal integration, refunds, payment tracking |
| wallet-service | User wallet, balance, transactions |
| review-service | Ratings, reviews, moderation |
| notification-service | Email, SMS, push notifications, templates |
| messaging-service | User-to-user chat, booking communication |
| search-service | Full-text search, filters, ranking (Elasticsearch) |
| recommendation-service | Personalized recommendations, trending destinations |
| loyalty-service | Reward points, membership tiers |
| fraud-service | Suspicious activity detection, rule engine |
| analytics-service | Business metrics, reports, aggregations |
| audit-service | Audit logs, compliance tracking |

Status of each module is tracked per-commit — see commit history and
`docs/adr` for the build order rationale (ADR-003).

## Modules

Status: 🚧 core services complete (21/21) — see [`docs/SERVICE_STATUS.md`](./docs/SERVICE_STATUS.md) for per-service detail and remaining hardening work.

| Module | Tier | Purpose |
|---|---|---|
| `shared-kernel` | — | DDD base classes: Entity, AggregateRoot, ValueObject, DomainEvent |
| `common-lib` | — | API response envelope, exception handling, Kafka topic registry |
| `discovery-server` | 0 | Eureka service registry |
| `config-server` | 0 | Centralized Spring Cloud Config |
| `api-gateway` | 0 | Routing, JWT auth, rate limiting, circuit breaking |
| `identity-service` | 1 | Registration, login, JWT, MFA (partial), RBAC |
| `booking-service` | 1 | Booking lifecycle and saga coordination |
| `payment-service` | 1 | Stripe integration, refunds |
| `notification-service` | 1 | Email/SMS/push, pluggable channel adapters |
| `property-service` | 2 | Property listings and availability |
| `hotel-service` | 2 | Hotels, rooms, inventory |
| `flight-service` | 2 | Flights, routes, seat inventory |
| `vehicle-service` | 2 | Rental fleet, one-way rentals |
| `search-service` | 2 | Federated search across all inventory types (Elasticsearch) |
| `user-service` | 3 | Profile, preferences, saved locations, travel history |
| `review-service` | 3 | Reviews, moderation, rating aggregation (MongoDB) |
| `messaging-service` | 3 | User-to-user chat, booking communication (MongoDB) |
| `wallet-service` | 3 | User wallet balance and transaction ledger |
| `loyalty-service` | 3 | Reward points and membership tiers |
| `recommendation-service` | 4 | Personalized and trending destinations |
| `fraud-service` | 4 | Rule-based risk detection, automated wallet freeze |
| `analytics-service` | 4 | Platform-wide booking funnel and revenue metrics |
| `audit-service` | 4 | Hash-chained, tamper-evident compliance trail |

See [`docs/adr`](./docs/adr) for the 15 architecture decision records tracing these choices across the build.

## Local development
\`\`\`
docker compose up -d
mvn clean install
\`\`\`
