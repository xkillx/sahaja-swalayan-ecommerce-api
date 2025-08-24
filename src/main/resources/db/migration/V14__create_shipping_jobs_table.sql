-- Create table for shipping jobs (background processing of shipping operations)
CREATE TABLE IF NOT EXISTS shipping_jobs (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    last_error VARCHAR(2000),
    next_run_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_shipping_jobs_due ON shipping_jobs(status, next_run_at);
CREATE INDEX IF NOT EXISTS idx_shipping_jobs_order ON shipping_jobs(order_id, created_at);
