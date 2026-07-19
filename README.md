# Jobbed

> Bewerbungs-Tracker für Jobsuchende – Bewerbungen verwalten, per Kanban
> organisieren, Interviews & Erinnerungen planen, Dokumente ablegen und den
> gesamten Bewerbungsprozess auswerten.

**Portfolio-Projekt** mit Angular (Standalone, Signals), Spring Boot (Java 21),
PostgreSQL, Docker und CI/CD.

![Status](https://img.shields.io/badge/status-release%20candidate-violet)
![License](https://img.shields.io/badge/license-MIT-blue)

## Inhaltsverzeichnis

- [Funktionsübersicht](#funktionsübersicht)
- [Technologie-Stack](#technologie-stack)
- [Architektur](#architektur)
- [Voraussetzungen](#voraussetzungen)
- [Schnellstart mit Docker](#schnellstart-mit-docker)
- [Lokale Entwicklung](#lokale-entwicklung)
- [Konfiguration](#konfiguration)
- [Tests](#tests)
- [API-Dokumentation](#api-dokumentation)
- [Sicherheitskonzept](#sicherheitskonzept)
- [Ordnerstruktur](#ordnerstruktur)
- [Bekannte Einschränkungen](#bekannte-einschränkungen)
- [Deployment](#deployment)
- [Lizenz](#lizenz)

## Funktionsübersicht

- Bewerbungen erstellen, bearbeiten, filtern und durchsuchen
- Kanban-Board mit Drag & Drop und Statusverlauf
- Unternehmen und Ansprechpartner verwalten
- Interviews & Erinnerungen inkl. E-Mail-Benachrichtigungen
- Dokumenten-Upload (CV, Anschreiben, …) mit sicherem Download
- Aktivitäten-Timeline pro Bewerbung
- Statistiken & Dashboard
- Stellenanzeigen-Analyse mit optionaler KI und zuverlässigem Regel-Fallback
- KI-gestützter, ATS-freundlicher Lebenslauf-Generator mit HTML-/PDF-Ausgabe
- Rollen USER / ADMIN mit strikter Mandantentrennung

## Technologie-Stack

| Bereich   | Technologien |
|-----------|--------------|
| Frontend  | Angular (Standalone, Signals), TypeScript, Angular Material, CDK Drag&Drop, RxJS, ng2-charts, SCSS, ESLint, Prettier |
| Backend   | Java 21, Spring Boot, Spring Web, Spring Data JPA, Spring Security, Bean Validation, MapStruct, Lombok |
| Daten     | PostgreSQL, Flyway |
| Infra     | Docker, Docker Compose, GitHub Actions, Mailpit, optional MinIO |
| Qualität  | JUnit 5, Mockito, Testcontainers, MockMvc, Jasmine/Karma, Playwright |
| Betrieb   | Actuator, OpenAPI/Swagger, strukturierte Logs, Correlation-ID |

## Architektur

Detaillierte Diagramme in [docs/architecture.md](docs/architecture.md).

```text
Angular SPA  ──REST /api/v1──►  Spring Boot API  ──JDBC──►  PostgreSQL
                                      │
                                      ├── SMTP (Mailpit)
                                      ├── Datei-Speicher (lokal / MinIO)
                                      └── KI-API (optional, nur serverseitiger Key)
```

## Voraussetzungen

- **Für Docker-Start:** Docker + Docker Compose.
- **Für lokale Entwicklung:** Java 21, Node 20+, npm 10+. Maven ist optional –
  das Backend bringt den Maven-Wrapper (`./mvnw`) mit.

## Schnellstart mit Docker

```bash
cp .env.example .env      # Werte anpassen (mind. Passwörter/Secret)
docker compose up --build
```

| Dienst        | URL |
|---------------|-----|
| Frontend      | http://localhost |
| Backend API   | http://localhost:8080/api/v1 |
| Swagger UI    | http://localhost:8080/swagger-ui.html |
| Health        | http://localhost:8080/actuator/health |
| Mailpit (Mail)| http://localhost:8025 |

Im `dev`-Profil werden bei einer leeren Datenbank idempotent Demo-Daten angelegt:

- E-Mail: `analytics@jobbed.local`
- Passwort: `Str0ng!Passw0rd`
- 8 Bewerbungen in unterschiedlichen Status sowie 3 Unternehmen

Mit `DEMO_DATA_ENABLED=false` lässt sich das Seed-Verhalten abschalten.

## Lokale Entwicklung

**Backend:**

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

**Frontend:**

```bash
cd frontend
npm install
npm start        # http://localhost:4200 (Proxy /api -> :8080)
```

## Konfiguration

Alle Umgebungsvariablen sind in [.env.example](.env.example) dokumentiert.
Spring-Profile: `dev`, `test`, `prod`. Angular-Environments unter
`frontend/src/environments/`.

### Optionale KI aktivieren

Ohne API-Key bleibt Jobbed vollständig nutzbar: Die Stellenanalyse verwendet
Regeln und der Lebenslauf-Generator eine professionelle Vorlage. Für echte
KI-Ausgaben werden ausschließlich im Backend folgende Werte gesetzt:

```env
AI_PROVIDER=openai
AI_API_KEY=sk-...
AI_MODEL=gpt-5-mini
```

Danach den Backend-Container neu bauen: `docker compose up -d --build backend`.
Der Schlüssel wird niemals an das Frontend ausgeliefert. Stellenanzeigen und
Lebenslaufdaten werden nur bei aktivierter KI an den konfigurierten Dienst gesendet.

## Tests

```bash
# Backend
cd backend && ./mvnw test

# Frontend
cd frontend && npm run lint && npm run test:ci && npm run build

# Gesamter Container-Build
docker compose build
```

## API-Dokumentation

OpenAPI/Swagger UI: `http://localhost:8080/swagger-ui.html`.
Der Entwurf ist in [docs/api-design.md](docs/api-design.md) beschrieben.

## Deployment

Die produktive Docker-Konfiguration mit automatischem HTTPS ist in
[docs/deployment.md](docs/deployment.md) beschrieben. Für echte Registrierungs-
und Reset-Mails muss ein Produktions-SMTP-Anbieter konfiguriert werden; Mailpit
ist ausschließlich für die lokale Entwicklung vorgesehen.

## Sicherheitskonzept

Vollständig in [docs/security.md](docs/security.md): kurzlebige JWT-Access-Tokens,
rotierende Refresh-Tokens in HttpOnly-Cookies, BCrypt, Rate-Limiting, strikte
Mandantentrennung, sichere Header/CORS, OWASP-Top-10-Abgleich.

## Ordnerstruktur

```text
jobbed/
├── frontend/            # Angular SPA
├── backend/             # Spring Boot API
├── docker/              # Zusätzliche Container-Assets (optional)
├── docs/                # Architektur, Datenmodell, API, Security, Plan, Risiken
├── .github/workflows/   # CI/CD (GitHub Actions)
├── docker-compose.yml
├── .env.example
├── README.md
└── LICENSE
```

## Bekannte Einschränkungen

- Erstversion nutzt PostgreSQL-Suche statt Elasticsearch.
- Datei-Speicher lokal; S3/MinIO als Adapter vorbereitet.
- Echte KI-Ausgaben benötigen einen eigenen API-Key; ohne Key arbeitet Jobbed im klar gekennzeichneten Fallback-Modus.

## Lizenz

[MIT](LICENSE)
