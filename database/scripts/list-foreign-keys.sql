SELECT c.conrelid::regclass AS table_name, c.conname AS constraint_name,
       pg_get_constraintdef(c.oid) AS definition
FROM pg_constraint c WHERE c.contype = 'f' AND c.connamespace = 'public'::regnamespace
ORDER BY 1, 2;
