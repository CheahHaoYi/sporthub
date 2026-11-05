# Functional requirements and implementation status

This page records confirmed behavior separately from product intent. **Confirmed** means observable in production code or migrations; **Planned** means described by product documentation but not implemented in the backend; **Blocked** means a meaningful test needs an absent service/API. These notes are an evidence inventory, not a signed-off product contract. Items requiring a product decision are marked **Needs confirmation**.

## Confirmed today

| ID | Behavior | Evidence | Status |
|---|---|---|---|
| FR-C01 | Classify age using completed years on the appointment date: below 18 child, 18–59 adult, 60+ senior. | [`PricingService.classify`](../../backend/src/main/java/com/sporthub/pricing/PricingService.java); [`PricingServiceTest`](../../backend/src/test/java/com/sporthub/pricing/PricingServiceTest.java) | Confirmed; unit tested. |
| FR-C02 | Calculate facility discounts: child 20%, adult 0%, senior 15%, rounded to cents. | [`PricingService.facilityPrice`](../../backend/src/main/java/com/sporthub/pricing/PricingService.java); [`PricingServiceTest`](../../backend/src/test/java/com/sporthub/pricing/PricingServiceTest.java) | Confirmed; unit tested. |
| FR-C03 | Apply migrations and seed catalogue data; PostgreSQL prevents two confirmed bookings for one slot. | [`database/migrations/`](../../database/migrations/); [`PostgresApplicationIT`](../../backend/src/test/java/com/sporthub/integration/PostgresApplicationIT.java) | Confirmed schema behavior; service/API behavior blocked. |
| FR-C04 | The current server exposes Actuator health and default security protects Springdoc metadata. | [`PostgresApplicationIT`](../../backend/src/test/java/com/sporthub/integration/PostgresApplicationIT.java); [`PlatformApiTest`](../../qa/automation/src/test/java/com/sporthub/qa/api/PlatformApiTest.java) | Confirmed; HTTP checked. |
| FR-C05 | React forms validate inputs and route guards make client-side decisions; pages render API response states. | [`Pages.tsx`](../../frontend/src/pages/Pages.tsx); [`Guards.tsx`](../../frontend/src/routes/Guards.tsx); [`Pages.test.tsx`](../../frontend/src/pages/Pages.test.tsx) | Confirmed frontend behavior; API calls are mocked in component tests. |

## Planned behavior needing confirmation and implementation

The product overview and project contract describe booking, shopping, checkout, persisted accounts and administrator workflows. Backend production source currently contains only the application entry point, shared error response/handler and pricing service; there are no business controllers, services, entities or repositories. Therefore the following are **Planned / Blocked**, and their details are not a verified API contract.

| ID | Product behavior to confirm | Required evidence before executable API tests |
|---|---|---|
| FR-P01 | Register/login, account age policy, duplicate email behavior, token format and session persistence. | Approved auth/API contract plus auth controller/service and test account fixture. |
| FR-P02 | Browse facilities, availability and slot selection. | Approved paths/DTO/statuses, implemented service and deterministic facility/slot fixtures. |
| FR-P03 | Customer booking, cancellation and ownership/authorization rules. | Agreed conflict and cancellation contract plus booking service/API. |
| FR-P04 | Product browsing, cart quantity and inventory handling. | Agreed stock semantics plus catalog/cart services and synthetic product fixtures. |
| FR-P05 | Coupon eligibility, expiry, limits, checkout totals and idempotent retry. | Approved decision rules, checkout contract and transactional implementation. |
| FR-P06 | Payment outcomes and administrator operations. | Approved permission/error contract and implemented payment/admin services. |

Do not treat the React API module or SQL table names as proof of these behaviors. Map approved decisions to cases in [`quality-engineering/test-cases/`](../quality-engineering/test-cases/README.md) and the [traceability matrix](../quality-engineering/requirements-to-tests.md).
