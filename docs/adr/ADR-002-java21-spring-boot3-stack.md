# ADR-002: Java 21 + Spring Boot 3 as the Platform Stack

## Status
Accepted

## Context
Need a stack that's production-proven for microservices, with first-class
service discovery, config, and circuit breaking support, replicated
consistently across 21 modules.

## Decision
Java 21 LTS, Spring Boot 3.2, Spring Cloud 2023.0.0 across all services.

## Consequences
Easier: full Spring Cloud ecosystem; records eliminate DTO boilerplate;
identical tooling across all 21 modules keeps the parent POM's dependency
management meaningful.
Harder: newer than Java 17 LTS, so some libraries may lag — mitigated by
sticking to mainstream, actively maintained dependencies.

## Alternatives Considered
- Java 17 LTS — safer, but no virtual threads/pattern matching benefits.
- Kotlin — concise, but project goal is demonstrating Java/Spring depth.
