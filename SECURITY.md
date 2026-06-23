# Security Policy

## Scope and intent

This is an **educational, demonstration project**. It illustrates secure-by-default coding patterns in
a small Java point-of-sale application; it is **not** a production payment system. It is **not**
PCI-DSS compliant and **must not be used to store, process, or transmit real payment-card data,
real personal data, or any other sensitive information.**

All bundled seed data in `src/main/resources/StoreData_v2024FA.csv` is **synthetic**: the social
security numbers are non-issuable placeholders (e.g. `000-00-0001`), the phone numbers use the
fictional `555` exchange, and the single credit-card record is stored only in masked form. No real
account, person, or card is represented.

## Security posture

The project deliberately demonstrates several defensive practices in its domain and persistence layers
(`POSPD`, `POSDM`):

### Credential handling
- Cashier passwords are stored as **salted PBKDF2-HMAC-SHA256** hashes (`POSPD.PasswordHasher`), using
  a per-credential random salt from `SecureRandom` and **600,000 iterations**.
- Hashes are written in a self-describing, Django-style PHC format
  (`pbkdf2_sha256$<iterations>$<base64-salt>$<base64-hash>`), so the work factor can be raised later
  and outdated hashes can be detected and upgraded (`needsRehash`) on the next successful login.
- Verification uses a **constant-time comparison** (`MessageDigest.isEqual`) to avoid leaking
  information through timing, and null/empty candidates and malformed stored hashes are rejected.
- Plaintext passwords are never stored. On load, an already-hashed credential is stored verbatim
  rather than re-hashed.

### Payment-card data
- Credit-card PANs are **masked to the last four digits** for both display and storage
  (`POSPD.Credit`). The full PAN is **never persisted** — the CSV writer emits only the masked form,
  and a regression test asserts the raw PAN never reaches disk.

### Resilient, fail-loud persistence
- The CSV parser validates the expected field count for each record and **logs and skips a malformed
  line** instead of aborting the entire load.
- Read/write failures surface as a `POSDM.StorePersistenceException` rather than being silently
  swallowed, so callers can react (for example, warn that a sale was not saved).
- Runtime data is written to a configurable path (`pos.data.file`, defaulting to
  `~/.pos/StoreData_v2024FA.csv`) and never to the source tree; the bundled seed is read from the
  classpath only as a read-only fallback. Application logs roll to `~/.pos/logs/pos-N.log`, so
  failures and warnings persist even when the jar is launched without a console.
- Writes are **atomic** (write to a temp file, then atomic rename), so a crash or full disk mid-save
  can never truncate the live data file; a file that is present but unparseable is **rejected**
  rather than silently loaded as empty (which a later save would have overwritten).
- Free-text fields are neutralized against **CSV/spreadsheet formula injection** (CWE-1236) on write
  — a value beginning with `=`, `+`, `-`, `@`, or a tab is sentinel-escaped — and the sentinel is
  stripped on read so values round-trip unchanged.

### Known, intentional limitations
- Credit-card "authorization" is a **simulation** for demo purposes, not a real payment-gateway
  integration.
- The CSV store is plaintext on disk (apart from hashed passwords and masked PANs) and is not
  encrypted; it is suitable for demo data only.
- The Swing UI is not hardened for multi-user or networked deployment.

These limitations are acceptable for a demonstration project and are documented here so they are not
mistaken for production guarantees.

## Security testing & tooling

Security is verified empirically, not assumed. The following run in CI (`.github/workflows/security.yml`)
on every push and pull request, and weekly on a schedule; they can all be run locally too:

| Layer | Tool | What it checks |
|-------|------|----------------|
| SAST (Java) | **CodeQL** (`security-extended`) + **find-sec-bugs** (SpotBugs plugin, in `mvn verify`) | injection, crypto misuse, path traversal, unsafe deserialization, weak randomness |
| SAST (rules) | **Semgrep** (`p/java`, `p/security-audit`, `p/secrets`) | OWASP-style code patterns |
| Dependencies (SCA) | **Trivy** (enforced) + **OWASP Dependency-Check** (advisory) | known CVEs in third-party libraries |
| Secrets | **gitleaks** | committed credentials/keys |
| Supply chain | **Dependabot** | outdated/vulnerable dependencies and actions |

The Trivy gate fails the build on HIGH/CRITICAL CVEs that have an available fix (`ignore-unfixed`):
vulnerabilities with no upstream patch are surfaced in the report but do not block the build, so they
cannot be silently "fixed" by a version bump that does not exist.

Local quick-run:

```bash
gitleaks detect --no-git --source .
trivy fs --scanners vuln,secret,misconfig --skip-dirs target .
semgrep scan --config p/java --config p/security-audit --config p/secrets src/
mvn verify        # includes SpotBugs + find-sec-bugs as a build gate
```

The latest pass across all of the above was **zero findings**.

### Why no DAST (e.g. OWASP ZAP)?

ZAP and similar dynamic scanners are **web-application** proxies: they exercise an HTTP attack surface
(endpoints, headers, cookies, parameters). This project is a **self-contained Java Swing desktop
application with no network listener or HTTP endpoint**, so there is nothing for a DAST proxy to scan —
running one would produce no meaningful signal. The equivalent assurance for this architecture comes
from the SAST + SCA + secret-scanning toolchain above. If a web or service layer were added, ZAP (and
API fuzzing) would be added to the pipeline at that point.

## Reporting a vulnerability

If you discover a security issue in this project, please report it privately rather than opening a
public issue:

- Open a **private security advisory** via the repository's GitHub "Security" tab
  (Security → Report a vulnerability), or
- Email the maintainer at **miltonshisia@gmail.com** with a clear description, reproduction steps, and
  the affected version or commit.

Please allow a reasonable period for the issue to be reviewed and addressed before any public
disclosure. As an educational project there is no formal SLA, but reports are genuinely appreciated and
will be acknowledged.
