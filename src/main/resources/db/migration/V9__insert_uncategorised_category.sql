-- Ensure the default 'Uncategorised' category exists for all products without a category
INSERT INTO categories (id, name, description, created_at, updated_at)
VALUES ('00000000-0000-0000-0000-000000000000', 'Uncategorised', 'Default category for uncategorised products', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;
