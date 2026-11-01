# API and browser automation

This standalone Maven project uses Java 21, TestNG, REST Assured, Selenium 4 and WebDriverManager. It can run independently of the backend build.

```sh
cd qa/automation
API_BASE_URL=http://localhost:8080/api/v1 UI_BASE_URL=http://localhost:5173 mvn clean test
mvn -Dgroups=smoke test
mvn -Dgroups=api test
mvn -Dgroups=ui test
```

Configuration is read from JVM properties first, then environment variables. Defaults are local backend/frontend URLs and headless Chrome. `DriverFactory` creates a fresh ChromeDriver; UI tests should always quit it in `@AfterMethod` and use explicit waits. API requests use REST Assured and must assert documented statuses and error codes. Add new user journeys as TestNG independent tests and create deterministic data via API fixtures rather than relying on execution order.

The current suite includes a facility-listing smoke test. Further API and UI cases depend on backend route implementation and stable frontend selectors. No live service test has been run as part of this setup.
