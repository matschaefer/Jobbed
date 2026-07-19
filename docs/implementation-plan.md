# Implementation Notes

Status: Deployed portfolio project  
Last updated: July 2026

This file summarizes the implemented project areas at a high level. It replaces
the earlier phase-based roadmap document.

## Implemented Areas

- Angular frontend with public pages, authentication pages and protected app
  routes
- Spring Boot REST API with user-specific data access
- PostgreSQL database with Flyway migrations
- Registration, login, email verification and password reset flow
- Application management with status history
- Kanban board for application status tracking
- Company and contact management
- Interviews, reminders and in-app notifications
- Document upload and download
- Dashboard statistics
- Job description analysis with rule-based fallback
- Resume builder
- Read-only demo mode for public portfolio access
- Docker-based local setup
- GitHub Actions checks for backend, frontend, Docker and E2E tests

## Notes

The project is intentionally built as a single fullstack application rather than
a distributed system. This keeps the architecture understandable for a portfolio
project while still showing frontend, backend, database, testing and deployment
skills.
