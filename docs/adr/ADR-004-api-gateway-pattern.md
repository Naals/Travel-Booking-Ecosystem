# ADR-004: API Gateway as Single Entry Point

## Status
Accepted

## Context
With 21 microservices, clients cannot be expected to know the address
of each service, handle service discovery, validate JWTs independently,
or manage per-service rate limiting. Cross-cutting concerns duplicated
across 21 services is a maintenance anti-pattern.

## Decision
Use Spring Cloud Gateway as the single entry point for all external
traffic. The gateway owns:
- JWT validation (all services trust X-User-Id header instead)
- Rate limiting (Redis token bucket, keyed per userId)
- Circuit breaking (Resilience4j, per downstream service)
- Route resolution (Eureka lb:// URIs, no hardcoded hosts)
- Distributed tracing header propagation
- Aggregated Swagger UI (all service OpenAPI specs in one place)

## Consequences
Easier: JWT validation logic exists in one place; adding a new
cross-cutting concern (IP allowlisting, API versioning) is a single
filter change; services are not directly reachable from the internet.
Harder: gateway is a potential single point of failure — mitigated
by running 2 replicas in Kubernetes with a LoadBalancer service in
front; gateway adds one network hop to every request.

## Alternatives Considered
- Each service validates its own JWT — rejected, duplicates security
  logic across 21 codebases and means a key rotation touches 21 deploys.
- Service mesh (Istio) for cross-cutting concerns — rejected, significant
  operational overhead for a portfolio project; revisit for production.
