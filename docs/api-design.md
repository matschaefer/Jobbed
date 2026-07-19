# Jobbed – API-Entwurf

> Status: Phase 1 (Planung) · Letzte Aktualisierung: 2026-07-18

## 1. Grundlagen

- **Basis-Pfad:** `/api/v1`
- **Format:** JSON (`application/json`), UTF-8. Zeitpunkte als ISO-8601 UTC
  (`2026-07-18T12:00:00Z`), Datumswerte als `YYYY-MM-DD`.
- **Auth:** `Authorization: Bearer <access-token>` für geschützte Endpunkte;
  Refresh-Token im HTTP-only-Cookie (siehe [security.md](security.md)).
- **Versionierung:** Pfad-basiert (`/v1`).
- **Correlation-ID:** Response-Header `X-Correlation-Id` (aus Request übernommen
  oder generiert).
- **Idempotenz:** `PUT`/`DELETE` idempotent; `PATCH` für Teiländerungen.

## 2. Einheitliches Fehlerformat

Alle Fehlerantworten (4xx/5xx) folgen diesem Schema:

```json
{
  "timestamp": "2026-01-01T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "code": "VALIDATION_ERROR",
  "message": "Die Eingabedaten sind ungültig.",
  "path": "/api/v1/applications",
  "correlationId": "c0ffee-1234",
  "fieldErrors": [
    { "field": "jobTitle", "message": "Die Stellenbezeichnung darf nicht leer sein." }
  ]
}
```

`fieldErrors` ist nur bei Validierungsfehlern gesetzt. Fehlercodes (`code`) sind
stabile, maschinenlesbare Schlüssel.

### Fehlercodes / HTTP-Status

| HTTP | `code`                  | Bedeutung |
|------|-------------------------|-----------|
| 400  | `VALIDATION_ERROR`      | Bean-Validation fehlgeschlagen |
| 400  | `MALFORMED_REQUEST`     | Nicht parsbarer Body / falscher Typ |
| 401  | `AUTHENTICATION_REQUIRED` | Kein/ungültiges Access-Token |
| 401  | `INVALID_CREDENTIALS`   | Login fehlgeschlagen |
| 401  | `TOKEN_EXPIRED`         | Access-Token abgelaufen (→ Refresh) |
| 403  | `ACCESS_DENIED`         | Rolle/Besitz unzureichend |
| 403  | `EMAIL_NOT_VERIFIED`    | E-Mail nicht bestätigt |
| 404  | `RESOURCE_NOT_FOUND`    | Ressource existiert nicht (oder gehört anderem Nutzer) |
| 409  | `RESOURCE_CONFLICT`     | z. B. E-Mail bereits vergeben, Firma mit Bewerbungen löschen |
| 413  | `PAYLOAD_TOO_LARGE`     | Datei/Body zu groß |
| 415  | `UNSUPPORTED_MEDIA_TYPE`| Unerlaubter Datei-/Content-Type |
| 422  | `BUSINESS_RULE_VIOLATION` | Fachregel verletzt |
| 429  | `RATE_LIMIT_EXCEEDED`   | Zu viele Anfragen (Login/Refresh) |
| 500  | `INTERNAL_ERROR`        | Unerwarteter Serverfehler (Details nur im Log) |

> **Sicherheitshinweis:** Bei fehlender Berechtigung auf fremde, aber
> existierende Ressourcen wird `404 RESOURCE_NOT_FOUND` zurückgegeben (kein
> `403`), um Existenz nicht zu verraten (Anti-Enumeration).

## 3. Pagination, Sortierung, Filter

Listen-Endpunkte nutzen Spring-Pageable-Konventionen.

**Query-Parameter:**

- `page` (0-basiert, Default 0)
- `size` (Default 20, Max 100)
- `sort` (`feld,richtung`, mehrfach möglich, z. B. `sort=applicationDate,desc`)
- feature-spezifische Filter (siehe unten)

**Antwortformat (einheitlicher Page-Wrapper):**

```json
{
  "content": [ /* Items */ ],
  "page": 0,
  "size": 20,
  "totalElements": 137,
  "totalPages": 7,
  "first": true,
  "last": false,
  "sort": [ { "property": "applicationDate", "direction": "DESC" } ]
}
```

## 4. DTO-Strategie

Pro Ressource getrennte DTOs – Entities werden nie serialisiert:

- `*CreateRequest` – Felder für Neuanlage (Pflichtfelder, keine IDs/Timestamps).
- `*UpdateRequest` – Felder für vollständige Aktualisierung (`PUT`).
- `*PatchRequest` – optionale Felder für Teiländerung (`PATCH`).
- `*SummaryResponse` – schlanke Listen-Repräsentation.
- `*DetailResponse` – vollständige Detail-Repräsentation inkl. verschachtelter
  Zusammenfassungen (z. B. Company-Summary in Application-Detail).

Beispiel `ApplicationCreateRequest`:

```json
{
  "companyId": "…uuid…",
  "contactPersonId": null,
  "jobTitle": "Senior Java Developer",
  "jobDescription": "…",
  "source": "LinkedIn",
  "jobUrl": "https://…",
  "employmentType": "FULL_TIME",
  "workModel": "HYBRID",
  "location": "München",
  "salaryMin": 70000,
  "salaryMax": 85000,
  "currency": "EUR",
  "applicationDate": "2026-07-10",
  "priority": "HIGH",
  "deadline": "2026-07-25",
  "nextActionDate": "2026-07-20",
  "notes": "…",
  "tagIds": ["…uuid…"]
}
```

Beispiel `ApplicationSummaryResponse` (Liste/Kanban):

```json
{
  "id": "…uuid…",
  "jobTitle": "Senior Java Developer",
  "company": { "id": "…", "name": "Acme GmbH", "logoUrl": null },
  "location": "München",
  "workModel": "HYBRID",
  "currentStatus": "APPLIED",
  "priority": "HIGH",
  "applicationDate": "2026-07-10",
  "nextActionDate": "2026-07-20",
  "tags": [ { "id": "…", "name": "Backend", "color": "#3B82F6" } ]
}
```

## 5. Endpunkte

### 5.1 Auth (`/auth`) – teils öffentlich

| Methode | Pfad | Auth | Beschreibung |
|---------|------|------|--------------|
| POST | `/auth/register` | öffentlich | Registrierung, sendet Verifikations-Mail |
| POST | `/auth/login` | öffentlich | Login → Access-Token (Body) + Refresh-Cookie |
| POST | `/auth/refresh` | Cookie | Rotiert Refresh-Token, neues Access-Token |
| POST | `/auth/logout` | Cookie | Widerruft Refresh-Token, löscht Cookie |
| POST | `/auth/verify-email` | öffentlich | Bestätigt E-Mail per Token |
| POST | `/auth/forgot-password` | öffentlich | Startet Passwort-Reset (immer 200) |
| POST | `/auth/reset-password` | öffentlich | Setzt Passwort per Token neu |
| GET  | `/auth/me` | Bearer | Aktueller Nutzer + Profil |

Login-Response:

```json
{ "accessToken": "…jwt…", "tokenType": "Bearer", "expiresIn": 900,
  "user": { "id": "…", "email": "…", "firstName": "…", "role": "USER" } }
```

### 5.2 Bewerbungen (`/applications`)

| Methode | Pfad | Beschreibung |
|---------|------|--------------|
| GET | `/applications` | Liste (Paging/Sort/Filter/Suche) |
| POST | `/applications` | Neu anlegen |
| GET | `/applications/{id}` | Detail |
| PUT | `/applications/{id}` | Vollständig aktualisieren |
| PATCH | `/applications/{id}` | Teil-Update |
| DELETE | `/applications/{id}` | Löschen |
| PATCH | `/applications/{id}/status` | Statuswechsel (+ Aktivität) |
| GET | `/applications/{id}/activities` | Aktivitäten-Timeline |
| POST | `/applications/{id}/activities` | Aktivität hinzufügen |

**Filter für `GET /applications`:** `query` (Volltext), `status` (mehrfach),
`companyId`, `priority`, `workModel`, `location`, `tagId` (mehrfach),
`applicationDateFrom`, `applicationDateTo`.

Beispiel:

```text
GET /api/v1/applications?page=0&size=20&sort=applicationDate,desc&status=APPLIED&status=SCREENING&query=java&priority=HIGH
```

`PATCH /applications/{id}/status` Body: `{ "newStatus": "INTERVIEW", "note": "Einladung erhalten" }`.

### 5.3 Unternehmen (`/companies`)

`GET` (Liste, `query`-Filter), `POST`, `GET /{id}` (Detail inkl.
Kontakt-/Bewerbungszahl), `PUT /{id}`, `DELETE /{id}`
(→ `409 RESOURCE_CONFLICT`, falls Bewerbungen existieren).

### 5.4 Kontakte (`/contacts`)

`GET` (Filter `companyId`, `query`), `POST`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}`.

### 5.5 Interviews (`/interviews`)

`GET` (Filter `applicationId`, `from`, `to`, `type`, `result`), `POST`,
`GET /{id}`, `PUT /{id}`, `DELETE /{id}`. Für Kalenderansicht liefert `from`/`to`
alle Interviews im Zeitfenster.

### 5.6 Erinnerungen (`/reminders`)

`GET` (Filter `completed`, `from`, `to`), `POST`,
`PATCH /{id}/complete`, `DELETE /{id}`.

### 5.7 Dokumente (`/documents`)

| Methode | Pfad | Beschreibung |
|---------|------|--------------|
| POST | `/documents/upload` | Multipart-Upload (`file`, `documentType`, `applicationId?`, `description?`) |
| GET | `/documents` | Liste (Filter `applicationId`, `documentType`) |
| GET | `/documents/{id}` | Metadaten |
| GET | `/documents/{id}/download` | Autorisierter Download (Stream, `Content-Disposition: attachment`) |
| DELETE | `/documents/{id}` | Löschen (Metadaten + Datei) |

Validierung: erlaubte MIME-Typen (`application/pdf`, `image/png`, `image/jpeg`,
`application/msword`, `…docx`), Max-Größe (z. B. 10 MB), Dateinamen-Sanitizing,
Content-Type-Prüfung anhand Magic-Bytes, Besitzprüfung. Download nie über
öffentliche statische Pfade.

### 5.8 Analytics (`/analytics`) – read-only

| Pfad | Inhalt |
|------|--------|
| `/analytics/overview` | Aggregat-Kennzahlen fürs Dashboard |
| `/analytics/status-distribution` | Anzahl je Status |
| `/analytics/applications-over-time?granularity=week\|month&from&to` | Zeitreihe |
| `/analytics/success-rate` | Erfolgs-/Interview-/Angebotsquote |
| `/analytics/source-performance` | Kennzahlen je Quelle |
| `/analytics/company-performance` | Kennzahlen je Unternehmen |

`overview`-Beispiel:

```json
{
  "totalApplications": 42, "applicationsThisMonth": 7, "openApplications": 18,
  "upcomingInterviews": 3, "pendingFollowUps": 5, "offers": 2, "rejections": 11,
  "successRate": 0.048, "interviewRate": 0.31
}
```

### 5.9 Stellenanzeigen-Analyse (`/job-analysis`)

| Methode | Pfad | Beschreibung |
|---------|------|--------------|
| POST | `/job-analysis/analyze` | Beschreibung + Profil-Skills → KI-/Regelanalyse mit Match |
| GET | `/ai/status` | Liefert nur Aktivierungsstatus, Provider und Modell; niemals den API-Key |

Response enthält `detectedSkills`, `matchedSkills`, `missingSkills`,
`matchPercentage`, `suggestions`, `seniorityLevel`, `workModel`,
`salaryHints`, `keywords`. Implementierung hinter `JobDescriptionAnalyzer`
(Default: `RuleBasedJobDescriptionAnalyzer`). Bei `AI_PROVIDER=openai` und
gesetztem `AI_API_KEY` wird die strukturierte KI-Analyse verwendet. Bei Fehlern
fällt der Endpunkt kontrolliert auf die Regeln zurück und kennzeichnet die Quelle.

### 5.9a Lebenslauf (`/resume`)

| Methode | Pfad | Beschreibung |
|---------|------|--------------|
| POST | `/resume/generate` | Erzeugt aus Profildaten einen strukturierten, ATS-freundlichen Lebenslauf |

Mit aktiver KI werden vorhandene Fakten zielrollenspezifisch formuliert. Ohne
KI-Konfiguration liefert der Endpunkt eine deterministische professionelle Vorlage.

### 5.10 Tags (`/tags`)

`GET`, `POST`, `PUT /{id}`, `DELETE /{id}` – jeweils nutzergebunden.

### 5.11 Profil & Einstellungen (`/profile`, `/settings`)

`GET /profile`, `PUT /profile` (UserProfile), `PUT /settings/password`
(Passwortänderung mit aktuellem Passwort), Benachrichtigungseinstellungen.

### 5.12 Benachrichtigungen (`/notifications`)

`GET /notifications` (Paging, `unreadOnly`), `PATCH /notifications/{id}/read`,
`PATCH /notifications/read-all`.

### 5.13 Suche (`/search`)

`GET /search?query=…` – globale Suche über Bewerbungen, Firmen, Kontakte, Tags,
Notizen; gruppierte Trefferliste.

### 5.14 Admin (`/admin`) – nur `ADMIN`

| Methode | Pfad | Beschreibung |
|---------|------|--------------|
| GET | `/admin/users` | Nutzerliste (Paging, Filter) |
| PATCH | `/admin/users/{id}/status` | Aktivieren/Deaktivieren |
| GET | `/admin/system` | Technische Systeminfos (aus Actuator aggregiert) |

Admins sehen **keine** privaten Bewerbungsinhalte anderer Nutzer.

### 5.15 Betrieb

`GET /actuator/health`, `/actuator/health/liveness`,
`/actuator/health/readiness`, `/actuator/info`, `/actuator/metrics`
(Metrics/Info abgesichert). Swagger UI: `/swagger-ui.html`, OpenAPI-JSON:
`/v3/api-docs`.

## 6. Statuscodes je Operation

| Operation | Erfolg |
|-----------|--------|
| Create (`POST`) | `201 Created` + `Location`-Header + Body |
| Read | `200 OK` |
| Update (`PUT`/`PATCH`) | `200 OK` |
| Delete | `204 No Content` |
| Upload | `201 Created` |
| Login | `200 OK` |
| Logout | `204 No Content` |

## 7. OpenAPI-Dokumentation

springdoc-openapi dokumentiert alle Endpunkte, Request-/Response-Schemata,
Fehlercodes, Auth (Bearer + Cookie), Pagination und Filter. Beispiel-Requests/
-Responses werden über `@Schema`-/`@ExampleObject`-Annotationen gepflegt.
