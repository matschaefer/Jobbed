# Security Notes

Status: Deployed portfolio project  
Last updated: July 2026

This document describes the main security-related implementation choices in
simple terms.

## Authentication

Jobbed uses Spring Security to protect backend endpoints. Users log in with an
email address and password. The backend returns an access token that the
frontend uses for protected API requests.

Passwords are hashed before they are stored.

## User-Specific Data

Application data is associated with the authenticated user. Backend services and
repository queries use the current user from the security context instead of
trusting a user id from the request body.

This keeps one user's applications, companies, contacts and documents separate
from another user's data.

## Email Verification and Password Reset

The backend supports:

- email verification after registration
- password reset through a token-based flow

In local development, emails are sent to Mailpit.

## Demo Mode

The public demo uses a dedicated `DEMO` role. A backend filter blocks
data-changing HTTP requests for that role. This allows visitors to explore the
application without changing or deleting sample data.

## API Protection

The backend validates request data, uses DTOs for API input/output and exposes a
consistent error response format. Protected endpoints require authentication.

## Configuration

Secrets such as database passwords, JWT secrets, SMTP credentials and optional
AI API keys are expected through environment variables. Example files are
included, but real secret values should not be committed.
