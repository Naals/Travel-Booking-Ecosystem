# ADR-017: Kubernetes Audit of Tier 0-1, and a Helm Chart for Tier 2-4

## Status
Accepted

## Context
Day 23's follow-up list named "Kubernetes manifests for Tier 2-4" as
open work, on the stated premise that Tier 0-1 already had this
solved (Day 4-9 era raw manifests). Reading those manifests closely
before extending the pattern to 14 more services — rather than
copy-pasting it 14 times on faith — surfaced five real, verifiable
gaps that had been present since Day 4:

1. **`config-server` has no Deployment.** It has a POM, an
   application class, and has been in CI's docker-build-push matrix
   since Day 1 — but no `Deployment` manifest was ever written for it.
2. **`notification-service` has a Service but no Deployment.** Its
   Service entry was written Day 4, speculatively, before the service
   existed (built Day 9). Once built, the matching Deployment was
   never added — the Service has been selecting zero Pods this whole time.
3. **`platform-secrets` is referenced but never defined.** Four Day-4
   Deployments do `secretRef: name: platform-secrets`; no file in the
   repository ever specifies what that Secret contains.
4. **`CONFIG_HOST` is missing from `platform-config`.** Every
   service's `spring.config.import` uses `optional:configserver:...
   ${CONFIG_HOST:localhost}...`. With no `CONFIG_HOST` key in the
   shared ConfigMap, that placeholder always resolved to `localhost`
   *inside each pod* — never the real `config-server` Service.
   Because the import is `optional:`, this failed silently.
   `config-server` has never actually been reachable, by name, from
   any service running in this cluster.
5. **`fraud-service` has no gateway route.** Built Day 21, three
   weeks after the route table was last edited (Day 5); reachable only
   via Spring Cloud Gateway's auto-locator fallback path, not the
   platform's normal `/api/v1/<domain>/**` convention.

None of these were caught earlier for the same reason the Day 24 saga
bug wasn't: nothing had exercised the actual deployment path
end-to-end. This is the same lesson in a different layer of the
system — see ADR-016.

Separately: writing 14 more raw Deployment + Service manifest pairs
for Tier 2-4, each ~95% identical to the last, would triple the size
of `infrastructure/kubernetes/` for content that varies in exactly
three dimensions per service (name, port, datastore).

## Decision

**Fix the five gaps directly, in the existing raw-manifest style** —
this is a correctness fix to what's already there, not a redesign.
`config-server` and `notification-service` get Deployments matching
the Day-4 pattern exactly (`envFrom` referencing the same ConfigMap
and Secret, the same annotation/probe/resource shape). `platform-config`
gains `CONFIG_HOST`. A `platform-secrets.example.yml` template
enumerates every key the four existing `secretRef`s (and the 14 new
ones from the chart) actually need — the real `platform-secrets.yml`
stays out of git, already covered by `.gitignore`'s `**/*-secrets.yml`
rule from Day 1 without any change needed there. `fraud-service` gets
an explicit route with a CircuitBreaker, matching every other
built-and-real service's treatment rather than its Tier 4 siblings'
original Day-5 bare stubs (which were written before those services
existed — a different, acceptable reason for being minimal).

**Introduce a Helm chart for Tier 2-4, not 14 more raw manifest
pairs.** One chart (`infrastructure/kubernetes/helm/travel-service`)
templated on name, port, and datastore type; 14 short `values/<service>.yaml`
files (5-12 lines each) supply what's actually service-specific. The
chart reuses every established Tier 0-1 convention exactly — the same
`travel-platform` namespace (via `--namespace` at install time, not
hardcoded in templates — a small correctness improvement over the raw
manifests, which do hardcode it; not retrofitted onto Tier 0-1 today,
noted as real follow-up), the same `platform-config`/`platform-secrets`
envFrom pattern, the same `/actuator/health/readiness` /
`/actuator/health/liveness` probe paths and timing Day 4 established,
the same `ghcr.io/travel-platform/<service>:latest` image convention.
Deliberately NOT a redesign of the deployment model — a DRY
implementation of the identical model, chosen only because 14 near-
duplicate files would be strictly worse than one parameterized one.

**Extend CI/CD's matrices while already in that file.** A Helm chart
for Tier 2-4 is inert if CI never builds or pushes their images —
`build-and-test` and `docker-build-push` had silently covered only
Tier 0-1's 7 services since Day 1 (every Tier 2-4 service has shipped
a working Dockerfile since its own build day and none had ever
actually been built by CI). The `deploy` job's raw `kubectl apply`
steps stay for Tier 0-1; a new loop of `helm upgrade --install` calls,
one per Tier 2-4 service, handles the rest — each pinned to
`--set image.tag="${{ github.sha }}"` so the deploy step always
installs the exact image `docker-build-push` just built, not
whatever `latest` happens to currently point at.

## Consequences
Easier: Tier 2-4 gets full Kubernetes coverage in 14 short files
instead of 14 near-duplicate ~90-line manifest pairs; adding a 22nd
service later is one new `values/<service>.yaml`, not copy-pasting a
Deployment and hoping nothing drifts; CI now actually verifies every
one of the platform's 21 services builds and its image can be pushed.

Harder: two deployment mechanisms now coexist in one repo (raw
manifests for Tier 0-1, Helm for Tier 2-4) — a real, acknowledged
inconsistency, not resolved today. Migrating Tier 0-1 onto the same
chart is genuine, valuable follow-up work, deliberately deferred
rather than rewritten under time pressure in the same day as fixing
five live bugs in it. The expanded CI/CD pipeline has never actually
been run against a real cluster — it's now more complete on paper,
but unverified in practice, which is exactly the category of risk
integration testing (ADR-016) addressed for the saga and this pipeline
has not yet had the same treatment.

## Alternatives Considered
- Kustomize (base + per-service overlays) instead of Helm — a
  reasonable alternative with a similar DRY outcome; Helm chosen for
  its more familiar templating (Go templates + values files) and
  because the `helm upgrade --install --wait` semantics fold rollout
  waiting into the deploy loop itself, which Kustomize's plain
  `kubectl apply` does not provide without a separate step.
- Migrate Tier 0-1 onto the new chart today — rejected as scope creep
  onto an already-large day; the five correctness fixes to the
  existing manifests are the right amount of surgery on Tier 0-1 for
  now, not a full rewrite.
