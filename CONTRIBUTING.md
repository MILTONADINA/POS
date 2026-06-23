# Contributing

Thanks for your interest in the Java Point-of-Sale project. This is a portfolio/learning project, but
contributions and suggestions are welcome. The guidelines below keep the codebase consistent and the
build green.

## Prerequisites

- **JDK 17 or newer**
- **Maven**

## Build and test

```bash
mvn clean verify
```

This compiles the project, runs the full JUnit 5 test suite, generates the JaCoCo coverage report at
`target/site/jacoco/index.html`, and builds the runnable jar at `target/point-of-sale-1.0.0.jar`.

During development you can run the faster feedback loops:

```bash
mvn test          # compile and run the tests
mvn exec:java     # launch the Swing GUI from source
```

## Code style

The project uses **Google Java Format** (AOSP / four-space variant), enforced by the Spotless Maven
plugin. Its `check` goal runs on every `mvn verify`, so a misformatted change fails the build. Format
your changes before committing:

```bash
mvn spotless:apply     # auto-format all sources
mvn spotless:check     # verify formatting without modifying files (runs in verify/CI)
```

General conventions:

- Four-space indentation, no tabs; UTF-8 source encoding.
- Keep the existing package boundaries intact: `POSPD` (domain + application service), `POSDM`
  (persistence), `POSUI` (Swing UI). The UI must not be referenced by the domain or persistence layers.
- Route all state changes and persistence through `StoreService`; do not have the UI talk to a
  repository directly.
- Use `BigDecimal` (never `double`/`float`) for money, with explicit rounding.
- Public types and methods should carry Javadoc consistent with the existing style.

## Static analysis

`mvn verify` already runs the full quality gate, so a single command covers it before opening a pull
request:

```bash
mvn clean verify
```

This enforces, and fails on:

- **SpotBugs** (effort `Max`, threshold `Medium`, `includeTests=false`) — intentional domain/Swing
  patterns are filtered in `spotbugs-exclude.xml`; any other finding must be resolved (not excluded).
- **Spotless** formatting (`spotless:check`).
- **JaCoCo** coverage gate on the `POSPD`/`POSDM` layers (line ≥ 60%, branch ≥ 50%).
- the full JUnit 5 test suite.

If further tooling (e.g. Checkstyle or Error Prone) is added later, wire it into `verify` the same way
and resolve findings before submitting.

## Branching and pull requests

- Branch off `main` using a descriptive name, e.g. `feature/promo-stacking` or `fix/tax-boundary`.
- Keep commits focused and write clear, imperative commit messages
  (e.g. *"Fix promo-price expiry off-by-one"*).
- Open a pull request against `main` with a short description of **what** changed and **why**, and link
  any related issue.
- Keep changes scoped: avoid mixing unrelated refactors, formatting-only churn, and feature work in one
  PR.

## Definition of done

Before requesting review, make sure that:

- `mvn clean verify` passes locally and in CI (GitHub Actions runs `mvn -B verify` on every push and
  pull request).
- New or changed behaviour is covered by tests, and **overall coverage does not drop**.
- Code is formatted (`mvn spotless:apply`) and free of new compiler/static-analysis warnings.
- No secrets, real personal data, or real payment-card data are added — all sample data must remain
  synthetic (see [SECURITY.md](SECURITY.md)).
