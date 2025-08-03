-- Flyway Migration Script: Add image_url to products
ALTER TABLE products ADD COLUMN image_url VARCHAR(512);
