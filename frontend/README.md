# SportHub frontend

Responsive React/TypeScript frontend for sports bookings and retail. The frontend runs independently from the Spring Boot API.

## Requirements and startup

- Node.js 20 or newer
- SportHub API available at port 8080 by default

```sh
cd frontend
npm install
cp .env.example .env
npm run dev
```

Vite serves `http://localhost:5173`. Set `VITE_API_BASE_URL` to the backend API root, including `/api/v1`; the default is `http://localhost:8080/api/v1`.

## Build and tests

```sh
npm run build
npm run test
npm run test:coverage
```

## Architecture and flows

- `src/api/` centralizes Axios, bearer-token injection, standard API error handling, and endpoint modules.
- `src/contexts/AuthContext.tsx` loads `/me`, manages login/registration/logout, and persists the access token and user in local storage.
- `src/layouts/` and `src/routes/` provide responsive role-aware navigation and protected customer/admin routes.
- `src/pages/` contains home, facility and slot booking, product browsing, cart, checkout, account, booking/order history, and admin management pages.
- `src/types/` contains shared API-facing TypeScript types.

Customers can browse API facilities/products, choose a server-returned slot and participant profile, add bookings/products to the shared cart, apply/remove coupons, update/remove cart items, check out with a per-attempt idempotency key, and view/cancel bookings or view orders. Availability, inventory and price calculations remain authoritative on the API. A checkout retry after a timeout reuses the browser's `sporthub_checkout_key` until a successful response.

Administrators receive protected routes for facilities, resources, slots, products, inventory, coupons, discount policies, orders, bookings and users. The contract does not define response DTOs or detailed write schemas for these endpoints; forms use documented routes and conventional request field names. Confirm exact request DTOs with the backend implementation before end-to-end admin use.

## API endpoints consumed

`/auth/register`, `/auth/login`, `/me`, `/customer-profiles`, `/sports`, `/facilities`, `/facilities/{id}`, `/facilities/{id}/slots`, `/products`, `/products/{id}`, `/cart`, `/cart/items/bookings`, `/cart/items/products`, `/cart/items/{itemId}`, `/cart/coupon`, `/checkout`, `/orders`, `/orders/{id}`, `/bookings`, `/bookings/{id}/cancel`, and documented `/admin/*` resources.

## Integration notes and limitations

- Response DTOs are not fully enumerated in `docs/contracts/PROJECT_CONTRACT.md`. List parsing accepts raw arrays and common pagination wrappers (`content`, `items`, `data`, `results`); align individual response fields with backend DTOs.
- The admin contract does not provide field-level write DTOs, nor an identifier path for discount-policy updates. Admin forms use the policy collection route for updates.
- Configure backend CORS for `http://localhost:5173` and use the contract's standard JSON error body.
- JWT persistence uses `localStorage` for this learning application. Production deployments should evaluate stronger token storage and refresh-token security.
- API-provided image URLs are preferred. Stable Unsplash URLs provide sports imagery when no image URL is returned.

## QA selectors

Important forms and flows have stable `data-testid` attributes for authentication, facility and slot selection, participants, product cards, cart totals and coupons, checkout, booking/order history, and admin create actions, alongside semantic labels and accessible names.
