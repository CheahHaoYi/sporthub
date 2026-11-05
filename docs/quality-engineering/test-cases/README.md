# SportHub test-case catalogue

These cases are derived from current Java/React code, SQL migrations and product documents. A product document or table does not prove server behavior exists.

**Status key:**

- **Implemented:** An automated test exists and exercises the described behavior.
- **Schema-only:** A database rule exists and is checked below the missing service layer.
- **Blocked:** The production API/service needed to make the case meaningful has not been implemented.

| Feature file | Coverage |
|---|---|
| [Booking](booking.md) | Pricing/age rules, slot states, conflicts, past slots. |
| [Catalog and checkout](catalog-and-checkout.md) | Products, coupons, cart totals, stock, checkout and rollback. |
| [Authentication and admin](authentication-and-admin.md) | Login/register validation, roles and access restrictions. |
| [Additional implemented cases](additional-implemented-cases.md) | Safe validation error formatting, populated product rendering and facility search filtering. |
| [Traceability matrix](../requirements-to-tests.md) | Requirement-to-test links and gaps. |

Each case records its goal/risk, preconditions/data, action, expected observable result, layer/evidence and status. **Implemented** means the named check exists at that boundary; it does not imply broader workflow coverage. **Schema-only** marks a database constraint without service/API behavior. **Blocked** means required behavior or prerequisites are absent.
