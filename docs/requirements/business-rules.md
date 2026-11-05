# Business rules and evidence

Only rules visible in current implementation are called confirmed. Rules described by the product overview or schema without corresponding application behavior remain planned or schema-only. Product owners should confirm the unresolved items before they become API expectations.

## Confirmed implementation rules

| ID | Rule | Evidence | Test/evidence boundary |
|---|---|---|---|
| BR-C01 | Age is measured in completed years between birth and appointment dates. A birth date after the appointment date and null required dates are invalid. | [`PricingService.classify`](../../backend/src/main/java/com/sporthub/pricing/PricingService.java) | [`PricingServiceTest`](../../backend/src/test/java/com/sporthub/pricing/PricingServiceTest.java); direct Java unit behavior. |
| BR-C02 | Child is under 18; adult is 18–59; senior begins at 60. | Same source | Data-provider boundary examples at 18 and 60. |
| BR-C03 | Facility price discount is 20% child, 0% adult, 15% senior; values use cent scale and half-up rounding. Negative base prices are invalid; zero is accepted. | [`PricingService.facilityPrice`](../../backend/src/main/java/com/sporthub/pricing/PricingService.java) | Pricing unit tests; not integrated into a booking/cart flow. |
| BR-C04 | At most one row for a slot may have `booking_status = 'CONFIRMED'`. | [V7 integrity constraints](../../database/migrations/V7__create_indexes_and_integrity_constraints.sql) | [`PostgresApplicationIT`](../../backend/src/test/java/com/sporthub/integration/PostgresApplicationIT.java) verifies sequential duplicate rejection against PostgreSQL. No API conflict response is established. |
| BR-C05 | A Spring transaction can roll back a JDBC insert when a later operation throws. | [`PostgresApplicationIT`](../../backend/src/test/java/com/sporthub/integration/PostgresApplicationIT.java) | Test proves transaction primitive with a user row, not checkout policy. |

## Planned rules requiring confirmation

| Topic | Questions to resolve | Status |
|---|---|---|
| Account age | Is the 18+ rule only a registration UI rule, or must the server enforce it? Which date/time zone defines the birthday boundary? | Planned; frontend message exists, backend auth absent. |
| Booking | Are slots capacity-one? Which states release a slot? What status/error should duplicate or past-slot requests return? | Planned; database confirmed-slot index is schema-only protection. |
| Inventory | When is stock reserved or decremented, and how are competing purchases handled? | Planned; no inventory/checkout service. |
| Coupons | How are date boundaries, minimum subtotal, per-user limits and stacking handled? | Planned; no coupon evaluation service. |
| Checkout | What are payment outcomes, rollback guarantees, and idempotency-key replay semantics? | Planned; unique schema key alone does not define application response. |
| Authorization | What roles can use each administration operation, and what should unauthorized callers observe? | Planned; frontend route guard is not server authorization. |

These questions are **Needs confirmation** rather than test expectations. Once decided and implemented, add requirement IDs and link each rule to an executable case in the [traceability matrix](../quality-engineering/requirements-to-tests.md).
