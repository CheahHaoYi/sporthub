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
mvn test
mvn verify
mvn spring-boot:run
```

`mvn test` runs TestNG unit tests through Surefire and `src/test/resources/testng.xml`. `mvn verify` additionally runs `*IT.java` integration tests through Failsafe and writes JaCoCo HTML to `target/site/jacoco`. The integration suite uses Testcontainers and requires Docker. Run the unit group with `mvn -Dgroups=unit test`; run the full unit plus integration pipeline with `mvn verify`.

Hibernate schema generation is disabled (`validate`). Flyway reads the checked-in SQL migrations from `../database/migrations`. V1–V8 currently define identity/profiles, facilities/resources, products, discounts/coupons, carts/slots, orders/bookings/payments, indexes/constraints and demo catalogue data.

## Integration status

`docs/contracts/PROJECT_CONTRACT.md` describes the intended product. `docs/contracts/DATA_MODEL.md` and `docs/contracts/API_CONTRACT.md` are still unpopulated headings. The schema is present, but Java entity mappings, repositories, persisted-account authentication, business controllers/services and transaction/concurrency behavior are not implemented. Current backend code includes the application bootstrap, pure `PricingService`, common error response handling and Actuator/Springdoc configuration. Do not interpret successful Maven compilation as a completed booking/shop API.

The payment outcome property is configured, but there is no checkout/payment service yet. No card data is accepted or stored. Springdoc registers its generated OpenAPI UI at `/swagger-ui.html` when the app starts; the current Spring Security defaults require authentication to view it, and business routes will appear only after controllers are implemented.
