# Architecture

Status: Deployed portfolio project  
Last updated: July 2026

## Overview

Jobbed is a fullstack web application with three main parts:

- Angular frontend
- Spring Boot backend
- PostgreSQL database

The frontend communicates with the backend through JSON REST endpoints under
`/api/v1`. The backend stores data in PostgreSQL and manages database schema
changes with Flyway.

```text
Browser
  |
  v
Angular Frontend
  |
  | REST / JSON
  v
Spring Boot Backend
  |
  | Spring Data JPA
  v
PostgreSQL
```

## Frontend

The frontend lives in `frontend/` and is built with Angular and TypeScript.

Main points:

- standalone Angular components
- Angular Router for page navigation
- route guards for protected application pages
- HttpClient services for backend communication
- interceptors for authentication, demo-mode feedback and error handling
- Angular Material and custom SCSS/Tailwind styling
- lazy-loaded feature routes

Important folders:

```text
frontend/src/app/
├── core/       # API services, auth store, guards, interceptors and models
├── features/   # application pages such as dashboard, board and calendar
├── layout/     # authenticated application shell
└── shared/     # reusable UI components
```

## Backend

The backend lives in `backend/` and is built with Java 21 and Spring Boot.

The code follows a simple layered structure:

```text
Controller -> Service -> Repository -> Database
```

- Controllers expose REST endpoints.
- Services contain application logic.
- Repositories use Spring Data JPA to access PostgreSQL.
- DTOs are used for API requests and responses.
- Flyway migration files define database changes.

Important backend areas:

```text
backend/src/main/java/com/jobbed/
├── auth/          # registration, login, JWT and demo login
├── application/   # job applications, status changes and activities
├── company/       # companies
├── contact/       # contacts
├── document/      # document upload and download
├── interview/     # interviews
├── reminder/      # reminders
├── analytics/     # dashboard statistics
├── jobanalysis/   # job description analysis
├── resume/        # resume generation
├── security/      # Spring Security configuration and filters
└── common/        # shared error handling and utility code
```

## Database

PostgreSQL stores users, applications, companies, contacts, documents,
interviews, reminders and activity data.

Flyway migrations are located in:

```text
backend/src/main/resources/db/migration/
```

The production profile validates the schema through JPA and applies Flyway
migrations on backend startup.

## Docker Setup

The local Docker Compose setup contains:

- PostgreSQL
- Mailpit for local email testing
- Spring Boot backend
- Angular frontend served by Nginx

The production Compose file additionally contains Caddy as a reverse proxy.

## Continuous Integration

GitHub Actions runs backend verification, frontend linting, frontend tests,
frontend build, Docker builds and Playwright end-to-end tests. The workflow is
defined in `.github/workflows/ci.yml`.
