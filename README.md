# utility_billing_system_ne

WASAC/REG Utility Billing System — a Spring Boot REST API for managing utility customers, meters, billing, payments, tariffs, and notifications.

## Tech Stack

- **Java 17**
- **Spring Boot 3.5** (Web, Security, Data JPA, Validation, Mail, Actuator)
- **PostgreSQL**
- **JWT** authentication
- **Swagger / OpenAPI** (`/swagger-ui.html`)

## Features

- User & staff management with role-based access
- Customer registration (admin-created and self-register with OTP)
- Meter readings and tariff-based bill generation
- Payment recording and bill status updates
- Email notifications (OTP, password reset, billing alerts)
- Audit logging and file uploads
- PostgreSQL triggers for bill notification events

## Prerequisites

- JDK 17
- Maven 3.9+
- PostgreSQL (database: `utility_billing_db`)

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/butterbee32/utility_billing_system_ne.git
cd utility_billing_system_ne
```

### 2. Configure application properties

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

Edit `application.properties` with your database credentials, JWT secret, and mail settings.

### 3. Create the database

```sql
CREATE DATABASE utility_billing_db;
```

### 4. Run the application

```bash
mvn spring-boot:run
```

The API starts on **http://localhost:8080**.

### 5. Explore the API

- Swagger UI: http://localhost:8080/swagger-ui.html
- API docs: http://localhost:8080/api-docs
- Health check: http://localhost:8080/actuator/health

A default admin user is seeded on first run (configure via `app.admin.*` properties).

## Project Structure

```
src/main/java/rw/gov/utility_billing_system/
├── config/        # Security, Swagger, seeders
├── controller/    # REST endpoints
├── dto/           # Request/response objects
├── entity/        # JPA entities
├── service/       # Business logic
├── repository/    # Data access
├── security/      # JWT & authentication
└── utility/       # Email, file storage, validators
```

## Documentation

Additional docs are in the `docs/` folder:

- [Architecture](docs/ARCHITECTURE.md)
- [API Testing Guide](docs/API_TESTING_GUIDE.md)
- [Testing Reference](docs/TESTING_REFERENCE.md)
- [ERD](docs/ERD.md)
- [Flow Diagram](docs/FLOW_DIAGRAM.md)

## Build & Test

```bash
mvn clean package
mvn test
```

## License

This project is for educational / institutional use (WASAC/REG).
