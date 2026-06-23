# Java Point-of-Sale System

[![CI](https://github.com/MILTONADINA/POS/actions/workflows/ci.yml/badge.svg)](https://github.com/MILTONADINA/POS/actions/workflows/ci.yml)
[![Java 17](https://img.shields.io/badge/Java-17-orange)](https://adoptium.net/)
[![Build](https://img.shields.io/badge/build-Maven-blue)](https://maven.apache.org/)
[![License: MIT](https://img.shields.io/badge/license-MIT-green)](LICENSE)

A modular, desktop point-of-sale (POS) application written in Java 17. It models the core of a small
retail register: an item catalogue with dated regular and promotional prices, date-aware tax
categories, multi-tender sales (cash, credit, check), cashier authentication, and register sessions.
State is loaded from and saved to a CSV file through a repository abstraction, and the whole thing is
driven by a Swing GUI. The project is organized as a clean, layered architecture and backed by a
JUnit 5 test suite with JaCoCo coverage, making it a compact showcase of object-oriented design and
disciplined engineering practices rather than a production payment system.

> **Educational / demo project.** All bundled data is synthetic and the application must not be used
> to process real payment-card data. See [SECURITY.md](SECURITY.md).

## Highlights

- **Object-oriented domain model** — inheritance and polymorphism across payment types, `Comparable`
  and `equals` contracts honoured for collection correctness, composition over a `Store` aggregate.
- **Layered architecture with dependency injection** — the UI talks only to an application service;
  persistence sits behind a `StoreRepository` interface so the service is testable in isolation.
- **Secure credential handling** — passwords are stored as salted PBKDF2-HMAC-SHA256 hashes with
  constant-time verification; credit-card PANs are masked to the last four digits and never persisted.
- **Correct money math** — all monetary values use `BigDecimal` with explicit `HALF_UP` rounding to
  two decimal places; no floating-point currency arithmetic.
- **Resilient CSV persistence** — a fully symmetric, line-oriented format with round-trip tests;
  malformed lines are logged and skipped instead of crashing the load.
- **JUnit 5 + JaCoCo** — 50 unit/integration tests covering the domain and persistence layers, with a
  coverage report produced on every build.
- **Enforced quality gates** — every `mvn verify` runs Spotless (Google Java Format), SpotBugs static
  analysis (zero warnings), and a JaCoCo coverage gate, so formatting and bug-pattern regressions fail
  the build rather than slipping through review.
- **Continuous integration** — GitHub Actions runs the full gate on JDK 17 and a compile/test pass on
  JDK 21 for every push and pull request.

## Architecture

The application is split into three layers with a strict, one-directional dependency flow. The UI
depends on the service; the service depends on the domain model and on the repository *interface*; the
concrete CSV repository depends on the domain model. Nothing in the domain depends on the UI or on a
storage technology.

```
            ┌─────────────────────────────────────────────┐
            │  POSUI  (Swing GUI)                          │
            │  Start  →  POSFrame, panels, reports         │
            └───────────────────┬─────────────────────────┘
                                │ calls
                                ▼
            ┌─────────────────────────────────────────────┐
            │  StoreService  (application service, POSPD)  │
            │  single entry point for state changes +      │
            │  persistence; login / sales / catalogue ops  │
            └─────────┬───────────────────────┬────────────┘
                      │ operates on            │ depends on (DI)
                      ▼                        ▼
       ┌──────────────────────────┐   ┌──────────────────────────────┐
       │  Domain model  (POSPD)   │   │  StoreRepository  (POSDM)     │
       │  Item, Price, PromoPrice │   │  interface                    │
       │  TaxCategory, Sale,      │   │     ▲ implemented by          │
       │  Payment/Cash/Credit/    │   │  CsvStoreRepository           │
       │  Check, Cashier, Session │   │  (classpath seed + data file) │
       └──────────────────────────┘   └──────────────────────────────┘
```

- **`POSUI` (presentation).** Swing frames, panels, and reports. `POSUI.Start` is the `main` class; it
  wires the default CSV repository into a `StoreService` and launches `POSFrame`.
- **`POSPD` (domain + application service).** The business model — items, dated prices and promotions,
  tax categories, sales, polymorphic payments, cashiers, and sessions — plus `StoreService`, the
  single orchestration point for every state change and the only collaborator that touches persistence.
- **`POSDM` (persistence).** The `StoreRepository` interface and its `CsvStoreRepository`
  implementation. The concrete store is injected into `StoreService`, so the service is decoupled from
  storage and can run against an in-memory fake in tests.

## Build & Run

Requires **JDK 17 or newer** and **Maven**.

```bash
# Compile, run the full test suite, generate the coverage report, and build a runnable jar
mvn clean verify
```

This produces:

- a self-contained runnable jar at `target/point-of-sale-1.0.0.jar`, and
- a JaCoCo coverage report at `target/site/jacoco/index.html`.

Run the GUI either way:

```bash
# From the build artifact
java -jar target/point-of-sale-1.0.0.jar

# Or directly from source via Maven
mvn exec:java
```

On first run the application loads the bundled seed catalogue from the classpath. Runtime state is
written to a configurable location — the `pos.data.file` system property, defaulting to
`~/.pos/StoreData_v2024FA.csv` — and **never** to the source tree:

```bash
java -Dpos.data.file=/path/to/store.csv -jar target/point-of-sale-1.0.0.jar
```

## Demo credentials

At the login screen:

1. Pick a **register** (`1` or `2`).
2. Pick a **cashier** — **David** (`#1`) or **Sally** (`#2`).
3. Enter a **starting cash amount** for the drawer.
4. Enter the password: **`demo1234`**.

Both seeded cashiers use the same demo password. These credentials exist purely to exercise the demo.

## Testing & coverage

```bash
mvn test       # run the unit/integration tests
mvn verify     # run tests and produce the JaCoCo report (target/site/jacoco/index.html)
```

The suite is 50 tests built on **JUnit 5 (Jupiter)** and covers, among other things:

- sale money math — subtotal, tax, total, change, payment sufficiency, and over-tender capping;
- date-based price selection, including promotional-window expiry;
- tax-rate selection by date, including the effective-date boundary;
- the `Comparable`/`equals` contracts that keep distinct prices and tax rates in their sorted sets;
- payment polymorphism and PAN masking;
- PBKDF2 password hashing and verification;
- the service-layer login flow against an in-memory repository (no disk I/O); and
- a lossless CSV persistence round-trip, including an assertion that the raw PAN never reaches disk.

Coverage focuses on the testable layers: **POSDM ~87% line** and **POSPD ~60% line**. The Swing UI
(`POSUI`) is exercised manually and is intentionally outside the unit-test scope.

## Project structure

```
POS/
├── pom.xml                       # Maven build: Java 17, JUnit 5, JaCoCo, Spotless, SpotBugs, Shade
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── POSPD/            # Domain model + StoreService (application service)
│   │   │   ├── POSDM/            # StoreRepository + CsvStoreRepository (persistence)
│   │   │   └── POSUI/            # Swing GUI; POSUI.Start is the main class
│   │   └── resources/
│   │       └── StoreData_v2024FA.csv   # Bundled synthetic seed data
│   └── test/
│       └── java/
│           ├── POSPD/           # Domain + service tests
│           └── POSDM/           # Persistence round-trip + in-memory repository fake
├── README.md
├── SECURITY.md
├── CONTRIBUTING.md
├── CHANGELOG.md
├── NOTICE
└── LICENSE
```

## Security

Passwords are salted PBKDF2-HMAC-SHA256 hashes verified in constant time, and credit-card PANs are
masked to the last four digits and never written to disk. The bundled data is entirely synthetic, and
this project is an educational demo that must not handle real cardholder data. Full details — and how
to report a vulnerability — are in [SECURITY.md](SECURITY.md).

## License

Released under the [MIT License](LICENSE). © 2026 Milton Adina Shisia.
Third-party attributions are listed in [NOTICE](NOTICE).
