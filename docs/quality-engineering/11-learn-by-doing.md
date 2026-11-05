# 11 — Learn by doing: run the SportHub test stages

This is the hands-on runbook for the lesson sequence in `docs/quality-engineering/`. It gives commands, explains what each stage checks, and shows what a meaningful pass/failure looks like.

## 1. What the current test suite can and cannot prove

Current executable checks cover:

- Java age classification, facility pricing, rounding and shared error formatting.
- The real PostgreSQL migrations, seed data, duplicate confirmed-slot constraint and Spring transaction rollback primitive.
- The public Spring health status and protected Springdoc route.
- React form/page behavior with mocked API data.
- Selenium checks for login form requirements, under-age client validation and the React admin route guard.
- A configurable JMeter workload against the health endpoint.

The backend does **not** currently implement registration/login, facility/product, booking, cart, coupon, checkout or administrator controllers/services. Therefore there is no honest automated end-to-end booking or purchase test to run yet. The feature cases in `docs/quality-engineering/test-cases/` specify those future checks and mark them blocked.

## 2. Prerequisites

| Tool | Used for | Needed when |
|---|---|---|
| Java 21 | Spring Boot and Maven test code | Backend unit/integration and QA automation. |
| Maven | Java builds and TestNG execution | Backend and `qa/automation`. |
| Node.js 20+ and npm | React/Vite | Frontend unit tests, dev server and build. |
| Docker with permission to use its daemon | PostgreSQL/Testcontainers | Backend integration tests and local database. |
| Chrome/Chromium | Selenium WebDriver | Browser UI tests. |
| Apache JMeter 5.6+ | Performance plans | JMeter smoke/load tests only. |

The backend and QA Maven configurations explicitly use the TestNG provider. A “BUILD SUCCESS” with zero discovered tests is not a pass; verify the report count. Maven may need network access the first time it downloads a dependency/provider.

## 3. Stage A — Backend unit tests

From `backend/`:

```sh
mvn test
```

### What happens

Surefire loads `src/test/resources/testng.xml`, selects the `unit` group and runs the real Java methods. `PricingServiceTest` covers the age/date and price rules. `GlobalExceptionHandlerTest` uses Mockito to supply a controlled servlet request and checks the generic error response.

This stage does not start Spring, PostgreSQL, Docker or a web server. The appointment-date example is a pure Java call to `PricingService.classify`, so it can run by itself on a local JDK.

### Expected result

The TestNG report should show 13 passing invocations (four data-provider age rows, the remaining pricing cases, and two error-handler tests), zero failures and zero errors. Open `backend/target/surefire-reports/` for detailed method results.

- **Passed:** actual and expected values match for every case.
- **Failed assertion:** inspect the expected/actual values and test input; a business rule may be wrong.
- **Compilation/dependency error:** no test ran, so this is not a test failure or pass.
- **Zero tests discovered:** fix runner/group configuration. Never count that as a passing test stage.

## 4. Stage B — Frontend component tests and build

From `frontend/`:

```sh
npm ci
npm test
npm run build
```

Vitest and React Testing Library render components in a simulated DOM. The API modules are mocked in `Pages.test.tsx`, so the tests check UI behavior for controlled responses, not server/database behavior.

Expected: the current `Pages.test.tsx` suite reports 15 tests passing; the production build completes TypeScript compilation and Vite bundling. A failed component assertion points to rendered page behavior. A build error may indicate a TypeScript/import/bundler problem even if the tests pass.

## 5. Stage C — PostgreSQL integration and embedded-server tests

From `backend/`:

```sh
mvn verify
```

This runs unit tests and then Failsafe tests whose class names end in `IT`. `PostgresApplicationIT` starts a temporary PostgreSQL 16 container through Testcontainers. The container is separate from the optional local Compose database; the test applies the real `database/migrations` scripts.

Expected: six integration/system checks pass: eight migrations and seed records, user-row save/read, rollback after an exception, duplicate confirmed booking rejected, health status `UP`, and the unauthenticated OpenAPI request is rejected with HTTP 401. Reports are under `backend/target/failsafe-reports/`; JaCoCo HTML is under `backend/target/site/jacoco/`.

Docker must be reachable. If Testcontainers fails before a test method starts, fix Docker permissions or availability; do not weaken the database assertions. The duplicate-slot case validates the SQL index, not a missing booking service.

## 6. Stage D — Start the app for external API and browser tests

### Terminal 1: disposable local PostgreSQL

From the repository root:

```sh
docker compose -f backend/compose.yaml up -d
```

This maps PostgreSQL to `localhost:5433`, database/user `sporthub`, local password `sporthub`.

### Terminal 2: Spring Boot

From the repository root:

```sh
cd backend
DATABASE_URL=jdbc:postgresql://localhost:5433/sporthub \
JWT_SIGNING_SECRET=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY= \
mvn spring-boot:run
```

Wait for Spring Boot to report startup on port `8080`. Flyway applies migrations when the app starts. Spring Security auto-configuration makes the basic Actuator health status public and protects `/v3/api-docs` by default. The backend has no project-specific authentication code yet.

### Terminal 3: React/Vite

From the repository root:

```sh
cd frontend
npm ci
npm run dev
```

Vite serves at `http://localhost:5173`. Booking/product pages may show API errors because their backend endpoints are not implemented; the login/register form and route guard tests do not need those APIs.

## 7. Stage E — REST Assured API smoke and sanity tests

With the backend running, from `qa/automation/`:

```sh
SYSTEM_BASE_URL=http://localhost:8080 mvn -Dgroups=api test
```

The API smoke test requests `/actuator/health` and asserts HTTP 200 plus status `UP`. The API sanity check confirms the generated `/v3/api-docs` route is protected by default Spring Security (HTTP 401 without credentials).

Expected: two TestNG API tests pass. A `401` from `/v3/api-docs` is expected under default Spring Security; a `401` from `/actuator/health` means the server security settings changed; connection refused means the server is not listening; a 500 health result suggests a backend/database problem. A pass verifies the operational API surface, not business endpoints.

## 8. Stage F — Selenium browser smoke and sanity tests

With Vite running and Chrome installed, from `qa/automation/`:

```sh
UI_BASE_URL=http://localhost:5173 BROWSER_HEADLESS=true mvn -Dgroups=ui test
mvn -Dgroups=sanity test
```

`ui` runs the login-page smoke case. `sanity` runs the Springdoc access-protection check plus the registration-under-age and customer/admin-route checks. Alternatively, `mvn test` runs all API, UI, smoke and sanity methods in the TestNG suite.

Each test creates a new Chrome driver and closes it in teardown. Explicit waits have a ten-second upper limit. When a test fails, inspect the Selenium exception and the `qa/automation/target/surefire-reports/` output; check that the Vite URL opens in the same browser environment.

These checks test the actual React pages but do not authenticate against a real SportHub account. End-to-end booking/checkout tests wait for backend services.

## 9. Stage G — JMeter smoke and low-scale load check

Install JMeter, keep the local backend running, then from the repository root run the smoke plan:

```sh
jmeter -n -t qa/performance/jmeter/health-smoke.jmx \
  -Jhost=localhost -Jport=8080 \
  -l /tmp/sporthub-health-smoke.jtl
```

Run the low-scale workload and create an HTML report:

```sh
jmeter -n -t qa/performance/jmeter/health-load-test.jmx \
  -Jhost=localhost -Jport=8080 \
  -Jthreads=5 -Jramp=5 -Jloops=5 \
  -l /tmp/sporthub-health-load.jtl -e -o /tmp/sporthub-health-report
```

Expected: no failed HTTP 200 assertions. If the JTL has errors, inspect status code and response message first. Review P95/P99 and throughput together. The report measures only Actuator health on this local environment; do not call it SportHub's user-facing capacity.

## 10. Stage H — CI workflow

On a push to `main` or pull request, GitHub Actions runs the same categories in separate jobs: backend unit/integration, frontend tests/build, and a live QA job that starts PostgreSQL, backend and frontend before REST Assured/Selenium. Reports upload even on a failed job. JMeter runs only in the separate scheduled nightly workflow at a small workload.

Open the Actions run and inspect the first failing job/step. A red test step blocks the combined check; a report artifact helps identify the failed method. Do not merge solely because one subproject compiles if another required job failed.

## 11. What to do when you want booking/checkout E2E coverage

Before adding those tests, implement and document the matching backend routes and service/repository logic. Agree request/response DTOs and status/error codes; add a deterministic fixture strategy; then add tests in this order:

1. Unit-test each business rule.
2. Integration-test database mappings, uniqueness, locks and rollback against PostgreSQL.
3. API-test positive, negative, authentication and boundary cases with REST Assured.
4. Add a small Selenium journey for register/login → book slot → checkout → order confirmation.
5. Add JMeter browse/slot/checkout plans using synthetic data and a safe reset plan.

The case catalogue and traceability matrix already provide the starting requirements, so tests can grow with real functionality rather than guessed endpoints.


## Test types in the repo

Think of a test as checking one boundary: **Arrange** the inputs and dependencies, **Act** by triggering the behavior, then **Assert** what can be observed. The wider the boundary, the more real components the test needs.

| Type | Where to find it | What it checks |
|---|---|---|
| **Backend unit tests** | [PricingServiceTest.java](../../backend/src/test/java/com/sporthub/pricing/PricingServiceTest.java) | Age cutoffs, facility discounts, money rounding, and invalid inputs. The real Java pricing class runs without Spring or a database. Four age examples use a TestNG data provider. |
| **Error-handler unit tests** | [GlobalExceptionHandlerTest.java](../../backend/src/test/java/com/sporthub/common/GlobalExceptionHandlerTest.java) | Error response shape, safe messages, and validation field details. The handler is real; request and validation objects are mocked. It does not prove Spring routes HTTP errors through the handler. |
| **Database/application integration tests** | [PostgresApplicationIT.java](../../backend/src/test/java/com/sporthub/integration/PostgresApplicationIT.java) | Spring Boot with a temporary PostgreSQL database: migrations and seeds, JDBC persistence, transaction rollback, duplicate confirmed-slot rejection, health, and OpenAPI access. The slot check is a direct, sequential database insert—not a booking API test. |
| **Frontend component tests** | [Pages.test.tsx](../../frontend/src/pages/Pages.test.tsx) | React forms, page states, filtering, cart display, and route guards. Components run in a simulated DOM; API responses are mocked, so these tests do not prove the backend endpoints work. |
| **External API tests** | [PlatformApiTest.java](../../qa/automation/src/test/java/com/sporthub/qa/api/PlatformApiTest.java) | REST Assured calls to a running server: health should return `200` and `UP`; unauthenticated Springdoc access currently returns `401`. |
| **Browser tests** | [PublicUiSmokeTest.java](../../qa/automation/src/test/java/com/sporthub/qa/ui/PublicUiSmokeTest.java) | Selenium drives Chrome to check login form requirements, under-age registration feedback, and a customer’s redirect away from the admin page. These are browser checks, not full sign-in or booking journeys. |
| **Performance plans** | [JMeter plans](../../qa/performance/jmeter) | Two plans exercise the implemented health route. Five feature plans target backend routes that do not exist yet; they are blocked templates, not product performance coverage. |

The added cases are explained step by step in [Additional implemented cases](../../docs/quality-engineering/test-cases/additional-implemented-cases.md): validation error details, rendering a returned product, and filtering facilities by search text. The [test-case catalogue](../../docs/quality-engineering/test-cases/README.md) links to the broader planned and blocked cases too.

## How to run them

**Backend unit tests** need Java 21 and Maven, but no database or Docker:

```sh
cd ../../backend
mvn test
```

**Backend integration tests** are included by `mvn verify`. They use Testcontainers, so Docker must be available:

```sh
cd ../../backend
mvn verify
```

**Frontend tests** need Node.js and npm. The build is a separate check: it type-checks and bundles the app, but does not run tests.

```sh
cd ../../frontend
npm ci
npm test
npm run build
```

**External API and browser tests** need PostgreSQL, the backend and frontend running, plus Chrome. Start the local database from the repository root:

```sh
cd /sporthub
docker compose -f backend/compose.yaml up -d
```

Then start the backend and frontend in separate terminals using the [runbook](../../docs/quality-engineering/11-learn-by-doing.md). Once they are available on ports 8080 and 5173:

```sh
cd ../../qa/automation
SYSTEM_BASE_URL=http://localhost:8080 \
UI_BASE_URL=http://localhost:5173 \
BROWSER_HEADLESS=true mvn test
```

To select a group, use `mvn -Dgroups=api test` or `mvn -Dgroups=ui test`. Stop the local database with `docker compose -f backend/compose.yaml down`; avoid `down -v` if you want to keep its data.

**JMeter health checks** need the backend running and JMeter installed. Run these from the repository root:

```sh
jmeter -n -t qa/performance/jmeter/health-smoke.jmx \
  -Jhost=localhost -Jport=8080 \
  -l /tmp/sporthub-health-smoke.jtl
```

```sh
jmeter -n -t qa/performance/jmeter/health-load-test.jmx \
  -Jhost=localhost -Jport=8080 \
  -Jthreads=5 -Jramp=5 -Jloops=5 \
  -l /tmp/sporthub-health-load.jtl \
  -e -o /tmp/sporthub-health-report
```

The load plan defaults to 25 requests. Check each JTL sample’s success flag, response code, and assertion result; JMeter’s exit code alone is not enough. The five facility, product, checkout, and concurrency plans target missing routes and depend on missing test-user/token setup. The recorded 2026-11-05 run returned `401` for those plans, so they should not be treated as meaningful feature tests.

## Current test logic and expected scope

The repository currently has 13 expected backend unit invocations, 6 integration test methods, 15 frontend tests, and 5 external API/browser test methods. These are counts from the current source, not a fresh run.

A useful mental model is:

```text
Pure Java rules ──┐
                  ├─ PostgreSQL / Spring checks ── HTTP checks
Mocked React UI ──┘                              └─ Chrome UI checks

Health endpoint ── JMeter smoke / small workload
```

Unit and component tests are quicker and isolate one behavior. Integration, API, and browser tests include more real components, so failures can have more possible causes. For example, the React cart test can show that the page displays a mocked total; only a future live checkout API test could establish that the server calculates and persists the correct total.

The CI workflow runs backend, frontend, and QA automation as separate jobs; they can run in parallel. The [CI lesson](../../docs/quality-engineering/08-cicd.md) and the [full runbook](../../docs/quality-engineering/11-learn-by-doing.md) explain reports, prerequisites, and failure interpretation.