# Automation framework

`qa/automation` is an independent Java 21 Maven project. TestNG discovers suite tests; REST Assured sends API traffic; Selenium uses ChromeDriver via WebDriverManager and is headless by default. Set `SYSTEM_BASE_URL` (default `http://localhost:8080`), `UI_BASE_URL` (default `http://localhost:5173`) and `BROWSER_HEADLESS` (default `true`). Start the backend from the runbook before API checks. The current Spring Boot health endpoint is public, while `/v3/api-docs` is protected by default security.

Run `cd qa/automation && mvn test`; use `-Dgroups=api`, `-Dgroups=ui`, `-Dgroups=smoke` or `-Dgroups=sanity`. Current tests check Actuator health, default OpenAPI access protection, and frontend form/route behavior. The old `/facilities` smoke check was removed because there is no backend facility route. Full UI journeys require backend APIs and synthetic account/data fixtures that do not exist yet.
