# Checkout concurrency and integrity

Checkout must run in one PostgreSQL transaction. Lock slot and product rows in a stable sorted ID order (`SELECT ... FOR UPDATE`) to reduce deadlocks. Then recheck active flags, future slot time, stock, profile ownership, coupon validity/limits, and recompute totals before inserting durable rows.

| Invariant | Database protection | Service/transaction work |
|---|---|---|
| Case-insensitive email and coupon codes | Unique indexes over `lower(...)` | Normalize/display email and code consistently |
| One SELF profile per account | Partial unique index | Require a SELF profile on registration |
| One ACTIVE cart per account | Partial unique index | Create/reuse cart safely on conflict |
| Valid cart item type and references | CHECK plus FKs | Ensure the referenced item is active and owned |
| No inventory below zero | CHECK `stock_quantity >= 0` | Lock product, check quantity, decrement conditionally in same transaction |
| No duplicate slot time definition | Unique `(resource_id, starts_at, ends_at)` | Reject past/inactive slots |
| One active booking per capacity-one slot | Partial unique index for `CONFIRMED` | Lock slot and translate unique violation into conflict; cancellation changes status and releases uniqueness |
| Booking belongs to order and account-owned profile | Foreign keys, including composite account/profile FK | Validate customer can use profile and calculate age at slot start |
| Checkout idempotency per account | Unique `(account_user_id,idempotency_key)` | Compare stored request fingerprint; same payload returns prior result, changed payload conflicts |
| Coupon redemptions per customer/overall | Redemption FKs/indexes, coupon count CHECK | Lock coupon row, count account redemptions, increment `redeemed_count`, insert redemption atomically |
| Checkout totals and snapshot math | Non-negative amount CHECKs | Calculate all snapshots and totals from locked current data |
| Failed payment rollback | Transaction boundary | Do not commit order, booking, decrement, redemption or payment on simulated failure |

A unique violation remains the final race-condition guard even after a row lock. Product stock may be updated with `UPDATE products SET stock_quantity = stock_quantity - :qty WHERE id = :id AND stock_quantity >= :qty AND is_active`; verify one affected row. Coupon count must be changed under a row lock with the total limit checked in the same transaction. Never reserve inventory or slots when writing cart items.
