# SportHub database

PostgreSQL 16 is the local development database. Schema changes are owned by Flyway SQL migrations in `database/migrations/`; Hibernate must use `ddl-auto=validate`. Migrations are ordered V1–V8 and are intended to be applied once to a fresh database. Do not edit an already deployed versioned migration; add a new version instead.

## Start locally

```sh
cp database/.env.example database/.env
# Change POSTGRES_PASSWORD if desired for a shared development environment.
docker compose --env-file database/.env -f database/docker-compose.yml up -d
```

Defaults: database `sporthub`, user `sporthub`, password `sporthub_local`, host port `5432`. JDBC URL: `jdbc:postgresql://localhost:5432/sporthub`. Set `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD` from those values. The compose file uses a named persistent volume and readiness health check. For Flyway, configure `spring.flyway.locations=filesystem:../database/migrations` when launched from the backend directory (or use the absolute path / packaged `classpath:db/migration` location if migrations are copied into backend resources). Spring Boot Flyway at application startup is the normal migration mechanism.

For manual migration execution only, use the Flyway CLI with `-locations=filesystem:database/migrations -url=jdbc:postgresql://localhost:5432/sporthub -user=sporthub -password="$POSTGRES_PASSWORD" migrate`. The CLI must be installed separately.

## Inspect and operate

```sh
database/scripts/check-connection.sh
psql -h localhost -U sporthub -d sporthub -f database/scripts/list-tables.sql
psql -h localhost -U sporthub -d sporthub -f database/scripts/list-indexes.sql
psql -h localhost -U sporthub -d sporthub -f database/scripts/list-foreign-keys.sql
psql -h localhost -U sporthub -d sporthub -f database/scripts/view-seed-data.sql
```

`database/scripts/reset-local.sh --confirm-delete-local-data` destroys the compose database volume and all local database data. The confirmation flag is required intentionally.

## Migration contents

- V1 identity and customer profiles
- V2 facilities and resources
- V3 product categories, products and stock
- V4 demographic discount policies and coupons
- V5 facility slots, carts and cart items
- V6 orders, bookings, item snapshots, coupon redemptions and simulated payments
- V7 query indexes
- V8 repeatable demonstration catalogue, slots, products, policies and coupons

Flyway records successful versioned migrations and does not rerun them during normal startup, so starting the application again does not duplicate the seed data. Slot generation also uses a unique resource/time key with `ON CONFLICT DO NOTHING` to make the generated slot rows safe if the seed statement is deliberately rerun. A fresh install generates multiple slots per resource over the next five Singapore calendar days, so seed slots do not expire permanently. URLs use `images.example.com` placeholders and should be replaced if a deployable image catalogue is introduced.

## Integrity ownership

SQL enforces keys, required values, enumerated states, monetary bounds, inventory non-negativity, valid slot intervals and capacity one, cart item shape, profile ownership for bookings, unique active carts, unique idempotency keys, and one confirmed booking per slot. Java must validate age eligibility at appointment time, dependent-child eligibility, future booking time, active facility/resource/slot/product checks, coupon validity and redemption limits, stock sufficiency, aggregate pricing arithmetic, and transactional checkout. Row locks and a single transaction are required for checkout; see [CONCURRENCY.md](CONCURRENCY.md).

No users are seeded. Development accounts must be created by the backend's development-only initializer with a password encoder; never put plaintext credentials in migrations.

## Known limitations

- Cross-row rules (active parent facility/resource, future slot, coupon limits and order total arithmetic) require service validation and transactional locking; ordinary CHECK constraints cannot safely enforce them.
- Cancellation history is retained by changing booking status; rows are not deleted.
- Migration rollback is not automatic. Flyway versioned migrations are forward-only in normal operation; recovery should use a database backup or a reviewed compensating migration.
