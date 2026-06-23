# Security Assessment — Point of Sale

**Target:** `MILTONADINA/POS` (standalone Java 17 / Swing desktop application)
**Commit assessed:** `b04ef54` (`main`)
**Type:** Authorized white-box assessment of the application the author owns.
**Method:** Attack-surface enumeration → automated scanning (SAST / SCA / secrets / IaC) →
dependency & credential review → exploitation attempt. No third-party systems were touched.

> **Why this report looks the way it does.** A point-of-sale tool evokes a network/web pentest,
> but this application is a **self-contained desktop binary** with no network listener, web
> endpoint, database, container, or Active Directory. The professional result is therefore not a
> list of exploits — it is an *evidenced demonstration that the remote attack surface is nil*, plus
> the static/supply-chain analysis that does apply. Knowing which tools fit a target (and which
> would be theater) is the assessment.

---

## Executive summary

| Dimension | Result |
|---|---|
| Remote/network attack surface | **None** — no socket, server, HTTP, DB, RMI, or JMX code exists (source-verified) |
| SAST (Semgrep `p/java` + `p/security-audit` + `p/secrets`, 124 rules) | **0 findings** |
| SAST (SpotBugs + find-sec-bugs, `mvn verify` gate) | **0 bugs** |
| SAST (GitHub CodeQL `security-extended`, CI) | **0 alerts** |
| SCA / dependency CVEs (Trivy, HIGH/CRITICAL) | **0** (only runtime dep: LGoodDatePicker) |
| Secrets (gitleaks) | **0 leaks** |
| IaC / container misconfig (Trivy) | **N/A** — no Dockerfile / IaC present |
| Credential storage (offline-cracking resistance) | **Strong** — salted PBKDF2-HMAC-SHA256, 600,000 iterations |

**No exploitable findings.** The assessment is reproducible — every applicable scanner runs in CI
on every push and is green.

---

## 1. Reconnaissance & attack-surface enumeration

The target ships as one runnable jar launched locally (`java -jar`). It reads/writes a single CSV
under `~/.pos/` and renders a Swing GUI. To confirm the *remote* surface empirically, the entire
`src/main/java` tree was searched for any networking, server, or data-source primitive:

```
ServerSocket | Socket | DatagramSocket | HttpURLConnection | com.sun.net.httpserver |
jakarta.servlet | HttpServer | .bind( | .accept( | InetSocketAddress | URLConnection |
openConnection | RMI | JMX | JDBC | DriverManager      → 0 matches
```

**Conclusion:** the application cannot open a listening port or originate a network/database
connection — there is no code path that does so. Its only trust boundaries are **local**: the
filesystem (data file + rolling log under `~/.pos/`) and the on-screen login. This single fact
determines the disposition of every remote-facing tool below.

---

## 2. Tooling disposition

Every tool from the requested arsenal, with its disposition against *this* target and the reason.
`APPLIED` = run, with result. `N/A` = no surface to target (empirical reason). `DEPLOYMENT-LEVEL` =
belongs to the operational environment, not the application artifact. `EXCLUDED` = withheld on
scope/ethics grounds.

### Recon & attack surface
| Tool | Disposition | Reason / result |
|---|---|---|
| **Nmap** | N/A | App opens no listening socket (§1); there is no port/service to scan. Network footprint = nil. |
| **Amass** | N/A | No app-owned domains/DNS; a desktop binary has no external DNS footprint to map. |
| **theHarvester** | N/A | No public org/domain footprint to OSINT-enumerate. |
| **httpx** | N/A | No HTTP services to probe. |

### Vulnerability scanning
| Tool | Disposition | Reason / result |
|---|---|---|
| **Trivy** | **APPLIED** | `fs` (vuln+secret+misconfig) → **0 HIGH/CRITICAL**, "Clean". SCA over dependencies → no known CVEs. Enforced in CI. |
| **Nuclei** | N/A | Template engine targets live HTTP/network endpoints; none exist. |
| **OpenVAS / Greenbone** | N/A | Network vulnerability scanner; the app exposes no network service. |
| **Nikto** | N/A | Web-server scanner; there is no web server. |

### Web application
| Tool | Disposition | Reason / result |
|---|---|---|
| **OWASP ZAP** | N/A | No HTTP attack surface (no endpoints, headers, cookies, params). |
| **sqlmap** | N/A | No SQL anywhere — persistence is a validated CSV; no JDBC/`DriverManager` in source. SQLi is not reachable. |
| **ffuf / gobuster / WPScan** | N/A | No web routes, vhosts, or WordPress to fuzz/enumerate. |

### Exploitation
| Tool | Disposition | Reason / result |
|---|---|---|
| **searchsploit / Exploit-DB** | **APPLIED (advisory)** | Checked the runtime dependency (LGoodDatePicker) and Java 17 baseline — no applicable public exploit/PoC. |
| **Metasploit** | N/A | No remote service, listener, or RCE surface to deliver a payload against. |

### Credentials
| Tool | Disposition | Reason / result |
|---|---|---|
| **Hashcat / John the Ripper** | **ASSESSED (not executed)** | The only credential material is the stored password hashes. Format = `pbkdf2_sha256$600000$…` — **salted PBKDF2-HMAC-SHA256 at 600,000 iterations**, per-credential random salt, constant-time verify. Offline cracking of a real password at this work factor is computationally infeasible and rainbow tables are defeated by the salt. (The seeded hash is for the deliberately-public demo password `demo1234`; cracking a known demo value proves nothing.) **Verdict: resistant to exactly the offline attack these tools perform.** |
| **Hydra** | N/A | Brute-forces *network* login services (SSH/RDP/HTTP). The app has no network auth service; the local login is GUI-only and rate-bounded by the PBKDF2 cost. |

### Red team
| Tool | Disposition | Reason / result |
|---|---|---|
| **Sliver / Mythic / Havoc / Empire+Starkiller** | N/A | C2 frameworks require an already-compromised host to beacon back. There is no exploit primitive here to gain that foothold, and no C2 surface in the app. |
| **BloodHound** | N/A | Graphs Active Directory attack paths; there is no AD/domain. |
| **NetExec (nxc) / Impacket** | N/A | Operate over SMB/Kerberos/MS-RPC; the app speaks none of these protocols. |
| **Responder** | **EXCLUDED** | LLMNR/NBT-NS/mDNS poisoning attacks **every device on the LAN**, not the POS — it targets third parties and is out of scope for an assessment of this application. Not run. |

### Adversary emulation (purple team)
| Tool | Disposition | Reason / result |
|---|---|---|
| **MITRE Caldera / Atomic Red Team** | DEPLOYMENT-LEVEL | Validate endpoint/network *detections* via ATT&CK TTPs. They assess the host an app is deployed on, not a desktop artifact in isolation. |

### Blue team — detection, SIEM, DFIR, analysis, hardening
| Tool | Disposition | Reason / result |
|---|---|---|
| **Suricata / Zeek / Snort / Security Onion / Arkime** | DEPLOYMENT-LEVEL | Network IDS/monitoring; a standalone desktop app generates no network traffic to inspect. |
| **Wazuh / Elastic Security (ELK)** | DEPLOYMENT-LEVEL | The app *does* emit a structured rolling audit log (`~/.pos/logs/pos-N.log`) — the natural ingestion point for a host SIEM in a real deployment. |
| **Velociraptor / osquery** | DEPLOYMENT-LEVEL | Endpoint visibility/IR across fleets, not a single artifact. |
| **Volatility / Autopsy / The Sleuth Kit** | N/A | Memory/disk forensics — invoked during an incident, not present here. |
| **YARA** | APPLIED (advisory) | Supply-chain integrity: the jar is reproducibly built from source and published with a SHA-256 (`checksums.txt`); no opaque third-party binaries are bundled beyond LGoodDatePicker. No malware signatures apply. |
| **Sigma** | DEPLOYMENT-LEVEL | Detection-rule format; needs a SIEM + log pipeline to compile against. |
| **Wireshark / tshark** | N/A | No network traffic to capture. |
| **CyberChef** | N/A | Data-transform utility — no opaque artifact to decode. |
| **Lynis / OpenSCAP** | DEPLOYMENT-LEVEL | Audit a host OS against CIS/STIG baselines — applicable to the machine the app runs on, not the artifact. |

---

## 3. Applied findings (detail)

- **SAST** — Semgrep (124 rules across `p/java`, `p/security-audit`, `p/secrets`): **0 findings**.
  SpotBugs + find-sec-bugs (build gate): **0 bugs**. CodeQL `security-extended` (CI): **0 alerts**.
- **SCA** — Trivy over the dependency set (sole runtime dependency: **LGoodDatePicker**; JUnit and
  spotbugs-annotations are test/provided): **0 HIGH/CRITICAL CVEs**, enforced in CI. Dependabot
  watches for new dependency and GitHub Actions updates.
- **Secrets** — gitleaks across the working tree: **0 leaks**. Bundled seed data is synthetic
  (non-issuable SSNs, `555` phone numbers, a single *masked* card record).
- **Credential storage** — salted PBKDF2-HMAC-SHA256 @ 600,000 iterations, PHC-encoded, constant-time
  verification, transparent work-factor upgrade on login. Resistant to offline cracking.
- **Data-at-rest** — full PANs and bank account numbers are **never persisted** (masked to last four);
  a regression test asserts the raw PAN never reaches disk. Free-text fields are neutralized against
  CSV/spreadsheet formula injection (CWE-1236). Writes are atomic; a corrupt data file is quarantined,
  never silently overwritten.

## 4. Conclusion

Against the real attack surface of a standalone desktop application, the assessment found **no
exploitable issues**. The remote/network/web/AD toolset has no target here (source-verified), the
static-analysis and supply-chain toolset returns clean, and credential storage withstands the
offline-cracking attack the credential tools represent. The result is empirically verified and
re-runs green in CI on every push.

## 5. Reproduce

```bash
mvn clean verify     # SpotBugs + find-sec-bugs + tests + coverage gate
trivy fs --scanners vuln,secret,misconfig --skip-dirs target .
gitleaks detect --no-git --source .
semgrep scan --config p/java --config p/security-audit --config p/secrets src/
```

See [SECURITY.md](../SECURITY.md) for the threat model and the standing CI security pipeline
(CodeQL, Semgrep, Trivy, gitleaks, Dependabot).
