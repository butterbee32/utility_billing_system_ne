# Entity Relationship Diagram — Utility Billing System

## Overview

PostgreSQL relational database for WASAC/REG utility billing (water postpaid, electricity transitioning to postpaid).

## ERD (Mermaid)

```mermaid
erDiagram
    USERS ||--o{ USER_ROLES : has
    USERS ||--o| CUSTOMERS : links
    CUSTOMERS ||--|{ METERS : owns
    METERS ||--|{ METER_READINGS : records
    METER_READINGS ||--o| BILLS : generates
    CUSTOMERS ||--|{ BILLS : receives
    TARIFFS ||--|{ TARIFF_TIERS : contains
    TARIFFS ||--|{ BILLS : applies
    BILLS ||--|{ PAYMENTS : paid_by
    CUSTOMERS ||--|{ NOTIFICATIONS : receives
    OTP_TOKENS }o--|| USERS : verifies
    UPLOADED_FILES }o--|| USERS : profile
    UPLOADED_FILES }o--|| CUSTOMERS : documents
    AUDIT_LOGS }o--|| USERS : performed_by

    USERS {
        bigint id PK
        varchar full_names
        varchar email UK
        varchar phone_number
        varchar password
        varchar status
        boolean email_verified
        timestamp created_at
        varchar created_by
    }

    CUSTOMERS {
        bigint id PK
        varchar full_names
        varchar national_id UK
        varchar email UK
        varchar phone_number
        varchar address
        varchar status
        bigint user_id FK
    }

    METERS {
        bigint id PK
        varchar meter_number UK
        varchar meter_type
        date installation_date
        varchar status
        bigint customer_id FK
    }

    METER_READINGS {
        bigint id PK
        bigint meter_id FK
        decimal previous_reading
        decimal current_reading
        date reading_date
        int billing_month
        int billing_year
        decimal consumption
    }

    TARIFFS {
        bigint id PK
        varchar name
        varchar meter_type
        varchar tariff_type
        decimal flat_rate
        decimal fixed_service_charge
        decimal tax_rate
        decimal penalty_rate
        int version
        date effective_from
        date effective_to
        boolean active
    }

    TARIFF_TIERS {
        bigint id PK
        bigint tariff_id FK
        decimal min_consumption
        decimal max_consumption
        decimal rate_per_unit
    }

    BILLS {
        bigint id PK
        varchar bill_reference UK
        bigint customer_id FK
        bigint meter_reading_id FK
        bigint tariff_id FK
        int billing_month
        int billing_year
        decimal total_amount
        decimal paid_amount
        decimal outstanding_balance
        varchar status
    }

    PAYMENTS {
        bigint id PK
        bigint bill_id FK
        decimal amount_paid
        varchar payment_method
        date payment_date
        varchar payment_reference UK
    }

    NOTIFICATIONS {
        bigint id PK
        bigint customer_id FK
        text message
        varchar type
        boolean read
        timestamp created_at
    }

    OTP_TOKENS {
        bigint id PK
        varchar email
        varchar otp_code
        varchar type
        timestamp expires_at
        boolean used
    }

    UPLOADED_FILES {
        bigint id PK
        varchar stored_file_name
        varchar original_file_name
        varchar file_path
        varchar category
        varchar entity_type
        bigint entity_id
    }

    AUDIT_LOGS {
        bigint id PK
        varchar action
        varchar entity_type
        bigint entity_id
        text details
        varchar performed_by
        timestamp created_at
    }
```

## Key Constraints

| Rule | Implementation |
|------|----------------|
| Unique customer national ID / email | DB unique + service validation |
| One reading per meter/month/year | Unique constraint on `(meter_id, billing_month, billing_year)` |
| Tariff versioning | `version` column; previous tariffs deactivated on new effective date |
| Bill approval before payment | `BillStatus.APPROVED` check in `PaymentService` |
| Inactive customer/meter rules | Enforced in `BillService` and `MeterReadingService` |

## Database Triggers

1. **trg_bill_generated_notification** — After INSERT on `bills`, inserts notification row.
2. **trg_bill_paid_notification** — After UPDATE on `bills` when status becomes PAID and balance is zero.
