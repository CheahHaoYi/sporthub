# API and browser automation

This standalone Maven project uses Java 21, TestNG, REST Assured, Selenium 4 and WebDriverManager. It can run independently of the backend build, but API tests need a running backend and browser tests need the Vite frontend and Chrome.

```sh
cd qa/automation
SYSTEM_BASE_URL=http://localhost:8080 \
UI_BASE_URL=http://localhost:5173 BROWSER_HEADLESS=true mvn test
mvn -Dgroups=api test
mvn -Dgroups=ui test
mvn -Dgroups=smoke test
mvn -Dgroups=sanity test
```

Configuration is read from JVM properties first, then environment variables. Defaults are local backend/frontend URLs and headless Chrome. Start the backend as shown in the runbook; the current Spring Boot health endpoint is public. `DriverFactory` creates a fresh ChromeDriver; `PublicUiSmokeTest` always quits it in `@AfterMethod` and uses explicit waits. API requests use REST Assured to assert the health status and default Spring Security response for Springdoc.

The current suite includes API health smoke/security sanity tests and browser checks for login form requirements, age rejection in registration, and the customer admin-route guard. The former facility-list smoke test was removed because the backend has no `/facilities` controller. Registration, bookings, products, checkout and server-side authorization tests remain blocked until backend routes/services are implemented. Create synthetic test data through API fixtures when those interfaces exist; do not rely on execution order.
