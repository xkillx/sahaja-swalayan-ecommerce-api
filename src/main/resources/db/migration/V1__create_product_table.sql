-- Flyway Migration Script: Create product table
CREATE TABLE product (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(19,2) NOT NULL,
    stock INTEGER NOT NULL
);