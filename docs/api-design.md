# API Overview

Status: Deployed portfolio project  
Last updated: July 2026

The backend exposes a JSON REST API under:

```text
/api/v1
```

Swagger UI is available in local development at:

```text
http://localhost:8080/swagger-ui.html
```

## Main API Areas

| Area | Purpose |
| --- | --- |
| `/auth` | registration, login, demo login, refresh, logout, email verification and password reset |
| `/account` | account-related actions |
| `/applications` | job applications, filters, details, status changes and activity entries |
| `/companies` | companies |
| `/contacts` | contacts |
| `/interviews` | interview scheduling |
| `/reminders` | reminders |
| `/documents` | document upload, download and deletion |
| `/analytics` | dashboard statistics |
| `/notifications` | in-app notifications |
| `/job-analysis` | job description analysis |
| `/resume` | resume generation |
| `/ai/status` | configured AI status |

## Authentication

Protected endpoints require authentication. The frontend logs in through the
auth endpoints and sends the received access token with protected requests.

## Request and Response Format

The API uses JSON request and response bodies. Validation errors and backend
errors are returned as structured error responses so the frontend can show
useful messages.

## Pagination and Filtering

List endpoints such as applications, companies and contacts support query
parameters for pagination, search and filtering where needed.

Example:

```text
GET /api/v1/applications?page=0&size=20&query=java
```

## Demo Mode

The demo login endpoint is:

```text
POST /api/v1/auth/demo
```

After demo login, read requests are allowed. Data-changing requests are blocked
for the demo user by the backend.
