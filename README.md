# SecureVault

A focused Spring Boot showcase of backend security and data-layer concepts —
built to demonstrate understanding, not to be a product. Every piece maps to
a real, deliberate engineering decision, documented below, not just "code
that happens to work."

## What's actually in here

- **JWT access tokens + opaque, rotating, revocable refresh tokens** — not
  one long-lived token. See [Why two tokens, not one](#why-two-tokens-not-one).
- **Escalating account lockout guarded by optimistic locking (`@Version`)**,
  not a naive counter — safe under concurrent brute-force bursts against the
  same account. See [The lost-update race this avoids](#the-lost-update-race-this-avoids).
- **A denormalized, materialized-view read model** (`user_summary_mv`) kept
  separate from the normalized write model (`app_user`), refreshed on a
  schedule. See [Why one table isn't enough](#why-one-table-isnt-enough).
- **Strategy + Decorator applied to notification sending** — pluggable
  channels, composable retry/logging, no framework magic, entirely explicit.
- **Builder pattern for a dynamic, optional-filter search** (`UserSearchCriteria`),
  composing a JPA `Specification` at runtime.
- **A correctly-avoided `@Transactional` self-invocation bug** — see
  `LockoutFailureRecorder`'s Javadoc for the trap it was deliberately split
  out to avoid.
- **A multi-stage Dockerfile** — JDK+Maven never ships in the runtime image.

## Why two tokens, not one

A single long-lived JWT can't be revoked without giving up statelessness
(no DB check per request). A single short-lived JWT with no refresh means
re-entering a password constantly. Splitting the two: the **access token**
is short-lived (15 min default), stateless, checked via signature only — if
stolen, its blast radius is bounded to a 15-minute window. The **refresh
token** is long-lived but opaque and tracked server-side (`refresh_token`
table), so it can genuinely be revoked — logout, admin action, or automatic
theft detection.

**Rotation + reuse detection**: every refresh exchange invalidates the old
token and issues a new one (`RefreshTokenService.rotate`). If an already-
rotated token is ever presented again, that's a strong signal of theft (an
attacker replaying a stolen, stale token) — the entire token family for that
user gets revoked immediately, not just the one token.

## The lost-update race this avoids

Without `@Version` on `LoginLockout`, two concurrent failed-login requests
for the same account could both read `failed_attempts = 4`, both compute
`5`, and both write `5` — one increment is silently lost, and the account
never actually reaches its lockout threshold despite genuinely receiving
enough failed attempts to trigger it. `@Version` makes the second writer's
`UPDATE ... WHERE version = X` match zero rows, throw
`OptimisticLockingFailureException`, and get retried
(`AccountLockoutService.recordFailureWithRetry`) against the now-current
row — no attempt is ever silently dropped.

## Why `LockoutFailureRecorder` is a separate bean

`AccountLockoutService.recordFailureWithRetry` needs to call a
`@Transactional` method multiple times across retries. If that
`@Transactional` method lived in the *same class* and were called via
`this.recordFailure(...)`, the call would bypass Spring's AOP proxy
entirely (self-invocation) — the method would silently run with **no
transaction at all**. Splitting it into `LockoutFailureRecorder`, a
genuinely separate Spring bean, means the call is routed through the DI
container and the proxy correctly applies `@Transactional` every time.

## Why one table isn't enough — `user_summary_mv`

The admin search screen wants: username, email, role, and current lockout
status, filterable by any combination of those. Querying that live, every
request, means joining `app_user` and `login_lockout` every single time —
fine at small scale, a real cost under real admin-dashboard traffic.
`user_summary_mv` pre-joins them into one flat, denormalized, indexed
materialized view, refreshed every 30 seconds
(`UserSummaryScheduler`) via `REFRESH MATERIALIZED VIEW CONCURRENTLY`
(the `CONCURRENTLY` keyword — and the unique index it requires, see
`V2__create_user_summary_mv.sql` — lets readers keep querying the *old*
version while the refresh runs, instead of blocking).

**The trade-off, made explicit**: the search screen can show data up to 30
seconds stale. This is a deliberate PACELC "Else" choice — trading a little
staleness for a query that never has to join two live tables — appropriate
here because "is this admin dashboard showing this second's exact state" is
not a real requirement; "is checkout/login fast and the write path
correct" is, and neither of those touches this view at all.

## Configuration & secrets

**Nothing in `application.yml` is a real secret** — the JWT signing key
default there is explicitly for local dev only and is overridden via the
`JWT_SECRET` environment variable in `docker-compose.yml` (which itself
refuses to start without one being set — `${JWT_SECRET:?...}`). This
mirrors a real finding from auditing a production `docker-compose.yml`
during this project's own prep: **never hardcode secrets directly in a
committed compose file** — inject them via environment variables backed by
a `.env` file (gitignored) or a real secrets manager.

`spring.jpa.hibernate.ddl-auto=validate` — schema changes are owned
entirely by Flyway migrations (`db/migration/`), never by Hibernate
auto-DDL. `open-in-view=false` — the Hibernate session doesn't stay open
for the whole HTTP request lifecycle (the common but discouraged default),
avoiding holding a DB connection longer than the actual transactional work
requires.

## Running locally

```bash
export JWT_SECRET=$(openssl rand -base64 48)
docker compose up -d
# API available at http://localhost:8080/api/auth/register etc.
```

## What's deliberately a stub, and why

`EmailNotificationSender` just logs — swapping in a real mail/SMS client
doesn't touch `NotificationConfig`'s decorator stack, `AuthService`, or
anything else, which is the entire point of the Strategy/Decorator split.
