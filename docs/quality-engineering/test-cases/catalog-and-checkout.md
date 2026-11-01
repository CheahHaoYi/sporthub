# Catalog, cart and checkout test cases

| ID / scenario | Preconditions and data | Steps | Expected result | Method | Status |
|---|---|---|---|---|---|
| EC-01 Browse active products | One active product exists. | Request product list. | Active product fields are returned. | API/system. | Blocked: product route absent. |
| EC-02 Add valid product quantity | Product has stock at least 2; quantity 2. | Add to cart. | Cart shows quantity 2 and server-calculated line total. | API/integration. | Blocked: cart/product service absent. |
| EC-03 Zero or negative quantity | Active product; quantity 0 or -1. | Submit add/update request. | Validation error; no cart line is persisted. | API negative / boundary. | Blocked. |
| EC-04 Out-of-stock product | Product stock 0 or requested quantity exceeds stock. | Attempt checkout. | Clear rejection; stock remains non-negative and no order is confirmed. | Integration/API. | Blocked: inventory/checkout services absent. |
| EC-05 Valid coupon | Coupon active, current time in range, subtotal meets minimum. | Apply coupon to cart. | Coupon is accepted and discount is calculated on server. | Decision table / API. | Blocked: coupon/cart services absent. |
| EC-06 Expired coupon | Coupon validity end is before current time. | Apply coupon. | Coupon rejected with stable error code; total unchanged. | API negative / boundary. | Blocked. |
| EC-07 Invalid coupon | No coupon with submitted code. | Apply coupon. | Not-found/validation error per contract; total unchanged. | API negative. | Blocked. |
| EC-08 Ineligible child discount | Adult profile added to booking cart. | Preview/checkout. | No child discount is applied; backend uses appointment-date age. | Integration/API. | Pricing classification is implemented; cart/checkout integration blocked. |
| EC-09 Empty cart checkout | Active cart has no items. | Submit checkout. | Checkout rejected; no order/payment/stock changes. | API/integration. | Blocked: checkout service absent. |
| EC-10 Payment failure rollback | Cart has booking/product; payment simulator set to failure. | Submit checkout. | No partial order, booking confirmation, stock decrement or coupon redemption remains. | Transaction integration. | Spring/Postgres rollback primitive is tested; checkout rollback blocked. |
| EC-11 Idempotent retry | Same customer sends same checkout key and request twice. | Repeat checkout after timeout. | One order is created and same result is returned. | API/integration/concurrency. | Schema has a unique key; checkout service absent. |
| EC-12 Cart summary displays server totals | API mock returns item/discount totals. | Render cart page. | UI shows line, coupon and total values from response. | React component test. | Implemented in `Pages.test.tsx` (API mocked). |
