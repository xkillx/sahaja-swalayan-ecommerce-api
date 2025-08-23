-- Flyway Migration Script: Create store_settings table
CREATE TABLE IF NOT EXISTS store_settings (
    id UUID PRIMARY KEY,
    store_name VARCHAR(200),
    address_line VARCHAR(500),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    google_maps_api_key VARCHAR(300)
);
