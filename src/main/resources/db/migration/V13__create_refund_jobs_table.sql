-- Create table for refund jobs (background processing of refunds)
CREATE TABLE IF NOT EXISTS refund_jobs (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    amount NUMERIC(18,2) NOT NULL,
    reason VARCHAR(500),
    status VARCHAR(30) NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    last_error VARCHAR(2000),
    next_run_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_refund_jobs_due ON refund_jobs(status, next_run_at);
CREATE INDEX IF NOT EXISTS idx_refund_jobs_order ON refund_jobs(order_id, created_at);
