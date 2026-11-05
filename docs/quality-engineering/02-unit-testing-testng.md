# 02 — Unit testing with TestNG

This lesson starts from the real backend test in `backend/src/test/java/com/sporthub/pricing/PricingServiceTest.java`. The goal is not merely to run it; it is to understand what behavior it protects and how to improve it.

## 1. A unit, in plain English

A **unit** is a small piece of the program that can be checked by itself, commonly one class or one method. `PricingService` is a good unit because it accepts dates and a price, applies deterministic rules, and returns a result. It does not need Spring, a browser, an HTTP server or a database.

We test small units because a focused test is usually quick and tells us which rule broke. If child eligibility changes accidentally, an age-classification test can point directly to the classifying method instead of failing at the end of a long checkout journey.

## 2. TestNG vocabulary

- **TestNG** is the Java test framework configured in `backend/pom.xml`. It discovers test methods, runs setup/teardown hooks, supports parameterized examples, groups, and produces pass/fail reports.
- A **test method** is a Java method marked with `@Test`. The method calls production code and asserts its result.
- An **annotation** is metadata written with `@`, such as `@Test` or `@DataProvider`, that tells TestNG how to treat a method.
- An **assertion** is an explicit check. If the check is false, the test fails and TestNG reports expected and actual values.
- **Setup** prepares shared prerequisites before a test or group of tests. **Teardown** releases them afterward. TestNG offers `@BeforeMethod` / `@AfterMethod` for every test and `@BeforeClass` / `@AfterClass` once per class. `PublicUiSmokeTest` uses `@BeforeMethod` to create Chrome and `@AfterMethod` to always close it. The pricing test needs no setup because the class is pure Java.
- **Arrange–Act–Assert (AAA)** is a simple test shape: arrange the input, act by calling the method, assert the result.
- A **mock** is a controlled substitute for a dependency. It lets a test choose what the dependency returns or reports.

## 3. Read the age-boundary test

The real test supplies these examples through a TestNG data provider:

| DOB | Appointment date | Expected classification | Reason |
|---|---|---|---|
| 2008-11-02 | 2026-11-01 | `CHILD` | One day before the 18th birthday. |
| 2008-11-01 | 2026-11-01 | `ADULT` | Exactly age 18 on appointment day. |
| 1966-11-02 | 2026-11-01 | `ADULT` | One day before the 60th birthday. |
| 1966-11-01 | 2026-11-01 | `SENIOR` | Exactly age 60 on appointment day. |

The production implementation is [`PricingService.classify`](../../backend/src/main/java/com/sporthub/pricing/PricingService.java). It rejects null dates, rejects a birth date after the appointment, calculates completed years with `Period.between(...).getYears()`, and maps ages below 18 to `CHILD`, 60 or above to `SENIOR`, and the rest to `ADULT`.

The test method follows AAA:

1. **Arrange:** TestNG takes one DOB, appointment date and expected category from `ageBoundaries`.
2. **Act:** `LocalDate.parse` turns the input strings into dates, then the real service method runs.
3. **Assert:** `assertEquals(actual, expected)` compares the result with the table row. A mismatch fails the test with the two values.

`@DataProvider(name = "ageBoundaries")` avoids writing four nearly identical test methods. Each row is still an independent example. The two pairs around age 18 and age 60 are **boundary-value tests**: they check exact edges where a rule changes.

The requested example, DOB `2008-11-02` on appointment date `2026-11-01`, is one day short of age 18 and expects `PricingService.Classification.CHILD`. It runs as a local Java unit test; it does not need PostgreSQL, Spring Boot or Docker.

## 4. What the other pricing tests prove

`PricingServiceTest` now covers:

- A child receives 20% off, including currency rounding for `$19.99` (`$4.00` off, `$15.99` payable).
- An adult receives no demographic discount.
- A senior receives 15% off.
- Zero is a valid price, and a half-up rounding edge is handled.
- An appointment before birth, a negative base price and null required values are rejected.

The pricing test class has eight `@Test` methods. Its four `ageBoundaries` data rows invoke one method four times, producing 11 invocations; the two error-handler methods add two more. The backend unit suite therefore has 10 test methods and 13 TestNG invocations. These examples come from current code: facility pricing is implemented, but there is no coupon, product, cart or order-total Java service to test yet.

Money assertions compare `BigDecimal` values with explicit scale (`20.00`). `BigDecimal` avoids the small binary representation errors that can happen with floating-point types such as `double`.

## 5. Why use Mockito here?

Most pricing tests should **not** use a mock: there is no external collaborator to replace, and calling the real pure Java class makes the test meaningful.

[`GlobalExceptionHandlerTest`](../../backend/src/test/java/com/sporthub/common/GlobalExceptionHandlerTest.java) uses Mockito for a different reason. The handler reads a path from `HttpServletRequest`, which normally comes from a running web server. The test creates a mock request, tells it to return `/api/v1/checkout`, calls the handler directly, then asserts:

- The status is 500.
- The error code is `INTERNAL_ERROR`.
- The response contains the request path and a timestamp.
- The private exception detail is not exposed in the generic response message.

The mock keeps this a small unit test. A later HTTP/API test should separately check the actual server's error handling. A **mock** is not evidence that the real servlet container behaves correctly; it only gives this unit a controlled request object.

The difference between a mock and a real dependency:

| Mock dependency | Real dependency |
|---|---|
| Created by the test, returns only what the test configures. | The actual component runs, such as PostgreSQL or Chrome. |
| Fast and easy to control; helps isolate one unit. | Can expose wiring and integration defects. |
| May not behave like the real service. | Needs setup, data isolation and cleanup. |

## 6. TestNG versus JUnit 5

Both are established Java test frameworks. TestNG is the requested project framework and is already declared in the Maven dependencies.

| Topic | TestNG | JUnit 5 |
|---|---|---|
| Test method | `@Test` from `org.testng.annotations` | `@Test` from `org.junit.jupiter.api` |
| Parameterized data | `@DataProvider` | `@ParameterizedTest` with a source such as `@MethodSource` |
| Lifecycle | `@BeforeMethod`, `@AfterMethod`, `@BeforeClass` | `@BeforeEach`, `@AfterEach`, `@BeforeAll` |
| Groups | `groups = {"unit"}` | Tags such as `@Tag("unit")` |
| Spring TestContext | Supported through Spring's TestNG base class | Commonly used with JUnit Jupiter extensions |

They solve similar problems but have different annotations, providers and integrations. Do not mix annotations casually: a TestNG runner does not automatically execute a JUnit test, and vice versa. The backend Maven configuration explicitly selects the TestNG provider so a “successful build with zero tests” cannot be mistaken for a passing suite.

## 7. Run and interpret the unit suite

From `backend/`:

```sh
mvn test
```

To focus on the pricing class after TestNG provider dependencies are available:

```sh
mvn -Dtest=PricingServiceTest test
```

The current TestNG summary should report 13 invocations across pricing and error-response checks. The four data-provider rows count as four invocations. A failed assertion includes the test method and expected/actual values. A compilation or dependency-resolution error is different: the test did not run.

The reports are written under `backend/target/surefire-reports/`. Open the TestNG text or XML report to see each method and invocation. Coverage is generated during `mvn verify` under `backend/target/site/jacoco/`; coverage helps spot untouched code but is not a quality score by itself.

## 8. Exercise

Add a test case on paper for age 59 on the appointment date. State the DOB, expected classification and one assertion you would write. Then explain why that is a boundary check even though the age is not the exact cutoff of 60.
