-- Create addresses table for user shipping addresses
CREATE TABLE addresses (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    label VARCHAR(50) NOT NULL,
    contact_name VARCHAR(100) NOT NULL,
    contact_phone VARCHAR(30) NOT NULL,
    address_line VARCHAR(255) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    area_id VARCHAR(50),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    is_default BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_user_address FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_addresses_user_id ON addresses(user_id);
