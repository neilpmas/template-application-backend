# Template Application — Backend

Spring Boot backend for the template application stack.

## Overview

This is the core business logic layer. It receives requests from the BFF (Cloudflare Workers), validates the JWT on every protected request, and talks to the database.

It never handles auth directly — it trusts the BFF to authenticate the user and forward a valid Bearer token.

## Architecture

```
Cloudflare Workers — BFF
    │
    │  Authorization: Bearer <token>
    ▼
Spring Boot (Fly.io)
    │
    ├──► Supabase (Postgres)
    └──► Auth0 (JWKS — token validation only)
```

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

Roles and permissions are defined in Auth0 and included in the JWT as a `permissions` claim. Enable **RBAC** and **Add Permissions in the Access Token** in the Auth0 API settings.

Use the `permissions` claim for fine-grained access control:

```java
@PreAuthorize("hasAuthority('read:data')")
```

## Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot (Java) |
| Database | Supabase (Postgres) |
| Auth | Auth0 (JWT validation via JWKS) |
| Hosting | Fly.io |

## Config

| Variable | Description |
|---|---|
| `AUTH0_ISSUER_URI` | e.g. `https://your-tenant.auth0.com/` |
| `AUTH0_AUDIENCE` | API identifier, e.g. `https://api.yourproject.com` |
| `DATABASE_URL` | Supabase Postgres connection string |

## Setup

> Setup instructions to be added when the scaffold is built.

## Part of

See [template-application-planning](https://github.com/neilpmas/template-application-planning) for the full stack overview and architecture decisions.
