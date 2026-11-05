# 03 — Integration testing

An integration test checks that two or more real parts work together. The unit lesson isolated `PricingService`; this lesson connects Spring Boot, Flyway SQL, JDBC and a real PostgreSQL database.

## 1. What integration testing means here

Suppose Java code appears to save a booking correctly. A unit test with a mocked repository can prove the service called `save`, but it cannot prove that:

- The SQL table exists under the expected name.
- PostgreSQL accepts the column types and values.
- A foreign key points to a real profile.
- The unique confirmed-slot index rejects a duplicate.
- A transaction rolls back partially written data.

Those questions cross component boundaries, so they need integration evidence. A good integration test uses real dependencies for the boundary it is checking and mocks only unrelated systems.

## 2. The database integration tests in this repository

[`PostgresApplicationIT`](../../backend/src/test/java/com/sporthub/integration/PostgresApplicationIT.java) has six `@Test` methods and uses:

- `@SpringBootTest` to start the actual Spring Boot application on a random local port.
- `AbstractTestNGSpringContextTests` so Spring's test context and dependency injection work with TestNG.
- `PostgreSQLContainer` to start a disposable PostgreSQL 16 database.
- `@DynamicPropertySource` to give Spring the container's URL and credentials before the context starts.
- The actual SQL migrations in `database/migrations/`.
- `JdbcTemplate` to issue focused SQL against the resulting schema.

Testcontainers is a library that asks a local container runtime (Docker) to create temporary dependencies for tests. This avoids reusing a developer's application database and gives each run a fresh schema. The container is started from Spring's dynamic property callback and is stopped by the TestNG suite teardown; it does not depend on JUnit's `@Container` extension.

`src/test/resources/docker-java.properties` selects Docker API 1.44 for the Testcontainers client. This avoids an older client default that the current Docker Engine rejects; revisit the override when upgrading Testcontainers/docker-java.

The tests check:

1. Flyway applied the eight versioned migrations and the demo facility/product data exists.
2. A test account can be inserted and queried from PostgreSQL.
3. A second confirmed booking for the same seeded slot is rejected by the real partial unique index in V7.
4. A Spring `TransactionTemplate` rolls back a user row when a later operation throws.
5. The embedded application reports public health status `UP` over HTTP.
6. Spring Security rejects unauthenticated access to the generated OpenAPI route with HTTP 401.

The duplicate-booking check validates the database guard, not a complete booking business service. There is no backend booking service yet. The rollback check validates Spring transaction behavior with PostgreSQL, not checkout rollback policy, because checkout is not implemented.

## 3. Test database, isolation and rollback

A **test database** is a separate database created for tests, populated with test-specific data, and discarded or reset afterward. Never run destructive integration tests against production data. These tests use a disposable PostgreSQL container, not the database a user may run for local development.

**Test isolation** means each test can run without depending on a previous test's records or execution order. The test uses generated unique account emails and fresh slots from the migrations. The duplicate booking case is wrapped in a Spring test transaction, which is rolled back after the test.

**Transaction rollback** means a group of database operations is treated as one unit: if an operation fails before commit, earlier changes in the transaction are undone. This is essential in checkout. For example, if an order row is inserted but simulated payment fails, the order, booking, coupon redemption and stock update should all roll back together. The current test proves the Spring/PostgreSQL mechanism with a user row; it does not claim to prove a checkout workflow.

## 4. Why PostgreSQL instead of H2?

H2 is an in-memory Java database that can be convenient for fast tests. It does not behave identically to PostgreSQL. SportHub's schema uses PostgreSQL-specific behavior such as partial unique indexes (`WHERE booking_status = 'CONFIRMED'`), expression indexes (`LOWER(email)`), PostgreSQL `TIMESTAMPTZ`, SQL functions and `ON CONFLICT`. An H2 test might pass even if the PostgreSQL migration or constraint fails.

Use H2 only when the test is deliberately checking database-independent logic and its limitations are understood. Use PostgreSQL for migrations, SQL, indexes, locking, transactions and concurrency tests.

## 5. Spring Boot TestContext

Spring Boot Test starts selected application components with test configuration. Spring's **TestContext** manages application contexts and lifecycle callbacks so tests can inject real beans. In this project, the context starts an embedded web server and reads the real `application.yml`; the test supplies a disposable datasource and observes Spring Security auto-configuration on the current endpoints.

The current application has no JPA entities or repositories, even though `spring-boot-starter-data-jpa` is in the Maven file. Therefore these integration tests use JDBC to test SQL and transaction behavior directly. Once entities/repositories are implemented, add a repository test that saves an entity and reads it back through the repository API.

## 6. Run the integration suite

From `backend/`:

```sh
mvn verify
```

This runs unit tests first, then Failsafe integration tests ending in `*IT.java`. Docker must be available so Testcontainers can start PostgreSQL. TestNG includes the `integration` group using `src/test/resources/testng-it.xml`. If the container cannot start, the result is an environment failure, not a failed application assertion.

Successful output should show the TestNG integration methods passing and Failsafe reporting no integration failures. A duplicate booking test failure should include the expected `DataIntegrityViolationException`; a Flyway failure should be investigated in the migration or database startup logs.

## 7. Planned integration cases that need backend features

These are important future tests, but cannot be implemented meaningfully until the matching production layers exist:

- Save and retrieve a user through a Spring Data repository (today we can test the table with JDBC only).
- Create a booking through a service and verify persistence.
- Reject conflicting bookings through the service/API and verify only one confirmed booking exists.
- Apply age/coupon discounts through cart pricing and checkout.
- Simulate payment failure and verify all checkout writes roll back.
- Race two independent transactions at the same time and prove only one booking or last-unit purchase succeeds.

The test catalogue in Phase 7 records these as blocked-by-implementation instead of pretending that schema-only checks prove the full workflows.
