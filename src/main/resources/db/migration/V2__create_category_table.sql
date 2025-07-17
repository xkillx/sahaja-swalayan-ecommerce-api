-- Create Category table
CREATE TABLE category (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT
);

-- Add category_id to product and set up foreign key
ALTER TABLE product ADD COLUMN category_id UUID;
ALTER TABLE product ADD CONSTRAINT fk_product_category FOREIGN KEY (category_id)
    REFERENCES category(id) ON DELETE SET NULL;
