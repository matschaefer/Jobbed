# Jobbed

A fullstack job application tracker built with Angular and Spring Boot.

[Live Demo](DEPLOYMENT_URL) · [Source Code](https://github.com/matschaefer/Jobbed)

## Overview

Jobbed helps job seekers keep applications, companies, contacts, documents,
interviews and reminders in one place. The goal is to replace scattered notes
and spreadsheets with a focused web application for the job search process.

This repository is a fullstack portfolio project. It combines an Angular and
TypeScript frontend with a Java 21 Spring Boot REST API, PostgreSQL persistence,
Flyway database migrations, Docker-based local development and automated checks
with GitHub Actions.

## Live Demo

The deployed version includes a demo entry point with prepared sample data.
No registration is required for trying the main application flow.

In demo mode, the backend uses a dedicated `DEMO` role. Read-only requests are
allowed, while data-changing requests such as create, update, delete, file
upload and AI requests are blocked for the demo user. Login, refresh and logout
remain available so the demo session can work normally.

- Live demo: [DEPLOYMENT_URL](DEPLOYMENT_URL)
- Demo entry: use the **Demo live ansehen** button on the landing page or the
  demo button on the login page.

## Main Features

- Create, edit, search and filter job applications
- Kanban board with drag-and-drop status changes
- Company and contact management
- Interview and reminder scheduling
- Document upload and download for application files
- Activity timeline for status changes and notes
- Dashboard with application statistics
- Job description analysis with a rule-based fallback
- Resume builder with HTML / print-to-PDF output
- User login with protected application data

## Screenshots

Screenshots are not committed yet to avoid broken image links. The repository
contains a screenshot guide in [docs/images/README.md](docs/images/README.md).

Planned screenshots:

- Dashboard
- Kanban board
- Application details
- Job analysis
- Resume builder

## Technology Stack

### Frontend

- Angular
- TypeScript
- Angular Material
- Angular CDK Drag and Drop
- RxJS
- SCSS
- Tailwind CSS
- Chart.js / ng2-charts

### Backend

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- Spring Validation
- REST API
- MapStruct
- Lombok

### Database and Infrastructure

- PostgreSQL
- Flyway
- Docker
- Docker Compose
- Nginx for the frontend container
- Caddy in the production Docker setup
- Mailpit for local email testing
- GitHub Actions

### Testing

- JUnit 5
- Mockito
- Spring Boot Test
- Spring Security Test
- Testcontainers
- MockMvc
- Jasmine / Karma
- Playwright

## How It Works

The Angular frontend renders the user interface and communicates with the
Spring Boot backend through REST endpoints. The backend contains the application
logic, checks authentication and authorization, and reads or writes data through
Spring Data JPA. PostgreSQL stores the application data. Flyway manages
versioned database changes.

```text
Angular Frontend
        |
        | REST API
        v
Spring Boot Backend
        |
        v
PostgreSQL Database
```

The backend follows a simple layered structure:

```text
Controller -> Service -> Repository -> Database
```

Controllers expose REST endpoints, services contain the application logic,
repositories access the database, and DTOs are used for API requests and
responses.

## Selected Technical Implementations

### Kanban Board

Applications can be grouped by status and moved on a Kanban board. The frontend
uses Angular CDK Drag and Drop. A status change is sent to the backend through a
REST request and the backend stores the new status and an activity entry.

### Authentication

Spring Security and JWT are used to protect the API and associate stored data
with the authenticated user. The frontend attaches the access token to protected
requests and redirects unauthenticated users to the login page.

### Demo Mode

The demo account is created from configured demo data. A backend filter blocks
non-read requests for users with the `DEMO` role, except the auth endpoints
needed for login, refresh and logout. The frontend also shows feedback when a
demo user tries to perform a blocked action.

### Database Migrations

Flyway stores database changes as versioned migration files. This keeps the
database schema reproducible for local development, tests and deployment.

### Automated Tests

The backend contains unit and integration tests, including Spring Boot tests,
MockMvc tests and Testcontainers-based database tests. The frontend contains
unit tests for Angular services, guards, interceptors and UI logic. Playwright is
used for end-to-end scenarios.

### Continuous Integration

GitHub Actions runs checks on pushes to `main` and on pull requests:

- backend verification with Maven
- frontend dependency audit
- frontend linting
- frontend unit tests
- frontend production build
- Docker image build with Docker Compose
- Playwright end-to-end tests against the Docker Compose stack

## Local Setup

### Prerequisites

- Docker and Docker Compose
- For manual development without Docker: Java 21, Node.js 20+ and npm

### Start with Docker

```bash
cp .env.example .env
docker compose up --build
```

Local URLs:

| Service | URL |
| --- | --- |
| Frontend | http://localhost |
| Backend API | http://localhost:8080/api/v1 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Health | http://localhost:8080/actuator/health |
| Mailpit | http://localhost:8025 |

The local Docker setup enables demo data by default. A seeded user is available:

```text
Email: analytics@jobbed.local
Password: Str0ng!Passw0rd
```

### Run Frontend and Backend Separately

Backend:

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Frontend:

```bash
cd frontend
npm install
npm start
```

The Angular development server runs on `http://localhost:4200` and proxies
`/api` requests to the backend.

## Tests

Backend:

```bash
cd backend
./mvnw verify
```

Frontend:

```bash
cd frontend
npm run lint
npm run test:ci
npm run build
```

End-to-end tests:

```bash
cd frontend
npm run e2e
```

Docker build:

```bash
docker compose build
```

## Project Structure

```text
.
├── backend/              # Spring Boot REST API
├── frontend/             # Angular application
├── docs/                 # Architecture, deployment and project notes
├── deploy/               # Caddy configuration for Docker deployment
├── .github/workflows/    # GitHub Actions workflow
├── docker-compose.yml    # Local Docker stack
├── compose.prod.yml      # Production Docker stack
├── vercel.json           # Vercel frontend deployment settings
└── LICENSE
```

## What I Learned

- Connecting an Angular frontend to a Spring Boot REST API
- Building user-specific application data with Spring Security
- Modeling relational data with PostgreSQL and Spring Data JPA
- Managing database changes with Flyway migrations
- Writing backend, frontend and end-to-end tests
- Containerizing a fullstack application with Docker
- Preparing a deployed portfolio application with GitHub Actions checks

## Current Limitations

- The public demo is read-only, so visitors cannot permanently change demo data.
- External AI features require backend configuration and are blocked for demo
  users.
- The application is currently focused on individual job seekers, not team
  collaboration.
- Production email delivery requires an SMTP provider. Mailpit is only used for
  local development.
- Uploaded documents are stored outside the Git repository.

## License

This project is licensed under the [MIT License](LICENSE).
