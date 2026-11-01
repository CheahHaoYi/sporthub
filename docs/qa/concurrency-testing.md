# Concurrency testing

Use real PostgreSQL transactions and independent threads/barriers for slot, stock, idempotency and coupon-limit contention. Assert one winner, conflict for losers, no duplicate active reservations, no negative inventory, a single order on retry, and full rollback after simulated payment failure. Lock rows in ascending IDs and recheck after acquiring locks. Testcontainers lifecycle must be explicitly managed by TestNG hooks or Spring dynamic properties (JUnit `@Container` is not a TestNG lifecycle). A current integration test checks the slot uniqueness constraint sequentially; concurrent service/API behavior remains a gap.

The SQL schema is now present in `database/migrations/`, but Java booking/cart/checkout services and endpoints are not implemented. The next concurrency tests should be added with those services so they exercise real locking and transactional behavior rather than only raw database inserts.
