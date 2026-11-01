# Entity relationship diagram

The [Mermaid ERD](erd.mmd) covers all 16 tables and their foreign-key relationships. PostgreSQL additionally enforces profile ownership for bookings through a composite `(account_user_id, customer_profile_id)` foreign key.
