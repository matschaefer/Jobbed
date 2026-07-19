# Jobbed – Datenmodell

> Status: Phase 1 (Planung) · Letzte Aktualisierung: 2026-07-18

## 1. Grundsätze

- **Mandantentrennung auf Zeilenebene:** Nahezu jede fachliche Tabelle trägt
  `user_id` als Fremdschlüssel. Autorisierung erfolgt serverseitig durch
  Abgleich mit dem Security-Context – die `userId` kommt **nie** aus dem Request.
- **UUID-Primärschlüssel** (`uuid`, serverseitig generiert) statt fortlaufender
  IDs, um Enumeration/IDOR-Angriffe zu erschweren.
- **Zeitstempel** `created_at` / `updated_at` als `timestamptz` (UTC).
- **Enums als `varchar` + CHECK/Applikations-Validierung** (keine Postgres-Enum-
  Typen, um Flyway-Migrationen einfach zu halten).
- **Soft-Delete** wird bewusst **nicht** global eingeführt; Bewerbungen nutzen
  stattdessen den Status `ARCHIVED`.
- **Geldbeträge** als `numeric(12,2)` + separates `currency` (ISO-4217, `char(3)`).

## 2. ER-Diagramm

```mermaid
erDiagram
    USER ||--|| USER_PROFILE : "hat"
    USER ||--o{ COMPANY : "besitzt"
    USER ||--o{ CONTACT_PERSON : "besitzt"
    USER ||--o{ JOB_APPLICATION : "besitzt"
    USER ||--o{ INTERVIEW : "besitzt"
    USER ||--o{ DOCUMENT : "besitzt"
    USER ||--o{ REMINDER : "besitzt"
    USER ||--o{ TAG : "besitzt"
    USER ||--o{ APPLICATION_ACTIVITY : "besitzt"
    USER ||--o{ REFRESH_TOKEN : "besitzt"

    COMPANY ||--o{ CONTACT_PERSON : "beschäftigt"
    COMPANY ||--o{ JOB_APPLICATION : "erhält"
    CONTACT_PERSON ||--o{ JOB_APPLICATION : "betreut (optional)"

    JOB_APPLICATION ||--o{ APPLICATION_ACTIVITY : "protokolliert"
    JOB_APPLICATION ||--o{ INTERVIEW : "plant"
    JOB_APPLICATION ||--o{ DOCUMENT : "referenziert (optional)"
    JOB_APPLICATION ||--o{ REMINDER : "erinnert (optional)"
    JOB_APPLICATION }o--o{ TAG : "über APPLICATION_TAG"

    INTERVIEW ||--o{ REMINDER : "erinnert (optional)"

    USER {
        uuid id PK
        varchar first_name
        varchar last_name
        varchar email UK
        varchar password_hash
        varchar role
        boolean enabled
        boolean email_verified
        timestamptz created_at
        timestamptz updated_at
        timestamptz last_login_at
    }
    USER_PROFILE {
        uuid id PK
        uuid user_id FK,UK
        varchar phone
        varchar location
        varchar linkedin_url
        varchar github_url
        varchar portfolio_url
        varchar preferred_job_title
        varchar preferred_locations
        numeric desired_salary
        char currency
        varchar notice_period
        varchar profile_image_url
    }
    COMPANY {
        uuid id PK
        uuid user_id FK
        varchar name
        varchar website
        varchar industry
        varchar company_size
        varchar location
        text description
        varchar logo_url
        timestamptz created_at
        timestamptz updated_at
    }
    CONTACT_PERSON {
        uuid id PK
        uuid user_id FK
        uuid company_id FK
        varchar first_name
        varchar last_name
        varchar position
        varchar email
        varchar phone
        varchar linkedin_url
        text notes
        timestamptz created_at
        timestamptz updated_at
    }
    JOB_APPLICATION {
        uuid id PK
        uuid user_id FK
        uuid company_id FK
        uuid contact_person_id FK "nullable"
        varchar job_title
        text job_description
        varchar source
        varchar job_url
        varchar employment_type
        varchar work_model
        varchar location
        numeric salary_min
        numeric salary_max
        char currency
        date application_date
        varchar current_status
        varchar priority
        smallint rating
        date deadline
        date next_action_date
        text notes
        text rejection_reason
        timestamptz created_at
        timestamptz updated_at
    }
    APPLICATION_ACTIVITY {
        uuid id PK
        uuid application_id FK
        uuid user_id FK
        varchar activity_type
        varchar title
        text description
        varchar previous_status
        varchar new_status
        timestamptz activity_date
        timestamptz created_at
    }
    INTERVIEW {
        uuid id PK
        uuid user_id FK
        uuid application_id FK
        varchar interview_type
        varchar title
        timestamptz start_date_time
        timestamptz end_date_time
        varchar time_zone
        varchar location
        varchar meeting_url
        varchar interviewer_names
        text notes
        varchar result
        boolean reminder_enabled
        int reminder_minutes_before
        timestamptz created_at
        timestamptz updated_at
    }
    DOCUMENT {
        uuid id PK
        uuid user_id FK
        uuid application_id FK "nullable"
        varchar document_type
        varchar original_file_name
        varchar stored_file_name
        varchar mime_type
        bigint file_size
        varchar storage_path
        varchar description
        timestamptz created_at
    }
    REMINDER {
        uuid id PK
        uuid user_id FK
        uuid application_id FK "nullable"
        uuid interview_id FK "nullable"
        varchar title
        text description
        timestamptz reminder_date_time
        boolean completed
        boolean sent
        timestamptz created_at
        timestamptz updated_at
    }
    TAG {
        uuid id PK
        uuid user_id FK
        varchar name
        varchar color
    }
    APPLICATION_TAG {
        uuid application_id FK
        uuid tag_id FK
    }
    REFRESH_TOKEN {
        uuid id PK
        uuid user_id FK
        varchar token_hash UK
        timestamptz expires_at
        boolean revoked
        varchar replaced_by
        varchar user_agent
        varchar ip_address
        timestamptz created_at
    }
```

## 3. Enum-Wertebereiche

| Enum                 | Werte |
|----------------------|-------|
| `role`               | `USER`, `ADMIN` |
| `current_status`     | `SAVED`, `PREPARING`, `APPLIED`, `SCREENING`, `INTERVIEW`, `TECHNICAL_INTERVIEW`, `FINAL_INTERVIEW`, `OFFER`, `ACCEPTED`, `REJECTED`, `WITHDRAWN`, `ARCHIVED` |
| `priority`           | `LOW`, `MEDIUM`, `HIGH`, `URGENT` |
| `employment_type`    | `FULL_TIME`, `PART_TIME`, `CONTRACT`, `INTERNSHIP`, `WORKING_STUDENT`, `FREELANCE` |
| `work_model`         | `ONSITE`, `HYBRID`, `REMOTE` |
| `activity_type`      | `CREATED`, `STATUS_CHANGED`, `NOTE_ADDED`, `EMAIL_SENT`, `INTERVIEW_SCHEDULED`, `FOLLOW_UP`, `DOCUMENT_UPLOADED`, `OFFER_RECEIVED`, `REJECTED`, `CUSTOM` |
| `interview_type`     | `PHONE`, `VIDEO`, `ONSITE`, `TECHNICAL`, `HR`, `CULTURAL_FIT`, `FINAL`, `OTHER` |
| `interview.result`   | `PENDING`, `PASSED`, `FAILED`, `CANCELLED`, `NO_SHOW` |
| `document_type`      | `CV`, `COVER_LETTER`, `CERTIFICATE`, `REFERENCE`, `PORTFOLIO`, `JOB_DESCRIPTION`, `OTHER` |

Der `rating`-Wert ist eine Ganzzahl 1–5 (CHECK-Constraint). `current_status`
wird im Frontend über eine konfigurierbare Metadaten-Tabelle (Label, Farbe,
Reihenfolge, Kanban-Spalte) dargestellt.

## 4. Bewerbungsstatus-Workflow

```mermaid
stateDiagram-v2
    [*] --> SAVED
    SAVED --> PREPARING
    PREPARING --> APPLIED
    SAVED --> APPLIED
    APPLIED --> SCREENING
    SCREENING --> INTERVIEW
    INTERVIEW --> TECHNICAL_INTERVIEW
    TECHNICAL_INTERVIEW --> FINAL_INTERVIEW
    INTERVIEW --> FINAL_INTERVIEW
    FINAL_INTERVIEW --> OFFER
    OFFER --> ACCEPTED
    OFFER --> REJECTED

    APPLIED --> REJECTED
    SCREENING --> REJECTED
    INTERVIEW --> REJECTED
    TECHNICAL_INTERVIEW --> REJECTED
    FINAL_INTERVIEW --> REJECTED

    SAVED --> WITHDRAWN
    APPLIED --> WITHDRAWN
    SCREENING --> WITHDRAWN
    INTERVIEW --> WITHDRAWN

    ACCEPTED --> ARCHIVED
    REJECTED --> ARCHIVED
    WITHDRAWN --> ARCHIVED
    ARCHIVED --> [*]
```

Übergänge werden im Frontend als erlaubte Ziele geführt; das Backend erlaubt aus
Pragmatismus jeden Statuswechsel, protokolliert ihn aber als
`APPLICATION_ACTIVITY` (`STATUS_CHANGED` mit `previous_status`/`new_status`).
Wechsel nach `REJECTED`/`WITHDRAWN` erfordern im Frontend einen Bestätigungsdialog.

## 5. Beziehungen und referentielle Integrität

| Beziehung                              | Kardinalität | ON DELETE |
|----------------------------------------|--------------|-----------|
| USER → USER_PROFILE                    | 1 : 1        | CASCADE   |
| USER → COMPANY / CONTACT / APPLICATION | 1 : n        | CASCADE   |
| COMPANY → JOB_APPLICATION              | 1 : n        | RESTRICT (Firma mit Bewerbungen nicht löschbar) |
| COMPANY → CONTACT_PERSON               | 1 : n        | CASCADE   |
| CONTACT_PERSON → JOB_APPLICATION       | 0..1 : n     | SET NULL  |
| JOB_APPLICATION → APPLICATION_ACTIVITY | 1 : n        | CASCADE   |
| JOB_APPLICATION → INTERVIEW            | 1 : n        | CASCADE   |
| JOB_APPLICATION ↔ TAG                  | n : m        | CASCADE (Join) |
| APPLICATION/INTERVIEW → REMINDER       | 0..1 : n     | CASCADE   |
| USER → REFRESH_TOKEN                   | 1 : n        | CASCADE   |

## 6. Indizes

Neben den automatischen PK-/UK-Indizes:

| Tabelle              | Index                                                   | Zweck |
|----------------------|---------------------------------------------------------|-------|
| `user`               | `UNIQUE(lower(email))`                                   | Login, Eindeutigkeit case-insensitiv |
| `job_application`    | `(user_id, current_status)`                             | Kanban/Filter |
| `job_application`    | `(user_id, company_id)`                                 | Filter nach Firma |
| `job_application`    | `(user_id, application_date DESC)`                      | Sortierung/Zeitreihen |
| `job_application`    | `(user_id, next_action_date)`                           | Follow-ups/Dashboard |
| `job_application`    | GIN auf `to_tsvector(job_title || job_description || notes)` | Volltextsuche |
| `application_activity` | `(application_id, activity_date DESC)`                | Timeline |
| `interview`          | `(user_id, start_date_time)`                            | Kalender |
| `reminder`           | `(reminder_date_time)` WHERE `sent = false AND completed = false` | Scheduler-Scan |
| `company`            | `(user_id, name)`                                       | Autocomplete/Suche |
| `contact_person`     | `(user_id, company_id)`                                 | Kontaktliste |
| `document`           | `(user_id, application_id)`                             | Dokumentliste |
| `refresh_token`      | `UNIQUE(token_hash)`, `(user_id)`                       | Refresh-Rotation |

## 7. Suche

Erste Version: PostgreSQL Full-Text-Search (`tsvector` + GIN-Index) für
Bewerbungen sowie `ILIKE`-Suche auf Firmen/Kontakte/Tags. Die Suchlogik liegt
hinter einem `SearchService`-Interface, damit später Elasticsearch/OpenSearch
ergänzt werden kann, ohne Aufrufer zu ändern.

## 8. Seed-/Demo-Daten

Für `dev` erzeugt eine Flyway-`R__seed`-Migration bzw. ein `CommandLineRunner`
(profilabhängig) u. a.: 1 Demo-USER + 1 ADMIN, ≥ 5 Unternehmen, ≥ 15
Bewerbungen über alle Status verteilt, Kontakte, Interviews, Erinnerungen, Tags
und Aktivitäten. Keine echten personenbezogenen Daten. Demo-Login wird im README
dokumentiert.
