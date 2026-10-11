## Hardening phase (Day 24+)

| Day | Focus | Outcome |
|---|---|---|
| 24 | Integration testing, booking saga | Found and fixed a real bug: `PaymentInitiatedEvent` silently dropped since Day 8, no listener wired since Day 7, out-of-order failures swallowed instead of retried. Fixed via ADR-016. Added the platform's first Kafka retry + DLQ config (`booking-service` only so far) and `test-support`. |
| 25 | Kubernetes manifests, Tier 2-4 | Audited Tier 0-1's existing raw manifests before extending the pattern and found five real gaps present since Day 4: missing `config-server` and `notification-service` Deployments; an undefined `platform-secrets` Secret; a missing `CONFIG_HOST` ConfigMap key (config-server was never actually reachable in-cluster); a missing `fraud-service` gateway route. Fixed all five, then added a parameterized Helm chart for the 14 Tier 2-4 services and extended CI/CD's build/test/push/deploy matrices, which had silently covered only Tier 0-1 since Day 1. See ADR-017. |

## Remaining work to close out the full portfolio

- **Integration testing** — `booking-service`'s saga has real coverage
  (Day 24). `property-service`, `hotel-service`, `flight-service`,
  `vehicle-service`, `payment-service`, and every Inbox/dedup consumer
  (`review-service`, `loyalty-service`, `analytics-service`,
  `audit-service`) still have none.
- **Kafka retry/DLQ coverage** — `KafkaErrorHandlingConfig` exists
  only in `booking-service`. Every other service still relies on
  "don't acknowledge on failure" alone.
- **Kubernetes manifests** — Day 25: closed for now. Tier 0-1's five
  gaps are fixed; Tier 2-4 is covered by the new Helm chart. Still
  open: `infrastructure/monitoring/` (Prometheus config, Grafana
  dashboards) remains unpopulated even though every Deployment already
  carries `prometheus.io/scrape` annotations pointing at nothing yet
  collecting them; Tier 0-1 and Tier 2-4 use two different deployment
  mechanisms (raw manifests vs. Helm), not yet unified.
- **CI/CD pipeline** — Day 25: build/test/push/deploy now cover all 21
  services, and Tier 2-4 deploys via the new Helm chart. Still open:
  the pipeline has never actually been run against a real cluster —
  it is more complete on paper, not yet verified in practice, the same
  category of risk integration testing addressed for the saga.
- **README / documentation layer**, **C4 diagrams** — unchanged, still open.
