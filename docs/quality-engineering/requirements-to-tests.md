# Requirements-to-test traceability matrix

This is the single maintained traceability matrix: confirmed source behavior maps to executable evidence, while planned product behavior maps to gaps. A blocked item is a test opportunity waiting on missing production behavior; it is not a passing test. Product rules requiring owner input are listed as **Needs confirmation** in [`docs/requirements/`](../requirements/functional-requirements.md).

| Requirement / risk | Test case(s) | Automated evidence | Status / gap |
|---|---|---|---|
| Age uses completed years on appointment date; child is 0–17 | BK-01, BK-02 | `PricingServiceTest.classifiesOnAppointmentDate` | Implemented as unit tests. |
| Senior classification begins at 60 | BK-03 | `PricingServiceTest.classifiesOnAppointmentDate` | Implemented as unit tests. |
| Child gets 20%, adult 0%, senior 15%; money rounds to cents | BK-04–BK-06 | `PricingServiceTest` | Implemented for facility price only; not integrated into cart/checkout. |
| Invalid dates/prices are rejected | BK-07, BK-08 | `PricingServiceTest` | Implemented as unit tests. |
| Unexpected exceptions return a generic internal-error response | ER-01 | `GlobalExceptionHandlerTest.unexpectedFailureReturnsGeneric500AndRequestPath` | Direct handler unit test checks status/body/path and detail suppression; server routing not covered. |
| Validation failures return stable safe field details | ER-02 | `GlobalExceptionHandlerTest.validationFailureReturnsBadRequestAndFieldDetails` | Unit tests the handler with a mocked validation exception; controller-to-handler dispatch is not covered. |
| SQL migrations create the catalogue schema and seed data | N/A | `PostgresApplicationIT.flywayCreatesTablesAndSeedsFacilitiesInPostgres` | Implemented against PostgreSQL Testcontainers. |
| One user row can be stored and retrieved | N/A | `PostgresApplicationIT.savesAndReadsAUserRowInThePostgresSchema` | JDBC integration only; no repository exists. |
| One slot has at most one confirmed booking | BK-11 | `PostgresApplicationIT.postgresRejectsASecondConfirmedBookingForTheSameSlot` | Database constraint tested; service/API behavior blocked. |
| Spring transaction rolls back after failure | EC-10 partial mechanism | `PostgresApplicationIT.failedSpringTransactionRollsBackItsInsertedUser` | Transaction primitive tested; checkout policy blocked. |
| Application health is publicly available | N/A | `PostgresApplicationIT.actuatorHealthReportsTheApplicationAndDatabaseAreUp`; `PlatformApiTest.healthEndpointAndDatabaseAreAvailable` | Implemented smoke check; the public health body exposes aggregate status only. |
| Unauthenticated access to the OpenAPI path is rejected | N/A | `PostgresApplicationIT.openApiDocumentIsProtectedByDefaultSecurity`; `PlatformApiTest.springdocMetadataIsProtectedByDefaultSecurity` | Implemented sanity check for Spring Security's current default behavior; it does not verify the document body. |
| Login form requires email/password | AU-01 | `Pages.test.tsx`; `PublicUiSmokeTest.loginPageExposesRequiredEmailAndPasswordFields` | Implemented in React tests and Selenium. |
| Under-age registration is rejected in UI | AU-02 | `Pages.test.tsx`; `PublicUiSmokeTest.registrationRejectsAChildAccountBeforeSendingAnApiRequest` | Frontend validation tested; server-side validation blocked. |
| Product page displays a populated API result | FE-14 | `Pages.test.tsx` | React component renders a mocked product name and formatted price; backend catalog endpoint remains blocked. |
| Facility search hides non-matching returned items | FE-15 | `Pages.test.tsx` | Client-side filter checked with two mocked facilities; no server-side search is established. |
| Customer cannot see admin route in UI | AU-06 | `Pages.test.tsx`; `PublicUiSmokeTest.customerRoleIsRedirectedAwayFromTheAdminRoute` | Client guard tested; server authorization blocked. |
| Customer can register/login with server account | AU-03, AU-04 | None | Blocked: backend auth controller/service absent. |
| Available slot can be booked; past/occupied slot rejected | BK-09, BK-10 | Database occupied-slot constraint only | Blocked: booking service/API absent. |
| Cart/coupon/checkout totals are correct | EC-05–EC-10 | UI mock test displays server totals; pricing unit test exists | Business checkout integration blocked. |
| Inventory never goes below zero under competing purchases | EC-04, EC-11 | None | Blocked: inventory/checkout service absent. |
| Admin APIs enforce role authorization | AU-07, AU-08 | None | Blocked: backend security/API implementation absent. |
| Health endpoint remains responsive under a small load | N/A | `qa/performance/jmeter/health-load-test.jmx` | Historical evidence: 25 HTTP 200 samples on 2026-11-05; this is a health check, not capacity evidence. |

## Traceability maintenance

When a requirement changes, update the matching test case and matrix row. When a feature implementation arrives, change `Blocked` to the test that now covers it and ensure the test checks actual application behavior, not only a mock.
