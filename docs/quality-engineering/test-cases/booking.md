# Booking test cases

| ID / scenario | Preconditions and data | Steps | Expected result | Method | Status |
|---|---|---|---|---|---|
| BK-01 Age 17 child boundary | DOB `2008-11-02`, appointment `2026-11-01`. | Call `PricingService.classify`. | `CHILD`; the birthday is tomorrow. | Unit / boundary value. | Implemented in `PricingServiceTest`. |
| BK-02 Turns 18 on appointment day | DOB `2008-11-01`, appointment `2026-11-01`. | Classify. | `ADULT`. | Unit / boundary value. | Implemented in data provider. |
| BK-03 Turns 60 on appointment day | DOB `1966-11-01`, appointment `2026-11-01`. | Classify. | `SENIOR`. | Unit / boundary value. | Implemented in data provider. |
| BK-04 Child facility discount | Base `$19.99`; child DOB/appointment. | Calculate facility price. | Base `$19.99`, discount `$4.00`, payable `$15.99`. | Unit / arithmetic assertion. | Implemented. |
| BK-05 Adult pays standard rate | Adult DOB; base `$20.00`. | Calculate facility price. | Discount `$0.00`; payable `$20.00`. | Unit. | Implemented. |
| BK-06 Senior discount | Age 60; base `$100.00`. | Calculate facility price. | Discount `$15.00`; payable `$85.00`. | Unit. | Implemented. |
| BK-07 Future DOB | DOB later than appointment. | Classify. | `IllegalArgumentException`; no category returned. | Unit / negative. | Implemented. |
| BK-08 Negative slot price | Base `-$0.01`. | Calculate facility price. | `IllegalArgumentException`. | Unit / invalid input. | Implemented. |
| BK-09 Add available future slot | Authenticated customer owns the selected profile; active slot is future and unbooked. | Add booking item to cart, then inspect server cart. | Item references selected slot/profile and has server-calculated price. | API + integration + E2E. | Blocked: booking/cart endpoints absent. |
| BK-10 Book a past slot | Valid account and a slot whose start time is before current time. | Attempt booking/checkout. | Request rejected; no confirmed booking row. | API/system. | Blocked: booking/checkout service absent. |
| BK-11 Book occupied slot | One confirmed booking exists for a capacity-one slot. | Submit another booking for that slot. | Conflict response; exactly one confirmed booking remains. | PostgreSQL integration + API. | Schema-only DB guard implemented in `PostgresApplicationIT`; service/API scenario blocked. |
| BK-12 Concurrent conflicting booking | Two customers target the same unconfirmed slot at the same instant. | Synchronize two independent requests/transactions. | Exactly one succeeds; no duplicate confirmed row exists. | Concurrency/system. | Blocked: no booking service/API. |
| BK-13 Cancel and release slot | Confirmed booking belongs to current customer. | Cancel, then book the same slot again. | First status becomes cancelled; second booking may succeed. | Integration + API. | Blocked: cancellation service absent. |
| BK-14 Cancel someone else's booking | Booking belongs to another account. | Submit cancellation as current customer. | Forbidden/not found per agreed contract; other booking remains confirmed. | API authorization. | Blocked: auth and booking endpoints absent. |
