# QA test plan

This is the operational summary for the current repository. Detailed explanations live in the [Quality Engineering syllabus](../quality-engineering/README.md), and the canonical cases and evidence links live in the [case catalogue](../quality-engineering/test-cases/README.md) and [traceability matrix](../quality-engineering/requirements-to-tests.md).

## Scope and execution entry points

| Stage | Command | Prerequisites | Output/evidence |
|---|---|---|---|
| Backend unit (Surefire) | `cd backend && mvn test` | Java 21, Maven | `backend/target/surefire-reports/` |
| Backend integration (Failsafe included) | `cd backend && mvn verify` | Java 21, Maven, Docker/Testcontainers | `backend/target/failsafe-reports/`; PostgreSQL migrations and current health/security routes. |
| Frontend tests/build | `cd frontend && npm ci && npm test && npm run build` | Node/npm | Vitest output and Vite build. API calls in component tests are mocked. |
| External API/browser automation | Start PostgreSQL, backend, frontend; then `cd qa/automation && mvn test` | Java 21, Maven, Docker DB, Chrome | `qa/automation/target/surefire-reports/` |
| JMeter health workload | See [performance runbook](performance-testing.md) | Local backend, Apache JMeter | JTL and optional HTML report. |

The fast-to-broad learning order is useful for debugging, but CI runs backend, frontend and QA automation as separate jobs that can execute in parallel. Nightly workflow runs API regression and a low-scale health workload. Neither is a full booking/checkout pipeline.

## Current scope limits

**Confirmed:** pricing, generic error formatting, PostgreSQL schema/migration constraints, current HTTP health/security routes, and frontend UI behaviors have executable checks. **Blocked:** business API/system tests for accounts, bookings, products, cart, coupons, checkout and administration because the matching backend implementation and fixtures are absent. See the [API status](../API_IMPLEMENTATION.md) and case catalogue for precise gaps. Do not report a page mock or database constraint as a complete workflow test.

## Execution record

The dated evidence currently recorded in the implementation plan is an observation from 2026-11-05: 13 frontend tests, 12 backend unit invocations, 6 integration checks and 5 API/Selenium automation checks passed; the two health JMeter plans returned HTTP 200 for 1 and 25 samples; five feature plans returned HTTP 401 for all 10 samples each. This historical record is **not a current run** and must be refreshed with reports when suites change. Never infer PASS from compilation or JMeter exit code alone.
