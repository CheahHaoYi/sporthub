# SportHub Quality Engineering syllabus

This is an interactive learning path from repository orientation to junior QE interview practice. The lessons use real files and clearly label scenarios that await missing backend business APIs.

| Phase | Lesson | Main outcome |
|---|---|---|
| 0 | [Codebase overview](00-codebase-overview.md) | Understand the actual frontend, backend, database, tools and implementation gaps. |
| 1 | [Testing fundamentals](01-testing-fundamentals.md) | Explain test purpose, levels, concepts and the test pyramid. |
| 2 | [Unit testing with TestNG](02-unit-testing-testng.md) | Read and extend isolated Java rule tests with TestNG and Mockito. |
| 3 | [Integration testing](03-integration-testing.md) | Test PostgreSQL migrations, constraints, Spring transactions and application wiring. |
| 4 | [API testing](04-api-testing.md) | Read HTTP requests and assert the real API surface using curl/REST Assured. |
| 5 | [System and E2E testing](05-system-and-e2e-testing.md) | Exercise the current frontend in Chrome and understand why full booking journeys are blocked. |
| 6 | [Performance testing with JMeter](06-performance-testing-jmeter.md) | Run a small repeatable health workload and interpret its limits. |
| 7 | [Test design](07-test-design.md) | Derive tests from requirements and link them to cases and risks. |
| 8 | [CI/CD](08-cicd.md) | Understand the GitHub Actions quality gates and reports. |
| 9 | [Debugging and reporting](09-debugging-and-reporting.md) | Triage a failed test and create a reproducible defect report. |
| 10 | [Interview preparation](10-interview-preparation.md) | Practice junior QE questions with accurate SportHub examples. |
| 11 | [Learn by doing runbook](../../doc/quality-engineering/11-learn-by-doing.md) | Start services and execute each runnable test stage. |

Feature cases live in [`test-cases/`](test-cases/), and the [requirements-to-tests matrix](requirements-to-tests.md) records coverage and gaps.

## Implemented test code map

| Layer | Code | What it currently checks |
|---|---|---|
| Java unit | `backend/src/test/java/com/sporthub/pricing/PricingServiceTest.java` | Age boundaries, age-based facility discounts, money rounding, null/invalid values. |
| Java unit + Mockito | `backend/src/test/java/com/sporthub/common/GlobalExceptionHandlerTest.java` | Generic error response shape and path using a mock servlet request. |
| PostgreSQL/Spring integration | `backend/src/test/java/com/sporthub/integration/PostgresApplicationIT.java` | Flyway schema/seed, JDBC persistence, unique confirmed-slot constraint, transaction rollback, public health status and HTTP 401 from the protected OpenAPI route. |
| React/Vitest | `frontend/src/pages/Pages.test.tsx` | Forms, mocked API states, cart totals, coupon errors and route guards. |
| REST Assured/TestNG | `qa/automation/src/test/java/com/sporthub/qa/api/PlatformApiTest.java` | Public health status and default protection of the generated OpenAPI route on a running server. |
| Selenium/TestNG | `qa/automation/src/test/java/com/sporthub/qa/ui/PublicUiSmokeTest.java` | Login form fields, client-side age rejection and customer-to-admin route redirect. |
| JMeter | `qa/performance/jmeter/health-smoke.jmx`, `health-load-test.jmx` | Low-scale health endpoint smoke/load plan; not booking/checkout capacity. |
