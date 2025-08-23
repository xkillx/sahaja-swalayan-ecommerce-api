-- Flyway Migration: add enriched shipping/courier fields to orders
ALTER TABLE orders ADD COLUMN IF NOT EXISTS courier_waybill_id VARCHAR(100);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS courier_company VARCHAR(50);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS courier_type VARCHAR(50);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS courier_driver_name VARCHAR(150);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS courier_driver_phone VARCHAR(50);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS courier_driver_plate_number VARCHAR(50);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS courier_driver_photo_url VARCHAR(500);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS courier_link VARCHAR(500);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS shipping_updated_at TIMESTAMP;
