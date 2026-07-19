# Data Model

Status: Deployed portfolio project  
Last updated: July 2026

Jobbed stores its application data in PostgreSQL. The schema is managed through
Flyway migration files in:

```text
backend/src/main/resources/db/migration/
```

## Main Data Areas

| Area | Description |
| --- | --- |
| Users | registered users, roles and profile data |
| Auth tokens | verification, reset and session-related tokens |
| Companies | companies connected to job applications |
| Contacts | contact people for companies |
| Applications | job applications and their current status |
| Tags | user-defined labels for applications |
| Activities | notes and status-change history for applications |
| Interviews | scheduled interviews |
| Reminders | follow-up reminders |
| Documents | uploaded application documents and metadata |
| Notifications | in-app notification entries |

## User-Specific Data

Business records such as applications, companies, contacts, tags, documents,
interviews and reminders are stored with a user reference. Backend code uses the
authenticated user to read and write these records.

## Migrations

Flyway migration files create and update the database schema in a reproducible
order. This allows the same schema to be used in local development, tests and
deployment.
