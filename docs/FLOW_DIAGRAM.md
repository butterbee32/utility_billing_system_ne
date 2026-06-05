# Spring Boot Flow Diagram — Utility Billing System

## Architecture Layers

```
Client (Postman / Swagger)
        │
        ▼
┌───────────────────────────────────────────────────────────┐
│  Controller Layer (REST + @PreAuthorize role checks)    │
│  Auth, User, Customer, Meter, Reading, Tariff, Bill,    │
│  Payment, Notification, File, AuditLog                  │
└─────────────────────────┬─────────────────────────────────┘
                          │
                          ▼
┌───────────────────────────────────────────────────────────┐
│  Service Layer (business rules + transactions)            │
└─────────────────────────┬─────────────────────────────────┘
                          │
          ┌───────────────┼───────────────┐
          ▼               ▼               ▼
   Repository         Mapper/DTO      Utilities
   (Spring Data JPA)  (no entities    (Email, OTP, File,
                       exposed)        TariffCalculator)
                          │
                          ▼
                   PostgreSQL + Triggers
```

## Request Flow (Authenticated API)

```mermaid
sequenceDiagram
    participant C as Client
    participant F as JwtAuthenticationFilter
    participant S as SecurityConfig
    participant Ctrl as Controller
    participant Svc as Service
    participant Repo as Repository
    participant DB as PostgreSQL

    C->>Ctrl: HTTP Request + Bearer JWT
    Ctrl->>F: Filter chain
    F->>F: Validate JWT, check blacklist
    F->>S: Set SecurityContext
    Ctrl->>Ctrl: @PreAuthorize role check
    Ctrl->>Svc: DTO request
    Svc->>Svc: Validate business rules
    Svc->>Repo: Entity operations
    Repo->>DB: SQL / triggers
    DB-->>Repo: Result
    Repo-->>Svc: Entity
    Svc-->>Ctrl: Response DTO
    Ctrl-->>C: JSON response
```

## Authentication Flow

```mermaid
flowchart TD
    A[Register] --> B[Save user INACTIVE]
    B --> C[Generate OTP + email]
    C --> D[Verify OTP]
    D --> E[Activate user ACTIVE]
    E --> F[Login]
    F --> G[BCrypt authenticate]
    G --> H[Issue JWT]
    H --> I[Access secured APIs]

    J[Forgot Password] --> K[OTP email]
    K --> L[Reset Password]
    L --> M[Update BCrypt hash]

    N[Logout] --> O[Blacklist JWT token]
```

## Billing Flow

```mermaid
flowchart TD
    A[Operator captures meter reading] --> B{Meter active?}
    B -->|No| X[Reject]
    B -->|Yes| C{Current > Previous?}
    C -->|No| X
    C -->|Yes| D{One per month/year?}
    D -->|No| X
    D -->|Yes| E[Save MeterReading]

    E --> F[Generate Bill]
    F --> G{Customer active?}
    G -->|No| X
    G -->|Yes| H[Apply versioned tariff]
    H --> I[Calculate amounts + VAT]
    I --> J[Save Bill PENDING_APPROVAL]
    J --> K[DB Trigger: notification]
    K --> L[Email bill notification]

    L --> M[Admin/Finance approves]
    M --> N[Bill APPROVED]
    N --> O[Record payment partial/full]
    O --> P{Balance = 0?}
    P -->|Yes| Q[Bill PAID + DB trigger notification]
    P -->|No| R[Update outstanding balance]
```

## Role Access Summary

| Module | ADMIN | OPERATOR | FINANCE | CUSTOMER |
|--------|-------|----------|---------|----------|
| Tariffs | CRUD | — | Read | — |
| Meter readings | CRUD | Create/Read | Read | — |
| Bills | Generate/Approve | — | Generate/Approve | Read own |
| Payments | CRUD | — | Create/Read | Read |
| Customers | CRU | CRU | Read | Self-register + Read |
