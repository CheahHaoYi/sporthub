# SportHub test-case catalogue

These cases are derived from `docs/contracts/PROJECT_CONTRACT.md`, the React code, SQL migrations and currently implemented Java rules. They are grouped by feature.

**Status key:**

- **Implemented:** An automated test exists and exercises the described behavior.
- **Schema-only:** A database rule exists and is checked below the missing service layer.
- **Blocked:** The production API/service needed to make the case meaningful has not been implemented.

| Feature file | Coverage |
|---|---|
| [Booking](booking.md) | Pricing/age rules, slot states, conflicts, past slots. |
| [Catalog and checkout](catalog-and-checkout.md) | Products, coupons, cart totals, stock, checkout and rollback. |
| [Authentication and admin](authentication-and-admin.md) | Login/register validation, roles and access restrictions. |
| [Traceability matrix](../requirements-to-tests.md) | Requirement-to-test links and gaps. |
