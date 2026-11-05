# Test data guide

## Data used by current automated tests

| Layer | Data strategy | Isolation and limit |
|---|---|---|
| Pricing unit tests | Fixed ISO dates and explicit `BigDecimal` values in [`PricingServiceTest`](../../backend/src/test/java/com/sporthub/pricing/PricingServiceTest.java). | Deterministic; no external data store. |
| Error-handler unit test | Mock servlet request with a controlled path and exception. | The request is a mock; no server is involved. |
| PostgreSQL integration | Fresh PostgreSQL 16 Testcontainer; real migrations and seed records, plus generated unique account email. | Container is discarded after the suite; not the developer's Compose database. Some assertions use seeded rows. |
| React/Vitest | API/AuthContext modules are mocked with controlled results in [`Pages.test.tsx`](../../frontend/src/pages/Pages.test.tsx). | In-memory DOM and mock responses; no live backend or database. |
| REST Assured/Selenium | Health/security routes and synthetic browser form values. | Requires running local services; the UI checks do not create a persisted account. |
| JMeter health | No user fixture; calls `/actuator/health`. | Only operational health path. |

## Synthetic data rules

- Use reserved `.example.test` email addresses for local or automated examples.
- Do not put real passwords, access tokens, customer details or production records in source control, test reports, screenshots or examples.
- Keep database integration data disposable and isolated. Do not point destructive setup/reset steps at a shared or production database.
- Avoid relying on current time in tests unless the test explicitly controls or states the date.

## Data that is not available yet

There is no committed `qa/performance/test-users.csv`, implemented login API that returns tokens, or deterministic business-service fixture/reset mechanism. Five feature JMeter plans refer to the missing CSV and send `Authorization: Bearer ${token}`; their configured token defaults empty. Do not create fake credentials or claim those plans exercise feature behavior. Before enabling them, implement stable endpoints, provision synthetic accounts/data, populate token setup, validate request bodies and assert the intended business result. See the [performance runbook](performance-testing.md).
