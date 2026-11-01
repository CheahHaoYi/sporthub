# Automation framework

`qa/automation` is an independent Java 21 Maven project. TestNG discovers suite tests; REST Assured sends API traffic; Selenium uses ChromeDriver via WebDriverManager and is headless by default. Set `API_BASE_URL` (default `http://localhost:8080/api/v1`), `UI_BASE_URL` (default `http://localhost:5173`) and `BROWSER_HEADLESS` (default `true`).

Run `cd qa/automation && mvn test`; use `-Dgroups=api`, `-Dgroups=ui`, or `-Dgroups=smoke`. The initial smoke test exercises the public facility listing contract. UI journey tests require stable frontend `data-testid` hooks and seeded development accounts, neither of which is currently present. Tests should own their data and avoid order dependencies as those interfaces arrive.
