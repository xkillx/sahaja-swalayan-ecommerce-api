-- Flyway Migration Script: Create products table with full fields
CREATE TABLE products (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1024),
    price NUMERIC(19,2) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    weight INTEGER NOT NULL CHECK (weight > 0),
    sku VARCHAR(64),
    height INTEGER CHECK (height > 0),
    length INTEGER CHECK (length > 0),
    width INTEGER CHECK (width > 0),
    category_id UUID NOT NULL,
    image_url TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    CONSTRAINT fk_category
        FOREIGN KEY (category_id)
        REFERENCES categories(id)
);