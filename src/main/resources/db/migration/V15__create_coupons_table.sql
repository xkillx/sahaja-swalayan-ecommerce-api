-- Create coupons table for discount codes
CREATE TABLE IF NOT EXISTS coupons (
    id UUID PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    type VARCHAR(16) NOT NULL, -- PERCENT or FIXED (enum stored as string)
    value NUMERIC(19,2) NOT NULL,
    min_spend NUMERIC(19,2),
    active BOOLEAN NOT NULL,

    -- audit fields (from AuditableEntity)
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);
