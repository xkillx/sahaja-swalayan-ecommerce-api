-- Relax product quantity constraint to allow out-of-stock (quantity can be 0)
-- This migration assumes the original inline check constraint was named by Postgres as "products_quantity_check"
-- If the name differs in your environment, adjust accordingly.

ALTER TABLE products DROP CONSTRAINT IF EXISTS products_quantity_check;
ALTER TABLE products ADD CONSTRAINT products_quantity_check CHECK (quantity >= 0);
