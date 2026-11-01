INSERT INTO product_categories(name,slug) VALUES
 ('Rackets','rackets'),('Balls','balls'),('Shoes','shoes'),('Apparel','apparel'),('Accessories','accessories'),('Drinks','drinks');

INSERT INTO discount_policies(customer_category,min_age_inclusive,max_age_inclusive,discount_percentage) VALUES
 ('CHILD',0,17,20),('ADULT',18,59,0),('SENIOR',60,NULL,15);

INSERT INTO facilities(slug,sport_type,name,description,image_url) VALUES
 ('badminton-centre','BADMINTON','Badminton Centre','Indoor courts with tournament-grade flooring.','https://images.example.com/sporthub/badminton.jpg'),
 ('tennis-centre','TENNIS','Tennis Centre','Outdoor and covered courts for all levels.','https://images.example.com/sporthub/tennis.jpg'),
 ('basketball-court','BASKETBALL','Basketball Court','Full-size court with evening lighting.','https://images.example.com/sporthub/basketball.jpg'),
 ('futsal-arena','FUTSAL','Futsal Arena','Five-a-side arena with quality turf.','https://images.example.com/sporthub/futsal.jpg'),
 ('swimming-centre','SWIMMING','Swimming Centre','Heated lap pool with eight lanes.','https://images.example.com/sporthub/swimming.jpg');
INSERT INTO facility_resources(facility_id,resource_code,name) VALUES
 ((SELECT id FROM facilities WHERE slug='badminton-centre'),'COURT-1','Badminton Court 1'),
 ((SELECT id FROM facilities WHERE slug='badminton-centre'),'COURT-2','Badminton Court 2'),
 ((SELECT id FROM facilities WHERE slug='tennis-centre'),'COURT-1','Tennis Court 1'),
 ((SELECT id FROM facilities WHERE slug='tennis-centre'),'COURT-2','Tennis Court 2'),
 ((SELECT id FROM facilities WHERE slug='basketball-court'),'COURT-1','Basketball Court 1'),
 ((SELECT id FROM facilities WHERE slug='futsal-arena'),'PITCH-1','Futsal Pitch 1'),
 ((SELECT id FROM facilities WHERE slug='swimming-centre'),'LANE-1','Swimming Lane 1'),
 ((SELECT id FROM facilities WHERE slug='swimming-centre'),'LANE-2','Swimming Lane 2');

-- Five days of recurring slots, anchored in Singapore local time to stay future-dated.
INSERT INTO facility_slots(resource_id,starts_at,ends_at,base_price,capacity)
SELECT r.id, (d.day::date + t.start_time) AT TIME ZONE 'Asia/Singapore',
       (d.day::date + t.end_time) AT TIME ZONE 'Asia/Singapore', t.price, 1
FROM facility_resources r
CROSS JOIN generate_series((CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Singapore')::date + 1,
                           (CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Singapore')::date + 5, interval '1 day') AS d(day)
CROSS JOIN (VALUES (TIME '09:00',TIME '10:00', '18.00'::numeric),
                   (TIME '18:00',TIME '19:00', '24.00'::numeric),
                   (TIME '19:00',TIME '20:00', '24.00'::numeric)) AS t(start_time,end_time,price)
ON CONFLICT(resource_id,starts_at,ends_at) DO NOTHING;

INSERT INTO products(category_id,sku,name,description,image_url,price,stock_quantity,is_active) VALUES
 ((SELECT id FROM product_categories WHERE slug='rackets'),'RKT-BAD-001','Badminton Racket','Lightweight graphite racket.','https://images.example.com/products/badminton-racket.jpg',89.90,15,TRUE),
 ((SELECT id FROM product_categories WHERE slug='rackets'),'RKT-TEN-001','Tennis Racket','All-round 100 sq in racket.','https://images.example.com/products/tennis-racket.jpg',149.00,8,TRUE),
 ((SELECT id FROM product_categories WHERE slug='balls'),'BALL-BSK-001','Basketball','Size 7 indoor/outdoor basketball.','https://images.example.com/products/basketball.jpg',35.00,20,TRUE),
 ((SELECT id FROM product_categories WHERE slug='balls'),'SHUT-001','Shuttlecock Tube','Tube of six feather shuttlecocks.','https://images.example.com/products/shuttlecock.jpg',24.50,30,TRUE),
 ((SELECT id FROM product_categories WHERE slug='balls'),'BALL-TEN-001','Tennis Ball Pack','Pack of three pressurised balls.','https://images.example.com/products/tennis-balls.jpg',12.90,25,TRUE),
 ((SELECT id FROM product_categories WHERE slug='shoes'),'SHOE-COURT-001','Court Sports Shoes','Non-marking court shoes.','https://images.example.com/products/court-shoes.jpg',79.00,7,TRUE),
 ((SELECT id FROM product_categories WHERE slug='apparel'),'SOCK-SPORT-001','Sports Socks','Breathable training socks.','https://images.example.com/products/socks.jpg',9.90,40,TRUE),
 ((SELECT id FROM product_categories WHERE slug='apparel'),'SHIRT-SPORT-001','Sports Shirt','Moisture-wicking training shirt.','https://images.example.com/products/shirt.jpg',29.90,18,TRUE),
 ((SELECT id FROM product_categories WHERE slug='accessories'),'BOTTLE-750','Water Bottle','750 ml BPA-free bottle.','https://images.example.com/products/bottle.jpg',14.90,22,TRUE),
 ((SELECT id FROM product_categories WHERE slug='accessories'),'TOWEL-SPORT','Sports Towel','Quick-dry microfibre towel.','https://images.example.com/products/towel.jpg',18.00,12,TRUE),
 ((SELECT id FROM product_categories WHERE slug='drinks'),'DRINK-ISO-001','Isotonic Drink','500 ml isotonic sports drink.','https://images.example.com/products/isotonic.jpg',2.50,1,TRUE),
 ((SELECT id FROM product_categories WHERE slug='apparel'),'CAP-OLD-001','Classic Sports Cap','Discontinued cap retained for catalogue testing.','https://images.example.com/products/cap.jpg',19.90,0,FALSE);

INSERT INTO coupons(code,discount_type,discount_value,minimum_eligible_subtotal,maximum_discount_amount,valid_from,valid_until,total_redemption_limit,per_customer_redemption_limit,redeemed_count,is_active) VALUES
 ('SPORT10','PERCENTAGE',10,0,30,CURRENT_TIMESTAMP - INTERVAL '1 day',CURRENT_TIMESTAMP + INTERVAL '365 days',1000,1,0,TRUE),
 ('WELCOME5','FIXED',5,20,NULL,CURRENT_TIMESTAMP - INTERVAL '1 day',CURRENT_TIMESTAMP + INTERVAL '365 days',500,1,0,TRUE),
 ('EXPIRED10','PERCENTAGE',10,0,NULL,CURRENT_TIMESTAMP - INTERVAL '30 days',CURRENT_TIMESTAMP - INTERVAL '1 day',NULL,1,0,TRUE),
 ('INACTIVE5','FIXED',5,0,NULL,CURRENT_TIMESTAMP - INTERVAL '1 day',CURRENT_TIMESTAMP + INTERVAL '365 days',NULL,1,0,FALSE),
 ('EXHAUSTED5','FIXED',5,0,NULL,CURRENT_TIMESTAMP - INTERVAL '1 day',CURRENT_TIMESTAMP + INTERVAL '365 days',1,1,1,TRUE),
 ('LIMITED10','PERCENTAGE',10,0,20,CURRENT_TIMESTAMP - INTERVAL '1 day',CURRENT_TIMESTAMP + INTERVAL '365 days',5,1,0,TRUE);
