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

## Local development
\`\`\`
docker compose up -d
mvn clean install
\`\`\`
