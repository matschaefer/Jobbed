# Deployment

Status: Deployed portfolio project  
Last updated: July 2026

Jobbed can be deployed either as separate services or as a Docker-based stack.
The repository contains configuration for both local Docker usage and a
production Docker Compose setup.

## Current Portfolio Deployment

The repository contains Vercel configuration for the Angular frontend and an API
rewrite to a Render backend:

- `vercel.json`
- `frontend/vercel.json`

The public frontend URL is not stored in the repository. Use `DEPLOYMENT_URL` in
documentation until the final URL is added to the GitHub repository metadata.

## Docker Production Setup

The production Docker setup is defined in:

```text
compose.prod.yml
deploy/Caddyfile
```

It contains:

- PostgreSQL
- Spring Boot backend
- Angular frontend container
- Caddy reverse proxy

The example environment file is:

```text
.env.production.example
```

At minimum, production deployment requires:

- domain name for the Docker/Caddy setup
- PostgreSQL password
- JWT secret
- SMTP settings for real emails

## Local Docker Setup

For local development:

```bash
cp .env.example .env
docker compose up --build
```

Local services:

| Service | URL |
| --- | --- |
| Frontend | http://localhost |
| Backend API | http://localhost:8080/api/v1 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Health | http://localhost:8080/actuator/health |
| Mailpit | http://localhost:8025 |

## Demo Mode

The public demo mode is controlled through environment variables:

```env
DEMO_MODE_ENABLED=true
DEMO_MODE_EMAIL=demo@jobbed.local
```

When enabled, the backend creates a demo user if it does not already exist.
Users with the `DEMO` role can read sample data, but data-changing requests are
blocked by `DemoModeWriteProtectionFilter`.

Allowed demo write requests are limited to:

- demo login
- token refresh
- logout

## Email

Mailpit is used only for local development. A deployed version needs an SMTP
provider for registration, verification and password reset emails.

If no real SMTP provider is configured, the application can still be shown
through the read-only demo mode, but real registration emails will not be
delivered.

## Updating a Docker Deployment

```bash
git pull --ff-only
docker compose --env-file .env.production -f compose.prod.yml up -d --build
```

Flyway runs pending database migrations when the backend starts.

## Simple Release Checklist

- Frontend URL opens
- Backend health endpoint responds
- Vercel API rewrite points to the deployed backend
- `SPRING_PROFILES_ACTIVE=prod` is set for the backend
- database connection uses a valid JDBC URL
- `JWT_SECRET` is set
- demo mode works for read-only exploration
- no real `.env` file is committed
