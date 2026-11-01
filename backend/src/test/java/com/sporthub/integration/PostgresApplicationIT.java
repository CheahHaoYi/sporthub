package com.sporthub.integration;

import io.restassured.RestAssured;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

/**
 * Integration/system checks for the real Flyway schema and currently available
 * Spring Boot endpoints. TestNG owns the Testcontainers lifecycle; no JUnit
 * Testcontainers extension is used in this project.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "management.endpoints.web.exposure.include=health,info",
                "sporthub.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
        }
)
public class PostgresApplicationIT extends AbstractTestNGSpringContextTests {
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @LocalServerPort
    private int port;

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        POSTGRES.start();
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.flyway.locations", () -> "filesystem:../database/migrations");
    }

    @BeforeClass
    public void configureHttpClient() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Test(groups = {"integration", "system", "smoke"})
    public void flywayCreatesTablesAndSeedsFacilitiesInPostgres() {
        Integer migrationCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE success = TRUE", Integer.class);
        Integer facilityCount = jdbc.queryForObject("SELECT COUNT(*) FROM facilities", Integer.class);
        Integer productCount = jdbc.queryForObject("SELECT COUNT(*) FROM products", Integer.class);

        assertEquals(migrationCount, Integer.valueOf(8), "All eight versioned SQL migrations should apply");
        assertEquals(facilityCount, Integer.valueOf(5), "The demo migration defines five facilities");
        assertTrue(productCount > 0, "The demo catalogue should include products");
    }

    @Test(groups = "integration")
    public void savesAndReadsAUserRowInThePostgresSchema() {
        String email = "persistence-test-" + UUID.randomUUID() + "@example.test";
        long userId = jdbc.queryForObject(
                "INSERT INTO app_users(email, password_hash, role) VALUES (?, ?, 'CUSTOMER') RETURNING id",
                Long.class, email, "not-a-real-login-hash");

        String storedEmail = jdbc.queryForObject("SELECT email FROM app_users WHERE id = ?", String.class, userId);
        String storedRole = jdbc.queryForObject("SELECT role FROM app_users WHERE id = ?", String.class, userId);

        assertEquals(storedEmail, email);
        assertEquals(storedRole, "CUSTOMER");
    }

    @Test(groups = "integration")
    public void failedSpringTransactionRollsBackItsInsertedUser() {
        String email = "rollback-test-" + UUID.randomUUID() + "@example.test";
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);

        expectThrows(IllegalStateException.class, () -> transaction.execute(status -> {
            jdbc.update("INSERT INTO app_users(email, password_hash, role) VALUES (?, ?, 'CUSTOMER')",
                    email, "not-a-real-login-hash");
            throw new IllegalStateException("simulate a later checkout failure");
        }));

        Integer rows = jdbc.queryForObject("SELECT COUNT(*) FROM app_users WHERE email = ?", Integer.class, email);
        assertEquals(rows, Integer.valueOf(0), "The failed transaction must leave no user row");
    }

    @Test(groups = {"integration", "system"})
    @Transactional
    public void postgresRejectsASecondConfirmedBookingForTheSameSlot() {
        long userId = jdbc.queryForObject(
                "INSERT INTO app_users(email, password_hash, role) VALUES (?, ?, 'CUSTOMER') RETURNING id",
                Long.class, "booking-test-" + UUID.randomUUID() + "@example.test", "not-a-real-login-hash");
        long profileId = jdbc.queryForObject(
                "INSERT INTO customer_profiles(account_user_id, profile_type, full_name, date_of_birth) " +
                        "VALUES (?, 'SELF', 'Integration Test', DATE '1990-01-01') RETURNING id",
                Long.class, userId);
        long orderId = jdbc.queryForObject(
                "INSERT INTO orders(order_number, account_user_id, idempotency_key, request_fingerprint, " +
                        "subtotal, demographic_discount_total, coupon_discount_total, total_amount, " +
                        "order_status, payment_status) VALUES (?, ?, ?, ?, 18.00, 0, 0, 18.00, " +
                        "'CONFIRMED', 'SIMULATED_SUCCESS') RETURNING id",
                Long.class, "TEST-" + UUID.randomUUID(), userId, UUID.randomUUID().toString(), "integration-test");
        long slotId = jdbc.queryForObject("SELECT id FROM facility_slots ORDER BY starts_at, id LIMIT 1", Long.class);

        insertConfirmedBooking(orderId, userId, slotId, profileId);

        expectThrows(DataIntegrityViolationException.class,
                () -> insertConfirmedBooking(orderId, userId, slotId, profileId));
    }

    @Test(groups = {"integration", "system", "smoke"})
    public void actuatorHealthReportsTheApplicationAndDatabaseAreUp() {
        RestAssured.given()
                .when().get("/actuator/health")
                .then()
                .statusCode(200)
                .body("status", equalTo("UP"));
    }

    @Test(groups = {"integration", "system", "sanity"})
    public void openApiDocumentIsProtectedByDefaultSecurity() {
        RestAssured.given()
                .when().get("/v3/api-docs")
                .then()
                .statusCode(401);
    }

    @AfterSuite(alwaysRun = true)
    public void stopPostgresContainer() {
        if (POSTGRES.isRunning()) POSTGRES.stop();
    }

    private void insertConfirmedBooking(long orderId, long userId, long slotId, long profileId) {
        jdbc.update("INSERT INTO bookings(order_id, account_user_id, facility_slot_id, customer_profile_id, " +
                        "booking_status, base_price, demographic_category_snapshot, discount_percentage_snapshot, " +
                        "discount_amount_snapshot, final_price) VALUES (?, ?, ?, ?, 'CONFIRMED', 18.00, 'ADULT', 0, 0, 18.00)",
                orderId, userId, slotId, profileId);
    }
}
