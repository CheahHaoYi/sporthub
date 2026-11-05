# Additional implemented test cases

This page explains the additional executable checks added to the suite. These cases use existing behavior; none assume a booking, checkout or account API exists.

## ER-02 — Validation errors include safe field details

**Risk:** A caller could receive the wrong status/code or no useful field-level guidance when request validation fails. The handler must identify the invalid field while keeping the message generic.

- **Arrange:** `GlobalExceptionHandlerTest.validationFailureReturnsBadRequestAndFieldDetails` supplies a mocked `MethodArgumentNotValidException`, a mocked binding result with one `email` field error, and a request path `/api/v1/register`.
- **Act:** It calls `GlobalExceptionHandler.validation` directly.
- **Assert:** HTTP 400, response status `400`, code `VALIDATION_ERROR`, the request path, and exactly one field error with field `email` and message `must be a valid email`.
- **Boundary:** This directly tests error-body formatting and mapping. It does not start Spring MVC or prove a controller's `@Valid` request invokes this handler.
- **Run:** `cd backend && mvn test` (Surefire, `unit` group). Source: [`GlobalExceptionHandlerTest`](../../../backend/src/test/java/com/sporthub/common/GlobalExceptionHandlerTest.java) and [`GlobalExceptionHandler`](../../../backend/src/main/java/com/sporthub/common/GlobalExceptionHandler.java).

## FE-14 — Product list renders a returned catalogue item

**Risk:** A page could receive a valid product list but show an empty state, omit the product name, or display the wrong price.

- **Arrange:** `Pages.test.tsx` configures the mocked `productApi.list` to return Training Racket at `45.00`.
- **Act:** It renders `Products` inside `MemoryRouter`.
- **Assert:** The product name and formatted `SGD 45.00` appear. This checks rendering of a populated response rather than the existing empty/error states.
- **Boundary:** The returned object is test-controlled. This does not establish that `/products` exists or that the server's price is correct.
- **Run:** `cd frontend && npm test`. Source: [`Pages.test.tsx`](../../../frontend/src/pages/Pages.test.tsx) and [`Products`](../../../frontend/src/pages/Pages.tsx).

## FE-15 — Facility search filters the returned list

**Risk:** Search text could be ignored or a non-matching facility could remain visible, making the list hard to use.

- **Arrange:** The mocked `facilityApi.list` returns Central Courts and East Pool.
- **Act:** Render `Facilities`, wait for the results, then enter `Central` in the accessible Search facilities field.
- **Assert:** Central Courts remains visible and East Pool is absent. This checks the client-side name filter over the returned data.
- **Boundary:** It does not test server-side search, network behavior or persisted facility data. The current component fetches the list and filters it in the browser.
- **Run:** `cd frontend && npm test`. Source: [`Pages.test.tsx`](../../../frontend/src/pages/Pages.test.tsx) and [`Facilities`](../../../frontend/src/pages/Pages.tsx).

## How to read these cases

Each test follows Arrange–Act–Assert: prepare controlled inputs/dependencies, invoke one behavior, and check the observable result. The backend case uses a mocked Spring validation exception but the real handler; the two frontend cases use mocked API responses and real React components. These tests add confidence at their own boundaries and do not substitute for live business API tests.
