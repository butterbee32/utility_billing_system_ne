# System Architecture Diagram — Utility Billing System

## High-Level Architecture

```mermaid
flowchart TB
    subgraph clients [Clients]
        SW[Swagger UI]
        PM[Postman]
    end

    subgraph api [Spring Boot API Layer]
        AC[Auth Controller]
        UC[User Controller]
        CC[Customer Controller]
        BC[Bill Controller]
        PC[Payment Controller]
        NC[Notification Controller]
    end

    subgraph security [Security Layer]
        JWT[JWT Filter]
        SEC[Spring Security]
        BCrypt[BCrypt Encoder]
    end

    subgraph services [Service Layer]
        AS[Auth Service]
        OS[OTP Service]
        NS[Notification Dispatch]
        ES[Email Service]
        BS[Bill Service]
        PS[Payment Service]
    end

    subgraph data [Data Layer]
        JPA[Spring Data JPA]
        PG[(PostgreSQL)]
        TRG[DB Triggers]
    end

    SW --> api
    PM --> api
    api --> JWT
    JWT --> SEC
    api --> services
    services --> JPA
    JPA --> PG
    PG --> TRG
    services --> ES
```

## Module Dependencies

| Module | Depends On |
|--------|------------|
| Authentication | OTP Service, Email Service, Audit Log |
| User Management | Notification Dispatch, Email Service |
| Customer Management | OTP Service, Auth (self-register) |
| Bill Management | Tariff, Meter Reading, Notification Dispatch |
| Payment Management | Bill, Notification Dispatch |
| Notification | Email Service, PostgreSQL triggers |

## Security Architecture

- **Stateless JWT** — no server sessions
- **BCrypt** — all passwords hashed
- **Token blacklist** — logout invalidates tokens
- **Account locking** — 5 failed attempts → 15 min lock
- **OTP rate limiting** — max 3 requests per 10 minutes
- **Role-based access** — `@PreAuthorize` on all secured endpoints

## Deployment

```
PostgreSQL (utility_billing_db)
    ↑
Spring Boot App (port 8080)
    ├── Swagger UI (/swagger-ui.html)
    ├── Actuator (/actuator/health)
    └── File storage (uploads/)
```
