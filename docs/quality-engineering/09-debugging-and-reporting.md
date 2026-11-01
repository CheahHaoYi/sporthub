# 09 — Debugging test failures and reporting defects

## 1. Read the result before changing code

When a test fails, first identify:

1. Which test method or test case failed?
2. What was expected?
3. What happened instead?
4. Did the test reach the assertion, or fail during setup/compilation/network access?
5. Can the same failure be reproduced with the same data and environment?

An **assertion failure** means the test ran and its comparison was false. A **stack trace** is the call sequence recorded at the failure; start at the exception and locate the first relevant project line, then read outward to understand who called it.

## 2. Failure, broken test, defect, environment or data?

- **Failed test:** The test ran and the application did not meet its expected result.
- **Broken test:** The test has an incorrect expectation, selector, setup or assumption. For example, expecting 500 for a healthy Actuator route would be a broken expectation.
- **Application defect:** Production behavior violates a valid requirement. Example: `classify` returns `ADULT` one day before the 18th birthday.
- **Environment issue:** Required software/service is unavailable or misconfigured. Docker permission denied before Testcontainers starts is an environment issue; no application assertion ran.
- **Test-data issue:** Required fixtures are absent, expired or shared incorrectly. A booking test with no future slot is not evidence that booking logic is wrong.

Do not repair a test by weakening its assertion until you know which category applies.

## 3. Example: age boundary failure

Suppose the TestNG report says `expected [CHILD] but found [ADULT]` for DOB `2008-11-02` and appointment `2026-11-01`.

- Reproduce that exact row, not an unrelated date.
- Check `PricingService.classify` and the use of completed years from `Period.between`.
- Compare the actual behavior with the contract's age-at-appointment rule.
- If the code is wrong, report the failure and fix the rule. If the test date was malformed or the expected category wrong, fix the test data/specification instead.

## 4. Example: database test cannot start

If `mvn verify` fails with a Testcontainers message about a Docker socket/daemon before the test method starts, the assertion was never reached. Check Docker availability and permissions. Do not change the database assertion to make the build green. On a CI runner, attach the startup logs to the test report.

If the container starts but Flyway fails on a migration, capture the migration name and database error. That is often a migration/schema defect rather than a Java pricing bug.

## 5. Reproducing and debugging

- Rerun one focused test first: `mvn -Dtest=PricingServiceTest test`.
- Keep the input/date/account/slot id the same while reproducing.
- Read exception type and message; do not assume every HTTP 500 is the root cause.
- Add temporary logs or use an IDE breakpoint to inspect values at the service boundary.
- For API failures, retain the request method/path/status/body (redact credentials and personal data).
- For flaky tests, repeat them and inspect concurrency, mutable shared state, clock dependence, selectors and waits.
- For performance problems, compare workload settings and system/resource metrics; a JMeter graph without a workload definition is not an actionable diagnosis.

## 6. Useful report contents

A defect report should include:

- Short descriptive title.
- Environment and application revision.
- Preconditions and synthetic test data.
- Exact reproducible steps or request.
- Expected result and actual result.
- Relevant status/error code, logs or screenshot.
- Frequency and impact, if known.
- Privacy-safe evidence only.

Example: “A second checkout for the same capacity-one slot is accepted” is more useful than “booking broken.” Include the slot fixture, two request timestamps, responses and final database state.

## 7. Reports in this repository

- Maven Surefire: `backend/target/surefire-reports/`.
- Maven Failsafe: `backend/target/failsafe-reports/`.
- JaCoCo: `backend/target/site/jacoco/` after `mvn verify`.
- Vitest: console summary; `npm run test:coverage` creates coverage output.
- Selenium/REST Assured: `qa/automation/target/surefire-reports/`.
- JMeter: `.jtl` raw results and optional HTML report directory.

## 8. Exercise

Imagine a REST Assured test fails to connect to `localhost:8080`. List one check for the application, one for the test environment and one for the test data. Which of the five categories above is most likely before the server is confirmed healthy?
