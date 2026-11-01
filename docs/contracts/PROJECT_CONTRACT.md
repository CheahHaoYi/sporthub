# SportHub — Shared Project Contract

## 1. Project objective

Build a commercial sports facility booking and e-commerce platform using React, Java Spring Boot and PostgreSQL.

The platform enables customers to browse sports facilities, reserve courts and appointment slots, purchase sports equipment, apply promotional discount codes and complete bookings and product purchases through a shared checkout.

The primary engineering objective is to create a realistic application for practising Quality Engineering, including unit testing, integration testing, API testing, end-to-end testing, concurrency testing, performance testing and CI/CD.

## 2. Fixed technology stack

### Frontend
- React
- Vite
- JavaScript or TypeScript (prefer TypeScript)
- React Router
- Axios
- Vitest
- React Testing Library

### Backend
- Java (choose a current LTS version compatible with the selected Spring Boot version)
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- Spring Validation
- Spring Actuator
- Maven
- PostgreSQL JDBC driver
- Flyway

### Database
- PostgreSQL
- SQL migrations managed by Flyway
- NUMERIC(12,2) for currency
- TIMESTAMPTZ for appointment timestamps

### Quality Engineering
- TestNG
- Mockito
- Spring TestContext
- REST Assured
- Selenium WebDriver
- JMeter
- Testcontainers PostgreSQL
- JaCoCo

### DevOps
- Docker
- Docker Compose
- GitHub Actions

Choose compatible stable dependency versions. Do not use arbitrary combinations of framework versions. Document the versions selected.

## 3. Domain model and user classification

There are two application access roles:

- CUSTOMER
- ADMIN

Customer pricing classifications are separate from access roles.

Age classification is calculated from the customer's date of birth and the appointment start date.

| Classification | Age at appointment | Default facility discount |
|---|---:|---:|
| CHILD | 0–17 | 20% |
| ADULT | 18–59 | 0% |
| SENIOR | 60+ | 15% |

These are configurable demonstration policies.

An authenticated adult customer can create dependent profiles for children. Children do not need independent login accounts.

Each customer profile belongs to an authenticated account.

A customer may book an appointment for their own profile or a dependent profile that they own.

Do not trust the frontend to determine or submit a discount category or discount percentage. The backend must calculate eligibility and pricing.

## 4. Core business modules

### A. Authentication and customer profiles

- Registration
- Login and logout
- JWT authentication
- Customer profile retrieval
- Dependent child profile creation
- Role-based access control
- Account suspension
- Profile ownership validation

Only users aged 18 or above may register their own customer accounts.

### B. Sports facilities and bookings

Sports types:
- Badminton
- Tennis
- Basketball
- Futsal
- Swimming

A facility contains one or more individual resources, such as badminton courts or tennis courts.

Each resource has appointment slots with:
- Start time
- End time
- Base price
- Active/blocked state
- Capacity

For the MVP, each individual court slot has a capacity of ONE booking.

Multiple different court slots can exist at the same time.

Booking rules:
1. No bookings in the past.
2. No duplicate active booking for the same slot.
3. No booking beyond slot capacity.
4. A cancelled booking releases its slot.
5. Customers cannot cancel bookings belonging to other customers.
6. Inactive facilities cannot be booked.
7. Checkout must revalidate slot availability.

### C. Products and inventory

Product categories:
- Rackets
- Balls
- Shoes
- Sports apparel
- Accessories
- Drinks

Products have:
- Name
- Description
- SKU
- Category
- Price
- Stock quantity
- Active/inactive status
- Image URL

Inventory rules:
1. Product quantities must be positive integers.
2. Customers cannot buy more than the available stock.
3. Inactive products cannot be purchased.
4. Stock only decreases after successful checkout.
5. Concurrent purchases must never produce negative stock.
6. Failed checkout must not decrement stock.

Products are assumed to be collected at the sports facility. No shipping integration is required.

### D. Cart

Each authenticated customer has one active cart.

A cart can contain:
- Facility booking items
- Product purchase items

Booking cart items contain:
- Facility slot ID
- Customer profile ID

Product cart items contain:
- Product ID
- Quantity

Cart pricing is always calculated by the backend.

Adding an item to the cart does not reserve a court or product inventory. Availability and stock are confirmed during checkout.

The UI must display a current server-calculated price breakdown.

### E. Promotions and discounts

Two different discount mechanisms exist.

1. Customer category discounts:
   - CHILD receives 20% off facility booking charges.
   - SENIOR receives 15% off facility booking charges.
   - ADULT receives no age-based discount.

2. Promotional coupons:
   - Apply only to eligible product purchases.
   - Support percentage and fixed-value discounts.
   - Support activation and expiry dates.
   - Support minimum eligible subtotal.
   - Support total redemption limit.
   - Support per-customer redemption limit.
   - Support maximum discount amount.

Seed demonstration coupons:
- SPORT10: 10% off eligible products.
- WELCOME5: SGD 5 off eligible products, subject to a minimum product subtotal of SGD 20.

Coupon values and policies must be configurable.

Age discounts and promotional product discounts may both appear in the same order because they apply to different item types.

### F. Checkout and orders

A customer can purchase multiple bookings and products in one order.

Checkout sequence:
1. Validate authenticated customer.
2. Check idempotency key.
3. Retrieve current cart contents.
4. Lock relevant facility slot rows.
5. Lock relevant product inventory rows.
6. Revalidate slot availability.
7. Revalidate product stock.
8. Recalculate all prices and discounts.
9. Revalidate coupon eligibility and redemption limits.
10. Create an order and order items.
11. Create confirmed bookings.
12. Decrement product stock.
13. Record coupon redemption.
14. Mark the simulated payment successful.
15. Commit all changes in one database transaction.

If any step fails, the transaction must roll back completely.

Use an idempotency key on checkout requests. Repeating the same request with the same key must not create duplicate orders.

A conflicting request with a reused key and different contents must be rejected.

Use PostgreSQL row-level locking or an equally robust concurrency-safe approach. The application must not rely solely on frontend validation or pre-checking availability.

### G. Simulated payment

No real payment gateway is required.

Implement a simulated payment service with a configurable outcome:
- SUCCESS
- FAILURE

A successful payment confirms an order.

A failed payment must roll back order creation, booking confirmation, inventory deductions and coupon redemption.

Never collect or store real credit card details.

## 5. API contract

All endpoints use the `/api/v1` prefix.

### Authentication

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/auth/register` | Register customer account |
| POST | `/auth/login` | Authenticate |
| GET | `/me` | Get authenticated account |

Registration request:

```json
{
  "email": "customer@example.com",
  "password": "Password123!",
  "fullName": "John Tan",
  "dateOfBirth": "1995-06-10"
}
```

Login request:

```json
{
  "email": "customer@example.com",
  "password": "Password123!"
}
```

Login response includes:
- accessToken
- tokenType
- expiresInSeconds
- user object with id, email and role

### Customer profiles

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/customer-profiles` | List owned profiles |
| POST | `/customer-profiles` | Create dependent profile |
| GET | `/customer-profiles/{id}` | View owned profile |

Dependent profile request:

```json
{
  "fullName": "Child Tan",
  "dateOfBirth": "2015-05-01"
}
```

The backend derives the dependent's age category.

### Facilities

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/sports` | List sport categories |
| GET | `/facilities` | Browse facilities |
| GET | `/facilities/{id}` | Get facility details |
| GET | `/facilities/{id}/slots?date=2026-11-10` | Get slots for date |

### Products

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/products` | Browse/filter products |
| GET | `/products/{id}` | Product details |

### Cart

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/cart` | Get current cart |
| POST | `/cart/items/bookings` | Add booking item |
| POST | `/cart/items/products` | Add product item |
| PATCH | `/cart/items/{itemId}` | Update product quantity |
| DELETE | `/cart/items/{itemId}` | Remove cart item |
| POST | `/cart/coupon` | Apply promotional code |
| DELETE | `/cart/coupon` | Remove coupon |

Booking cart request:

```json
{
  "slotId": 101,
  "customerProfileId": 5
}
```

Product cart request:

```json
{
  "productId": 25,
  "quantity": 2
}
```

Coupon request:

```json
{
  "code": "SPORT10"
}
```

### Checkout and orders

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/checkout` | Submit checkout |
| GET | `/orders` | List customer orders |
| GET | `/orders/{id}` | View order details |
| GET | `/bookings` | List customer bookings |
| POST | `/bookings/{id}/cancel` | Cancel customer booking |

Checkout request must include:

```http
Idempotency-Key: unique-client-generated-key
```

The body can be empty. The server reads the cart and recalculates all prices.

### Admin endpoints

All admin endpoints require ADMIN privileges.

| Method | Endpoint | Purpose |
|---|---|---|
| GET/POST | `/admin/facilities` | List/create facilities |
| PATCH | `/admin/facilities/{id}` | Update facility |
| GET/POST | `/admin/resources` | List/create courts/resources |
| PATCH | `/admin/resources/{id}` | Update resource |
| GET/POST | `/admin/slots` | List/create slots |
| PATCH | `/admin/slots/{id}` | Update slot |
| GET/POST | `/admin/products` | List/create products |
| PATCH | `/admin/products/{id}` | Update product |
| PATCH | `/admin/products/{id}/inventory` | Adjust stock |
| GET/POST | `/admin/coupons` | List/create coupons |
| PATCH | `/admin/coupons/{id}` | Update coupon |
| GET | `/admin/orders` | List all orders |
| GET | `/admin/bookings` | List all bookings |
| PATCH | `/admin/bookings/{id}` | Manage booking status |
| GET/POST/PATCH | `/admin/discount-policies` | Manage pricing policies |
| GET | `/admin/users` | View customer accounts |

### API conventions

- Use JSON for request and response bodies.
- Use ISO 8601 timestamps with explicit time-zone offsets.
- Use UUIDs or consistent numeric IDs throughout the API and database.
- Currency is SGD.
- Serialize monetary values as decimal strings to avoid frontend floating-point errors. Example: `"45.50"`.
- Include pagination on listing endpoints.
- Apply validation to request DTOs.
- Return appropriate HTTP status codes.
- Never expose password hashes or internal security information.

### Standard error response

All backend errors must follow this format:

```json
{
  "timestamp": "2026-11-01T16:00:00+08:00",
  "status": 409,
  "code": "SLOT_ALREADY_BOOKED",
  "message": "The selected appointment slot is no longer available.",
  "path": "/api/v1/checkout",
  "fieldErrors": []
}
```

Use relevant status codes:
- 200: successful retrieval/update
- 201: successful creation
- 400: invalid input
- 401: unauthenticated
- 403: forbidden
- 404: not found
- 409: booking, inventory, coupon or idempotency conflict
- 500: unexpected server error

## 6. Integration conventions

All agents must follow these rules.

1. Do not rename API paths, properties or status values independently.
2. Backend is the source of truth for business calculations.
3. PostgreSQL schema is managed by Flyway SQL migrations.
4. Spring Hibernate configuration must use `ddl-auto=validate`, never automatic schema updates.
5. Database migrations must be compatible with the backend entity mappings.
6. Frontend must consume the agreed JSON API contract.
7. React must never directly connect to PostgreSQL.
8. Keep the backend and frontend independently runnable.
9. Keep database configuration environment-based.
10. Use consistent error response structures.
11. Keep timestamps timezone-aware.
12. Do not introduce microservices, message brokers or unnecessary infrastructure.

## 7. QA learning requirements

The final application must support meaningful test cases for:

- Positive and negative testing.
- Boundary-value analysis.
- Equivalence partitioning.
- Authentication and authorisation.
- Input validation.
- Unit tests.
- Integration tests.
- API automation.
- UI/system/E2E automation.
- Database constraints.
- Duplicate requests.
- Race conditions.
- Inventory consistency.
- Pricing calculations.
- Coupon redemption limits.
- Performance and load testing.
- CI/CD quality gates.

A key acceptance scenario:

When two customers attempt to purchase the final product unit simultaneously, exactly one purchase must succeed.

Another key acceptance scenario:

When two customers attempt to book the same court slot simultaneously, exactly one booking must succeed.

No race condition may create duplicate active bookings, negative stock or inconsistent orders.

## 8. Common demo data

Provide:
- At least 5 facilities across different sports.
- At least 2 resources for a popular sport.
- At least 10 products.
- Child, adult and senior customer profiles.
- At least 2 demonstration coupons.
- Multiple future booking slots.
- One administrator account.
- Customer accounts for adult and senior demonstrations.

Demo users must be generated only in a development profile. Use password hashing and document the credentials.

Do not store production credentials in the repository.

## 9. Definition of done

A feature is not complete merely because the code compiles.

Each agent must provide:
1. Working implementation.
2. Automated tests appropriate to its component.
3. README with startup instructions.
4. Environment/configuration documentation.
5. Error handling.
6. Sample/demo data where applicable.
7. Integration instructions.
8. List of completed functionality.
9. Known limitations.
10. Commands to build, test and run.

Do not leave placeholder implementations, fake success responses, unimplemented TODO methods or mocked business logic in the actual application.
