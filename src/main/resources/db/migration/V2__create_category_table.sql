-- Create Category table
CREATE TABLE categories (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- Add category_id to product and set up foreign key
ALTER TABLE products ADD COLUMN category_id UUID;
ALTER TABLE products ADD CONSTRAINT fk_product_category FOREIGN KEY (category_id)
    REFERENCES categories(id) ON DELETE SET NULL;
