# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-06-23

A consolidation and hardening pass that turned the original prototype into a coherent, tested,
layered application.

### Security
- Replaced plaintext password handling with **salted PBKDF2-HMAC-SHA256** hashing (600,000 iterations)
  and **constant-time** verification (`POSPD.PasswordHasher`), using a self-describing PHC hash format
  with rehash detection.
- **Masked credit-card PANs** to the last four digits for display and storage; the full PAN is never
  written to disk.
- Masked the **check bank-account number** at rest as well, so no full financial account identifier is
  ever persisted.
- Confirmed all bundled seed data is **synthetic** (non-issuable SSNs, fictional `555` phone numbers,
  a masked demo card and check account).
- Added an **empirically-verified security pipeline** (`.github/workflows/security.yml`): CodeQL,
  Semgrep, Trivy, gitleaks, advisory OWASP Dependency-Check, plus **find-sec-bugs** in the `mvn verify`
  gate and **Dependabot** — currently a zero-finding pass.

### Fixed / correctness
- Corrected **date-based price selection** so promotional prices apply only within their window and
  expire afterward, with the regular price resuming.
- Corrected **tax-rate selection** to be inclusive of a rate's effective date and to fall back to zero
  before any rate applies.
- Switched all monetary calculations to **`BigDecimal` with explicit `HALF_UP` rounding** to two
  decimal places.
- Honoured the `Comparable`/`equals` contracts so distinct prices and tax rates are no longer dropped
  from their sorted sets.
- Made CSV persistence **lossless and symmetric** (multi-UPC items, promo prices, session timestamps,
  tax-free flag, and payment subtypes all round-trip); malformed lines are now logged and skipped, and
  persistence failures surface as `StorePersistenceException` instead of being swallowed.

### Architecture
- Introduced a **`StoreRepository` abstraction** with a `CsvStoreRepository` implementation, injected
  into a new **`StoreService`** application service that is the single entry point for state changes and
  persistence — decoupling the UI from storage and making the service unit-testable.
- Separated runtime data (configurable `pos.data.file`, defaulting to `~/.pos/`) from the read-only
  bundled classpath seed; the source tree is never written to.

### Build
- Adopted **Maven** with the LGoodDatePicker dependency resolved from **Maven Central** (no vendored
  jar), Java 17 compilation, **JaCoCo** coverage reporting + gate, and a **Maven Shade** runnable jar.
- Enforced **Spotless** (Google Java Format) and **SpotBugs + find-sec-bugs** as build gates.
- Added **GitHub Actions** CI (JDK 17 verify + JDK 21 compatibility) and a dedicated security workflow,
  both running on every push and pull request.

### Testing
- Added a real **JUnit 5 (Jupiter)** suite covering sale money math, price/promo selection,
  tax-rate selection, collection contracts, payment polymorphism and PAN masking, PBKDF2 hashing, the
  service-layer login flow against an in-memory repository, and a lossless persistence round-trip.

### Documentation
- Rewrote the README and added `SECURITY.md`, `CONTRIBUTING.md`, this changelog, and a `NOTICE` file
  with third-party attribution.

[1.0.0]: https://github.com/MILTONADINA/POS/releases/tag/v1.0.0
