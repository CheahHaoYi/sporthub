# SportHub backend

Java 21, Spring Boot 3.4.5, PostgreSQL, Spring Data JPA, Spring Security, Flyway and TestNG. The backend owns business logic; the schema and Flyway SQL are owned by `database/`.

## Local setup

Set `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `JWT_SIGNING_SECRET` (at least 32 bytes, Base64 encoded), `JWT_EXPIRATION_SECONDS`, `SIMULATED_PAYMENT_OUTCOME` (`SUCCESS` or `FAILURE`) and `SPRING_PROFILES_ACTIVE`. The migration location defaults to `filesystem:../database/migrations` for local execution from `backend/`; set `DATABASE_MIGRATION_LOCATION` to the mounted migration directory in packaged deployments. Database credentials in `application.yml` are local convenience defaults only. Never use them outside a development database.

For a disposable local PostgreSQL instance with credentials matching the defaults, run `docker compose -f backend/compose.yaml up -d` from the repository root. It listens on host port `5433` to avoid taking over an existing PostgreSQL server. Then, from `backend/`, run:

```sh
DATABASE_URL=jdbc:postgresql://localhost:5433/sporthub \
JWT_SIGNING_SECRET=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY= \
mvn spring-boot:run
```

Or point `DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD` at your own PostgreSQL instance. The password authentication error means the server at the configured host/port rejected the configured credentials; the application cannot discover or reset an existing PostgreSQL password. The compose database and its password are for local development only.

```sh
cd backend
mvn clean test
mvn verify
mvn spring-boot:run
```

`mvn clean test` runs TestNG unit tests through Surefire and `src/test/resources/testng.xml`. `mvn verify` additionally runs integration tests selected by `src/test/resources/testng-it.xml` through Failsafe and writes JaCoCo HTML to `target/site/jacoco`. Run one class with `mvn -Dtest=SomeTest test`; select groups with `-Dgroups=unit`. Integration tests use `*IT.java` and `-Dit.test=SomeIT verify`.

Hibernate schema generation is disabled (`validate`). Flyway reads the database agent's migrations, never migrations copied into this project. The checked-in database migration files currently contain placeholders, so application startup and PostgreSQL integration tests remain blocked until database schemas are supplied.

## Integration status

`docs/contracts/PROJECT_CONTRACT.md` is authoritative. `docs/contracts/DATA_MODEL.md`, `docs/contracts/API_CONTRACT.md` and all current `database/migrations/*.sql` are unpopulated placeholders. Entity mappings, authentication against persisted accounts, the business endpoints and transaction/concurrency behavior cannot be safely implemented without the agreed column names, constraints and status fields. Do not interpret successful Maven compilation as a completed API.

Payment simulation is configured with `SIMULATED_PAYMENT_OUTCOME`; no card data is accepted or stored. OpenAPI UI is exposed at `/swagger-ui.html` when the app starts.
