# ADR-001: Adopt Microservices Architecture

## Status
Accepted

## Context
The platform spans property rentals, hotels, flights, vehicle rentals,
payments, reviews, and messaging — domains with very different scaling
and ownership characteristics. A monolith would couple unrelated domains
and prevent independent scaling.

## Decision
One service per bounded context (21 total — see README), each with its
own database, deployable independently. REST for synchronous queries,
Kafka for state-changing workflows.

## Consequences
Easier: independent deploys, independent scaling, failure isolation.
Harder: distributed transactions need sagas instead of local ACID;
operational complexity increases (service discovery, tracing) — at 21
services this is significant and must be staged deliberately (see ADR-003).

## Alternatives Considered
- Modular monolith — lower overhead, but doesn't demonstrate the
  distributed-systems skills this project targets.
- Serverless per endpoint — rejected due to cold-start latency concerns.
