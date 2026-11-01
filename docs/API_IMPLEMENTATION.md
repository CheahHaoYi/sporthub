# API implementation status

The required route contract is documented in `docs/contracts/PROJECT_CONTRACT.md`. At present no business route is implemented: the database schema documentation and SQL migrations are still placeholders, so adding JPA mappings or returning fabricated domain data would create an incompatible API. The Spring Boot 3 application, environment-based PostgreSQL/Flyway configuration (`ddl-auto=validate`), TestNG/Surefire/Failsafe configuration, Actuator and common validation error envelope are in place.

## Required route groups

`/api/v1/auth/*`, `/me`, `/customer-profiles/*`, `/sports`, `/facilities/*`, `/products/*`, `/cart/*`, `/checkout`, `/orders/*`, `/bookings/*`, and `/admin/*` are contract requirements and await database/API schema integration. Request/response shapes, role access, decimal-string pricing, idempotency, and HTTP errors must continue to follow `docs/contracts/PROJECT_CONTRACT.md`.

## Error response

Validation errors use timestamp, HTTP status, stable error code, safe message, request path, and field-error list. Unexpected exceptions return a generic 500 response; stack traces must remain in server logs only.

## Deployment dependency

Local Flyway path is `../database/migrations` from `backend/`. Packaged deployments must mount database-owned migrations and set `DATABASE_MIGRATION_LOCATION` to their filesystem location. No backend migration is created or copied here.
