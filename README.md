# Template Application — Backend

Spring Boot backend for the template application stack.

## Overview

This is the core business logic layer. It receives requests from multiple clients, validates the JWT on every protected request, and talks to the database.

- **From the BFF** — gRPC-Web requests with a Bearer token forwarded from Cloudflare Workers
- **From mobile/desktop clients** — direct HTTPS requests with a Bearer token from Auth0

Spring Boot validates JWTs the same way regardless of which client the request came from.

## Architecture

```
Cloudflare Workers — BFF
    │  gRPC-Web + Bearer token
    │
    ├──────────────────────────────────────┐
    │                                      │
    ▼                                      │ (future)
Spring Boot (Fly.io)         Mobile / Desktop clients
(Spring Modulith)                Bearer token (direct)
    │
    ├──► Supabase (Postgres)
    └──► Auth0 (JWKS — token validation only)
```

## Module Structure (Spring Modulith)

Modules are defined upfront and enforced by Spring Modulith:

| Module | Responsibility |
|---|---|
| `api` | gRPC-Web endpoint definitions, request handling |
| `domain` | Business logic, domain model |
| `auth` | Security config, JWT claims extraction |
| `infrastructure` | Database access, external integrations |

## Auth

Spring Boot acts as an OAuth 2.0 resource server. It validates the JWT on every protected request using Auth0's JWKS endpoint.

`application.yml` config:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://your-tenant.auth0.com/
          audiences: https://api.yourproject.com
```

Protect endpoints with standard Spring Security annotations:

```java
@PreAuthorize("isAuthenticated()")
```

### Roles & Permissions

Roles and permissions are defined in Auth0 and included in the JWT as a `permissions` claim. Enable **RBAC** and **Add Permissions in the Access Token** in Auth0 API settings.

```java
@PreAuthorize("hasAuthority('read:data')")
```

## Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot (Java) |
| Architecture | Spring Modulith |
| Build tool | Maven |
| API protocol | gRPC-Web |
| Database | Supabase (Postgres) |
| Auth | Auth0 (JWT validation via JWKS) |
| Hosting | Fly.io |

## Config

| Variable | Description |
|---|---|
| `AUTH0_ISSUER_URI` | e.g. `https://your-tenant.auth0.com/` |
| `AUTH0_AUDIENCE` | API identifier, e.g. `https://api.yourproject.com` |
| `DATABASE_URL` | Supabase Postgres connection string |

## Testing

| Type | Approach |
|---|---|
| Unit | JUnit 5 — domain logic, TDD |
| Integration | Testcontainers — real Postgres, no mocks |

Docker must be running locally for Testcontainers. Spring Boot 3+ has built-in support — minimal boilerplate.

## Setup

> Setup instructions to be added when the scaffold is built.

## Part of

See [template-application-planning](https://github.com/neilpmas/template-application-planning) for the full stack overview, architecture decisions, and project workflow.
