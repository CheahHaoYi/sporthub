# 07 — Test design and test-case management

Professional testing begins with a requirement and a risk, not with a tool. A test case is a reasoned example that could reveal a specific problem.

## 1. From requirement to test

1. **Requirement analysis:** Read the product contract, current code and schema. Ask what rules, roles, boundaries and failure cases matter.
2. **Test scenario:** Name the broad behavior (for example, “a customer checks out a booking”).
3. **Test case:** Define preconditions, data, steps, expected results and cleanup.
4. **Test data:** Choose representative valid, boundary and invalid values. Use synthetic data and keep tests isolated.
5. **Traceability:** Link each important requirement to one or more tests so untested requirements are visible.
6. **Risk-based priority:** Test high-impact and high-likelihood failures first. Duplicate bookings, incorrect charges, lost inventory and privilege leaks are high risk.

A test case should be specific enough that another person could repeat it and know whether it passed. Include expected results for every important step, not just “works.”

## 2. Test-design techniques

- **Equivalence partitioning:** Divide inputs into groups expected to behave similarly, then select a representative from each. For age pricing: child (0–17), adult (18–59), senior (60+), and invalid future DOB.
- **Boundary-value analysis:** Test values at and around a boundary because comparisons are often off by one. `PricingServiceTest` checks the day before and the exact 18th and 60th birthdays.
- **Decision table:** List combinations of conditions and their outcomes. For a coupon, conditions could include active, within validity dates, subtotal meets minimum, and redemption count available. Outcomes are accept or a specific rejection reason.
- **State-transition testing:** Check valid state changes and rejected transitions. A slot may move from available to confirmed to cancelled/released. A cart may move from active to checked out.
- **Exploratory testing:** A person learns the product while trying thoughtful actions, observing results and adjusting the next step. It can uncover unexpected behavior but should record steps/data so a defect can be reproduced.

## 3. Test-case catalogue

The detailed catalogue is organized by feature in [`test-cases/`](test-cases/). It separates:

- Tests already implemented in code (for example pricing boundaries and database duplicate-slot constraint).
- UI tests that verify the current React behavior.
- High-value business scenarios that remain blocked because the backend controllers/services do not exist.

“Blocked” is not the same as “failed.” It means the production behavior needed for a meaningful test is not implemented yet. Do not pass a mocked response off as a real booking or checkout check.

## 4. A concrete pricing decision table

`PricingService.classify` uses completed age on the appointment date. The production comparison is under 18 → child, 60 or older → senior, otherwise adult. The [TestNG provider](../../backend/src/test/java/com/sporthub/pricing/PricingServiceTest.java) supplies one day before and the exact birthday at both thresholds; this is the layer that directly observes those branches. The code supports a child DOB down to its input type's valid dates; the product age policy beyond the observed code is not otherwise specified.

For coupon rules, a decision table would be useful only after the owner confirms active date boundaries, minimum subtotal, per-user limits, stacking and rejection codes. Until then, those combinations remain **Planned / Needs confirmation**, and no expected API response should be invented.

## 5. How we derived current tests

| Source evidence | Risk/behavior | Test design choice | Implemented check |
|---|---|---|---|
| `PricingService.classify` and project contract age bands | Off-by-one age errors change charges. | Boundary-value analysis at age 18 and 60. | `PricingServiceTest.classifiesOnAppointmentDate`. |
| `PricingService.facilityPrice` rates and rounding | Wrong discount or currency rounding. | One representative input per class, plus rounding boundary. | Child/adult/senior/zero/rounding tests. |
| V7 partial unique confirmed-slot index | Two confirmed bookings for one slot. | Attempt a second insert using a real PostgreSQL schema. | `PostgresApplicationIT.postgresRejectsASecondConfirmedBookingForTheSameSlot`. |
| V1 required user fields and Spring transactions | Failed work should not leave data behind. | Insert inside a real transaction, throw, then query after rollback. | `PostgresApplicationIT.failedSpringTransactionRollsBackItsInsertedUser`. |
| React registration logic | Under-age account should receive a clear message. | Negative browser test for a realistic under-age DOB. | `PublicUiSmokeTest.registrationRejectsAChildAccountBeforeSendingAnApiRequest`. |
| React `AdminGuard` | Customer must not view admin pages in the UI. | Set a customer role and attempt protected route. | `PublicUiSmokeTest.customerRoleIsRedirectedAwayFromTheAdminRoute`. |

## 6. Keep tests maintainable

- Give each test one main reason to fail.
- Use names that describe behavior and expected outcome.
- Prefer stable codes/fields over full timestamps or incidental copy.
- Avoid depending on test execution order.
- Keep test data unique or reset it in a known way.
- Use mocks for isolation in unit tests, not to replace every dependency in integration tests.
- Put each requirement-to-test link in the traceability matrix.

## 7. Exercise

Pick “booking a past slot” from `test-cases/booking.md`. Identify the equivalence class, preconditions, test data, expected result, and which layer should own the first automated check once the booking service exists.
