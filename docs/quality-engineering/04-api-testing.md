# 04 — API testing

## 1. API, REST, HTTP and JSON

An **API** is a defined way for one program to ask another program to do something or return information. SportHub's React app calls a backend API through Axios in `frontend/src/api/index.ts`.

**HTTP** is the request/response protocol used by a browser and a web server. A request commonly contains a method, URL, headers and sometimes a body. The response contains a status code, headers and sometimes a body.

**REST** is a common style for HTTP APIs. It represents things as resources and uses HTTP methods to describe actions. The paths below are frontend calls and remain **Planned/Blocked** until a matching backend controller exists:

| Method | Usual meaning | SportHub example from frontend calls |
|---|---|---|
| `GET` | Read data without intentionally changing it. | `GET /facilities` to list facilities. |
| `POST` | Submit data or create/trigger an operation. | `POST /auth/login`, `POST /cart/items/bookings`, `POST /checkout`. |
| `PUT` | Replace a whole resource. | A possible complete facility update. |
| `PATCH` | Change part of a resource. | `PATCH /cart/items/{itemId}` to change quantity. |
| `DELETE` | Remove or cancel a resource/action. | `DELETE /cart/items/{itemId}`. |

The frontend's use of a path does not prove the backend implements it. Current Spring source implements no business controller. The basic Actuator health status is public; Spring Security auto-configuration protects the generated Springdoc document.

### Headers, bodies and JSON

- A **request header** carries metadata, such as `Content-Type: application/json` or `Authorization: Bearer <token>`.
- A **request body** contains submitted data, such as a login email and password.
- A **response body** contains the server's result, often JSON.
- **JSON** (JavaScript Object Notation) is a text format built from objects, arrays, strings, numbers, booleans and null. Example: `{"status":"UP"}`.
- A **status code** summarizes the response outcome: `200` success, `201` created, `400` invalid request, `401` unauthenticated, `403` authenticated but forbidden, `404` not found, `409` conflict, `500` unexpected server error. Exact codes should be stated in the API contract.

### Authentication, authorization and tokens

- **Authentication** asks, “Who is calling?” A login flow commonly checks credentials and returns an access token.
- **Authorization** asks, “What is this caller allowed to do?” A customer should not perform administrator operations.
- A **bearer/access token** is a credential sent in an `Authorization: Bearer ...` header. The frontend client attaches `sporthub_token` from browser storage.

The project contract calls for JWT authentication, and the frontend is prepared to store a token. But backend source currently has no JWT filter, auth controller, security configuration or user service. The active server falls back to Spring Security's generated/configured HTTP Basic user for local operational endpoints. Do not treat the frontend route guard as server-side authorization.

### API contract

An **API contract** says which paths and methods exist, what inputs are allowed, what the response fields look like, which status codes/errors can occur, and what access rules apply. It lets a client and server agree on behavior.

`docs/contracts/API_CONTRACT.md` is currently just a heading. Springdoc is a dependency and can generate `/v3/api-docs`, but with no business controllers the generated document does not describe the booking/shop API.

## 2. Inspect and call the current API manually

Start PostgreSQL as described in the overview, then start the backend from `backend/`:

```sh
DATABASE_URL=jdbc:postgresql://localhost:5433/sporthub \
JWT_SIGNING_SECRET=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY= \
mvn spring-boot:run
```

Check public health and the unauthenticated OpenAPI path:

```sh
curl http://localhost:8080/actuator/health
curl http://localhost:8080/v3/api-docs
```

Successful health output includes `"status":"UP"`. The unauthenticated OpenAPI request receives HTTP 401 because Spring Security auto-configuration protects `/v3/api-docs` by default; this is the behavior the API sanity test checks.

The frontend lists planned business paths such as `/auth/login`, `/facilities`, `/products`, `/cart` and `/checkout`. They are not implemented in backend source, so do not use their existence in a frontend module as evidence they can be called successfully.

## 3. Automated API checks with REST Assured

REST Assured is a Java library for building HTTP requests and asserting responses. Two test classes use it:

- `backend/src/test/java/com/sporthub/integration/PostgresApplicationIT.java` calls the random-port embedded Spring server while the PostgreSQL Testcontainer is running. It checks the public health status and confirms unauthenticated `/v3/api-docs` access is rejected.
- `qa/automation/src/test/java/com/sporthub/qa/api/PlatformApiTest.java` checks the same real running app from a separately launched test process. That separation exercises the network boundary.

These checks send real HTTP requests to the running server. They assert HTTP status codes and, for health, the public health JSON status. The `/v3/api-docs` 401 is evidence of current default Spring Security behavior, not an application authentication contract. The prior facility-list smoke test only expected `GET /facilities` to work, even though no such controller exists; it has been replaced with checks for routes that really exist.

Run the embedded-server API checks as part of `cd backend && mvn verify`. Run external API checks after starting the app:

```sh
cd qa/automation
SYSTEM_BASE_URL=http://localhost:8080 mvn -Dgroups=api test
```

The successful output should show two tests: a health smoke check and an API sanity check that confirms unauthenticated access to the OpenAPI path is rejected by default security. A health response other than HTTP 200 or status `UP`, or an OpenAPI response other than HTTP 401, fails an assertion.

## 4. API test design for SportHub workflows

When the business endpoints are implemented and documented, cover positive and negative behavior. The response details below are examples of what should be agreed in the contract, not claims about current controller behavior.

| Workflow | Positive check | Negative/boundary checks |
|---|---|---|
| Register/login | Valid adult registration and login return the agreed user/token fields. | Missing email/password, malformed email, under-age account, duplicate email, wrong password. |
| Facilities/slots | List/detail/slot requests return active data for the selected date. | Unknown facility, invalid date, past date, inactive facility. |
| Cart/booking | Authenticated customer adds an available slot/profile. | Missing auth, unowned profile, past/inactive slot, already confirmed slot, duplicate request. |
| Product/cart | Active in-stock product can be added at a positive quantity. | Zero/negative quantity, inactive item, quantity greater than stock. |
| Coupon/checkout | Valid coupon and cart return a consistent server-calculated total. | Invalid/expired coupon, below-minimum subtotal, empty cart, out-of-stock item, payment failure, reused idempotency key with different payload. |
| Administration | Admin can perform an allowed management action. | Anonymous or customer role must not read or mutate admin resources. |

For each API test, write down: preconditions/test data, request method and path, headers, body, expected status/body, and any persisted state to verify. Check error **codes** and stable fields rather than matching a full timestamp or an unstable message.

## 5. Exercise

Look at `frontend/src/api/index.ts`. Pick one planned request. Write down its HTTP method, path and body (if any). Then name the backend class that would need to receive it. If you cannot find that class, mark the API test blocked rather than inventing a route implementation.
