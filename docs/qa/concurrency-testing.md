# Concurrency testing

Use real PostgreSQL transactions and independent threads/barriers for slot, stock, idempotency and coupon-limit contention. Assert one winner, conflict for losers, no duplicate active reservations, no negative inventory, a single order on retry, and full rollback after simulated payment failure. Lock rows in ascending IDs and recheck after acquiring locks. Testcontainers lifecycle must be explicitly managed by TestNG hooks (JUnit `@Container` is not a TestNG lifecycle).

These tests are not yet present because the database migration scripts and schema contract are empty. Implementing them before those constraints/columns are agreed would test an invented schema.
