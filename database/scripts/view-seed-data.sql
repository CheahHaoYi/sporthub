SELECT id, slug, sport_type, name, is_active FROM facilities ORDER BY id;
SELECT id, resource_code, name, facility_id FROM facility_resources ORDER BY facility_id, id;
SELECT id, resource_id, starts_at, ends_at, base_price FROM facility_slots WHERE starts_at > CURRENT_TIMESTAMP ORDER BY starts_at LIMIT 30;
SELECT id, sku, name, price, stock_quantity, is_active FROM products ORDER BY id;
SELECT code, discount_type, discount_value, is_active, valid_from, valid_until, redeemed_count, total_redemption_limit FROM coupons ORDER BY code;
