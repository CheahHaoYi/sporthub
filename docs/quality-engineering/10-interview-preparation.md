# 10 — Junior Quality Engineer interview preparation

For each question, first understand what the interviewer is checking. Then practice a concise answer in your own words and support it with a SportHub example.

## 1. What is the difference between unit and integration testing?

**Evaluates:** Whether you understand test scope and dependency control.

**Beginner explanation:** A unit test checks one small rule alone. An integration test checks that connected parts work together.

**Interview answer:** “A unit test isolates a small unit of behavior and is usually fast and deterministic. An integration test crosses a component boundary using real collaborators, such as a service and a database, to catch wiring, mapping, SQL, or transaction issues. I use mocks to isolate units, but keep important boundaries real in integration tests.”

**SportHub example:** `PricingServiceTest` calls a Java pricing method directly. `PostgresApplicationIT` applies the real Flyway migrations and checks PostgreSQL rejects a duplicate confirmed booking.

## 2. Integration testing versus system testing?

**Evaluates:** Whether you can distinguish component collaboration from application-wide behavior.

**Beginner explanation:** Integration testing joins selected pieces. System testing checks a whole feature/application against requirements.

**Interview answer:** “Integration tests focus on selected boundaries, for example Java-to-PostgreSQL. System tests exercise a complete feature through an external interface with its relevant dependencies configured. The terms can overlap; I state which components are real and what the test is intended to prove.”

**SportHub example:** Inserting two bookings and seeing the PostgreSQL constraint reject the second is a database integration check. After booking API exists, verifying the customer receives a conflict and only one booking persists is a system/API test.

## 3. API testing versus UI testing?

**Evaluates:** Whether you select the right test boundary and know what each misses.

**Beginner explanation:** API tests send requests directly to the server. UI tests interact as a browser user.

**Interview answer:** “API tests are usually faster and give precise contract feedback about status, headers and response data. UI tests validate rendered behavior and user interaction, but are slower and more sensitive to browser/UI changes. I cover business variations at the API/unit layers and keep a smaller number of high-value browser journeys.”

**SportHub example:** REST Assured checks `/actuator/health`; Selenium checks required fields on `/login`. Neither currently proves a booking journey because its backend route is missing.

## 4. What is TestNG, and how does it compare with JUnit 5?

**Evaluates:** Familiarity with the project's Java test framework and execution lifecycle.

**Beginner explanation:** TestNG and JUnit run Java tests, provide assertions, setup/cleanup and test reports. Their annotations and integrations differ.

**Interview answer:** “This project uses TestNG with `@Test`, `@DataProvider`, `@BeforeMethod` and test groups. JUnit 5 provides analogous annotations such as `@ParameterizedTest`, `@BeforeEach` and tags. I would keep one runner consistent with the build configuration rather than mixing annotations. In this project, Surefire/Failsafe explicitly select the TestNG provider.”

**SportHub example:** A data provider supplies age-boundary inputs to `PricingServiceTest`; `testng-it.xml` selects integration tests.

## 5. What is Selenium WebDriver? Why use explicit waits and Page Objects?

**Evaluates:** Whether you understand browser automation and test stability.

**Beginner explanation:** Selenium tells a real browser to open pages, find controls and interact with them. Explicit waits wait for a condition; Page Objects keep selectors/actions organized.

**Interview answer:** “WebDriver automates a browser through a browser driver. I use stable accessible selectors or test IDs and wait for observable conditions instead of fixed sleeps. A Page Object centralizes page selectors and actions, while tests state the expected user behavior. I create and quit a driver per test to reduce shared state.”

**SportHub example:** `LoginPage` wraps `data-testid` locators; `PublicUiSmokeTest` waits for the page title and quits Chrome in `@AfterMethod`.

## 6. What does a JMeter test tell you?

**Evaluates:** Understanding of workload and performance evidence.

**Beginner explanation:** JMeter simulates concurrent request loops and reports time, throughput and failures.

**Interview answer:** “I define the workload model first, then ramp virtual users gradually, assert responses are valid, and examine throughput, error rate, median and tail percentiles alongside application/database resource metrics. I don't label a virtual-user count as capacity without a representative workload and measured environment.”

**SportHub example:** The new JMeter plan sends a small configurable load to `/actuator/health`; it validates the tool and endpoint, but it cannot establish booking or checkout capacity.

## 7. How would you test a booking application?

**Evaluates:** Requirement analysis, risk prioritization and test-layer choices.

**Beginner explanation:** Check available, occupied and past slots, customer profile eligibility, prices, duplicate requests and cancellation.

**Interview answer:** “I would derive cases from the booking rules, start with unit tests for age and validation boundaries, use PostgreSQL integration tests for persistence and uniqueness/transaction behavior, API tests for status/error/access contracts, and a small set of E2E journeys. I would include valid, invalid, boundary and concurrent attempts, using isolated future slots and synthetic profiles.”

**SportHub example:** The contract says slots have capacity one. The database test proves the unique index rejects the second confirmed row; the full service/API journey is still a gap until booking endpoints are implemented.

## 8. How would you prevent two users from booking the same slot?

**Evaluates:** Whether you recognize race conditions and defense at the correct layer.

**Beginner explanation:** Two requests can check “free” at nearly the same time. The system needs a database-safe rule, not just a button disabled in the browser.

**Interview answer:** “I would recheck availability inside a transaction while acquiring the appropriate database lock or using a conditional write, then rely on a database uniqueness/capacity constraint as a final invariant. I would test two independent requests synchronized at the boundary and assert one winner, a conflict for the other, and one confirmed row.”

**SportHub example:** V6 has a partial unique index on `facility_slot_id` for confirmed bookings. The current integration test exercises it sequentially; a concurrent API race test waits on the missing booking service.

## 9. How would you test a discount calculation?

**Evaluates:** Boundary analysis, arithmetic assertions and data-driven testing.

**Beginner explanation:** Pick examples for each customer age category, exact birthday edges, normal prices, rounding and invalid inputs.

**Interview answer:** “I would test the calculation as a pure unit where possible: age at appointment date, each eligible rate, zero/no-discount, currency rounding, negative values and null/invalid dates. I would use exact decimal assertions and independently verify the expected math. Then I'd add integration/API tests to ensure checkout uses server-calculated values.”

**SportHub example:** `PricingServiceTest` checks 20% child and 15% senior pricing plus adult rate and 18/60 boundaries. Coupon and checkout integration are not available yet.

## 10. How do you investigate an API that occasionally returns HTTP 500?

**Evaluates:** Evidence collection, reliability thinking and defect triage.

**Beginner explanation:** Record when it fails, what request was sent, and what was happening at the server; try to reproduce it.

**Interview answer:** “I would capture a correlation/request ID, timestamp, sanitized request shape, response body, rate/frequency and environment. I would compare server logs, database timeouts, dependency health and concurrent traffic, then try controlled repeated requests. I would distinguish a valid application exception from a test-data, environment or test-harness failure and create a reproducible defect report.”

**SportHub example:** Use the standard error `code` and `path` in `ApiErrorResponse`, health endpoint and backend logs. Avoid leaking passwords or customer data into a report.

## 11. How would you investigate slow checkout?

**Evaluates:** Performance diagnosis without jumping to conclusions.

**Beginner explanation:** Measure the request stages and look for the slow part—database, code, payment simulation or network.

**Interview answer:** “I would define the workload, capture response-time percentiles and throughput, trace the checkout request by stage, and inspect database queries/locks, connection-pool waits, app threads, CPU and external dependency time. I would reproduce with representative synthetic carts and adjust one variable at a time.”

**SportHub example:** Current checkout code is absent, so first make the endpoint and transaction measurable. Existing JMeter health load is not a checkout benchmark.

## 12. What belongs in unit tests versus integration tests?

**Evaluates:** Ability to balance speed and realistic dependencies.

**Beginner explanation:** Put a local rule in a unit test. Put a collaboration or storage rule in an integration test.

**Interview answer:** “Pure calculations and boundary decisions belong in fast unit tests. Database mappings, migrations, SQL constraints and transactions belong in integration tests with a real PostgreSQL dependency. I mock external collaborators in a unit test when isolation helps, but avoid mocking the database in a test whose purpose is to validate persistence.”

**SportHub example:** Age classification is unit-tested with TestNG. Flyway schema, unique booking constraint and rollback are tested through Spring TestContext plus PostgreSQL Testcontainers.

## 13. What makes a useful bug report?

**Evaluates:** Communication and reproducibility.

**Beginner explanation:** Another person should be able to follow the steps and see the same problem.

**Interview answer:** “I include a concise title, build/environment, preconditions, synthetic data, exact steps/request, expected and actual outcomes, frequency, impact and relevant privacy-safe logs/screenshots. I state uncertainty rather than guessing at the root cause.”

**SportHub example:** Report the two slot-booking requests, timestamps, returned statuses and final confirmed-row count, rather than only writing ‘booking broken.’

## 14. Exercise

Practice the answer to question 8 in under one minute. Include both an implementation safeguard and a test that proves the safeguard under concurrency.
