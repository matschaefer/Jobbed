# Jobbed – Technische Risiken & Gegenmaßnahmen

> Status: Phase 1 (Planung) · Letzte Aktualisierung: 2026-07-18

Bewertung: **W** = Wahrscheinlichkeit, **A** = Auswirkung (jeweils
niedrig/mittel/hoch).

## 1. Sicherheitsrisiken

| ID | Risiko | W | A | Gegenmaßnahme |
|----|--------|---|---|---------------|
| S1 | **Broken Access Control / IDOR** – Nutzer greift auf fremde Ressourcen zu | mittel | hoch | Ownership immer im Backend (`findByIdAndUserId`), `userId` nur aus SecurityContext, UUID-PKs, `404` statt `403`, dedizierte Mandantentrennungs-Tests je Feature |
| S2 | **Token-Diebstahl via XSS** | mittel | hoch | Access-Token nur im RAM (kein `localStorage`), Refresh-Token als HttpOnly-Cookie, strikte CSP, Angular-Escaping, Markdown-Sanitizer |
| S3 | **Refresh-Token-Missbrauch/Replay** | niedrig | hoch | Rotation + Reuse-Detection (alle Sessions widerrufen), nur Hash in DB, kurze Access-Lebensdauer |
| S4 | **CSRF auf Cookie-Endpunkten** | niedrig | mittel | `SameSite=Strict`, enger Cookie-`Path`, optional Double-Submit-Token |
| S5 | **Brute-Force/Credential-Stuffing** | mittel | mittel | Rate-Limiting (IP+Account), Backoff, angeglichene Antwortzeiten, generische Fehlermeldungen |
| S6 | **Gefährliche Datei-Uploads** (Malware, Path-Traversal, XSS-SVG) | mittel | hoch | MIME+Magic-Byte-Whitelist, Größenlimit, Dateinamen-Sanitizing, generierte Storage-Namen, Download nur als `attachment` mit `nosniff`, kein öffentliches Verzeichnis |
| S7 | **Enumeration von E-Mails** (Register/Forgot) | mittel | niedrig | Einheitliche `200`-Antworten, keine „User existiert"-Hinweise |
| S8 | **Secrets im Repository** | niedrig | hoch | `.env.example` statt echter Werte, `.gitignore`, Secret-Scanning in CI, Secrets nur via Umgebung |
| S9 | **Verwundbare Abhängigkeiten** | mittel | mittel | Dependency-/Vulnerability-Scanning in CI (z. B. `npm audit`, OWASP Dependency-Check), regelmäßige Updates, gepinnte Base-Images |
| S10 | **Sensible Daten in Logs** | niedrig | mittel | Keine Passwörter/Token/PII loggen, Log-Review, strukturierte Logs mit Filterung |

## 2. Technische / Architektur-Risiken

| ID | Risiko | W | A | Gegenmaßnahme |
|----|--------|---|---|---------------|
| T1 | **Umfang zu groß** – Projekt wird nicht fertig | hoch | mittel | Strikte Phasen mit AK, jede Phase liefert lauffähigen Stand, Kernfunktionen zuerst, „nice-to-have" (MinIO/S3, KI-Adapter, Prometheus) optional |
| T2 | **Entity-Leak in API** (fehlende DTO-Trennung) | mittel | mittel | Konsequente DTO-Strategie, MapStruct, Architektur-Test, der Entity-Serialisierung verbietet |
| T3 | **Flyway-Migrationskonflikte** (Drift, geänderte Migrationen) | mittel | mittel | Migrationen unveränderlich nach Merge, `validate` in prod, Testcontainers-Migrationstests, klare Versionierung |
| T4 | **Zeitzonen-/Datumsfehler** (Interviews, Reminder) | mittel | mittel | Alles in UTC (`timestamptz`), `timeZone` explizit am Interview, Umrechnung nur in der UI |
| T5 | **Silent-Refresh-Sturm** (parallele 401 lösen viele Refreshes aus) | mittel | mittel | Single-Flight-Refresh im Interceptor (eine laufende Refresh-Promise, andere warten) |
| T6 | **Windows/POSIX-Pfad- und Zeilenende-Probleme** in Docker/CI | mittel | niedrig | Container-Builds als Referenz, `.gitattributes` (LF), Pfade plattformneutral |
| T7 | **Inkonsistente Statuslogik** zwischen FE und BE | niedrig | mittel | Status als geteilte, konfigurierbare Metadaten, BE protokolliert jeden Wechsel, Tests für Kanban-Statusänderungen |
| T8 | **Kopplung an lokalen Datei-/Suchspeicher** | niedrig | mittel | Interfaces (`FileStorageService`, `SearchService`, `JobDescriptionAnalyzer`) für spätere Cloud-/KI-/ES-Adapter |

## 3. Performance-Risiken

| ID | Risiko | W | A | Gegenmaßnahme |
|----|--------|---|---|---------------|
| P1 | **N+1-Queries** (Bewerbung→Firma/Tags/Aktivitäten) | hoch | mittel | Gezielte `JOIN FETCH`/Entity-Graphs, projizierende DTO-Queries für Listen, Query-Count-Assertions in Tests |
| P2 | **Große Listen ohne Pagination** | mittel | mittel | Pflicht-Pagination, Max-`size`=100, sinnvolle Indizes |
| P3 | **Langsame Volltextsuche** | mittel | mittel | `tsvector` + GIN-Index; `SearchService`-Abstraktion für spätere ES-Migration |
| P4 | **Teure Analytics-Aggregationen** | mittel | mittel | Read-optimierte SQL-Aggregate, passende Indizes, optional Caching kurzer TTL |
| P5 | **Große Frontend-Bundles** | mittel | niedrig | Lazy-Loading pro Route, Tree-Shaking, `ng2-charts` gezielt importieren, Bundle-Budget in CI |
| P6 | **Scheduler-Last / Doppelversand** | niedrig | mittel | Effizienter Index (`WHERE sent=false`), Batch-Verarbeitung, idempotenter Versand mit `sent`-Flag und Sperre |

## 4. Betriebs-/Prozessrisiken

| ID | Risiko | W | A | Gegenmaßnahme |
|----|--------|---|---|---------------|
| O1 | **Nicht reproduzierbares Setup** | mittel | mittel | Alles via `docker compose up --build`, dokumentierte `.env.example`, Healthchecks + `depends_on` |
| O2 | **Instabile/flaky Tests in CI** | mittel | mittel | Deterministische Testdaten, Testcontainers statt geteilter DB, klare Isolation, Retry nur gezielt |
| O3 | **Fehlende Nachvollziehbarkeit im Fehlerfall** | mittel | mittel | Correlation-ID Ende-zu-Ende, strukturierte Logs, Actuator-Health/Metrics |
| O4 | **CI-Laufzeit zu lang** | mittel | niedrig | Caching (Maven/npm), parallele Jobs, Docker-Layer-Caching |
| O5 | **Scope-Creep bei Benachrichtigungen/E-Mail** | mittel | niedrig | Lokal Mailpit, klar abgegrenzte Kanäle, Prod-SMTP nur konfigurativ |

## 5. Priorisierte Top-Risiken

1. **S1 – Mandantentrennung/IDOR** → höchste Priorität, in jeder Phase testen.
2. **T1 – Projektumfang** → strikte Phasen-/AK-Disziplin.
3. **P1 – N+1-Queries** → früh mit Query-Count-Tests absichern.
4. **S2/S3 – Token-Sicherheit** → korrektes Cookie-/Rotationsmodell von Beginn an.
5. **T3/T4 – Migrationen & Zeitzonen** → Konventionen früh festlegen (UTC,
   unveränderliche Migrationen).

## 6. Bewusste Vereinfachungen (Erstversion)

- Suche via PostgreSQL statt Elasticsearch (hinter `SearchService` austauschbar).
- Datei-Speicher lokal statt S3/MinIO (hinter `FileStorageService`).
- Stellenanzeigen-Analyse regelbasiert statt KI (Adapter vorbereitet).
- Monitoring über Actuator; Prometheus/Grafana optional.

Diese Vereinfachungen sind dokumentiert und über Interfaces so gekapselt, dass
spätere Erweiterungen keine Aufrufer brechen.
