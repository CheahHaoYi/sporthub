# 05 — System and end-to-end testing with Selenium

## 1. System test versus end-to-end test

A **system test** checks a complete application or feature against its requirements through an external interface. An **end-to-end (E2E) test** follows a complete user journey across the layers involved, often browser → frontend → HTTP → backend → database.

They overlap, but the terms emphasize different things: system testing emphasizes scope and requirements; E2E emphasizes the full journey. A browser test for a login form's required fields is a UI-level test, not proof that login works against the real backend.

In this repository, `PostgresApplicationIT` checks Spring Boot, PostgreSQL migrations and HTTP health. The Selenium tests check browser behavior in the React app. A complete booking/checkout E2E test cannot pass yet because the backend has no auth, facility, cart or checkout routes.

## 2. Frontend component tests with Vitest and React Testing Library

[`Pages.test.tsx`](../../frontend/src/pages/Pages.test.tsx) contains 13 focused UI tests. They were derived from behavior already implemented in `Pages.tsx`, the React route guards and the intended API response states:

- Login and registration fields must satisfy browser form validation, and an under-age registration must show an error.
- Facility/product pages display empty and error states; facility detail lets a customer select a returned slot and profile.
- The cart displays server-provided totals/discounts, coupon errors are surfaced, and checkout disables while its request is pending.
- Customer and admin route guards redirect or hide links based on the current frontend user state.

The methodology is arrange–act–assert: Vitest mocks the API/AuthContext, React Testing Library renders the page inside `MemoryRouter`, `fireEvent` performs user actions, and `findBy...`/`waitFor` waits for asynchronous UI updates before checking visible text, attributes or mock calls. This keeps the tests repeatable without a running backend or PostgreSQL.

These tests prove how the component responds to the chosen mock data. They do not prove that a real HTTP endpoint exists, that the server calculates the totals correctly, or that database state changes. Those checks belong to API and integration tests.

## 3. Selenium basics

**Selenium WebDriver** is a tool that sends commands to a real browser, such as Chrome: navigate to a URL, locate a button, type into a field, click it, and inspect what appears. A **browser driver** translates WebDriver commands into browser-specific operations. `DriverFactory` uses WebDriverManager and ChromeDriver and can run Chrome headlessly.

A **locator** tells Selenium how to find an element. SportHub provides stable `data-testid` attributes for important fields, so the tests use CSS selectors such as `[data-testid='login-email']`. The registration page also has a label-based XPath selector for the full-name field.

- **CSS selector** selects elements by tag, class, attribute or relationship. Example: `input[type='date']`.
- **XPath** describes a path through the document. It can select by text/relationship, but complex XPath expressions can be harder to maintain.
- Prefer accessible labels and stable test IDs; avoid selectors based on fragile visual layout or generated CSS class names.

## 4. Explicit waits and flaky tests

An **explicit wait** tells Selenium to wait up to a bounded time for a condition, such as the page title appearing or a redirect completing. `PublicUiSmokeTest` uses `WebDriverWait` and `ExpectedConditions`.

A **fixed sleep** pauses for the same duration whether the page is ready or not. It wastes time on fast runs and still fails on slower runs. Explicit waits poll for the expected state and continue as soon as it is ready.

A **flaky test** passes sometimes and fails other times without a relevant code change. Common causes include fixed sleeps, shared test data, hidden browser state, parallel tests contending for one resource, unstable selectors and dependent services. The current browser tests create a fresh Chrome instance for each method and close it in `@AfterMethod(alwaysRun=true)`.

## 5. Page Object Model

The **Page Object Model (POM)** keeps page locators and page actions in helper classes, separate from test intent. `LoginPage` and `RegisterPage` are small page objects. The test says what behavior to check; the page object knows where fields and buttons are.

A **test fixture** is setup and cleanup around a test. In the Selenium suite, the fixture creates a fresh WebDriver before each test and quits it afterward. A good fixture isolates state and always releases resources even when an assertion fails.

## 6. Browser tests that are executable today

`qa/automation/src/test/java/com/sporthub/qa/ui/PublicUiSmokeTest.java` checks:

1. The login page loads; email/password fields have the expected types/required behavior.
2. Client-side registration rejects a child account with the visible 18+ message before sending an API request.
3. A stored `CUSTOMER` role is redirected away from `/admin` to login by the React route guard.

These are browser smoke/sanity checks of implemented frontend behavior. The third test proves only that the client route guard works. Real authorization must be enforced by the backend too; no backend authorization layer exists yet.

The negative registration test fills realistic values and uses the native date-input value setter so React observes the selected under-age date. It then asserts the visible error message. The expectation comes from the actual `Register` component in `frontend/src/pages/Pages.tsx`.

## 7. Run frontend and Selenium tests

Run the fast component suite from `frontend/` (no database or backend required):

```sh
npm run test
```

The TestNG Selenium checks require the Vite frontend and Chrome:

Install/start the React app:

```sh
cd frontend
npm install
npm run dev
```

In another terminal, make sure Chrome is installed and run the browser group:

```sh
cd qa/automation
UI_BASE_URL=http://localhost:5173 BROWSER_HEADLESS=true mvn -Dgroups=ui test
```

The tests use explicit waits up to ten seconds. Success is shown as TestNG methods passing. Failure reports include the test method and assertion/timeout; Selenium exceptions and browser logs can reveal missing elements or a page that never became ready.

## 8. Planned user journeys and present blockers

The full business E2E suite should eventually cover registration, login, facilities, slot selection, conflicting booking rejection, product/cart, coupon, checkout, confirmation and administrator restrictions. Those journeys require working, documented backend APIs and deterministic fixture/reset support. Writing Selenium tests against fabricated responses now would only test the mock, not SportHub's booking or payment behavior.

After backend APIs exist, use API helpers to create unique synthetic accounts, slots, products and coupons; navigate with WebDriver; use explicit waits for visible outcomes; then use an API/database check to verify persisted state. Keep a small number of journeys that prove the highest-value paths, and exercise validation combinations at lower layers.

## 9. Exercise

Open `PublicUiSmokeTest.loginPageExposesRequiredEmailAndPasswordFields`. Identify the setup fixture, one CSS locator, one assertion and the cleanup fixture. Explain what this browser test proves—and one thing it does not prove about authentication.
