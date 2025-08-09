-- Create payments table for Payment entity
CREATE TABLE payments (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    external_id UUID NOT NULL UNIQUE,
    payment_status VARCHAR(20) NOT NULL,
    amount NUMERIC(19,2) NOT NULL CHECK (amount > 0),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    paid_at TIMESTAMP WITHOUT TIME ZONE,
    xendit_invoice_url VARCHAR(500) NOT NULL,
    xendit_callback_token VARCHAR(100) NOT NULL UNIQUE,
    CONSTRAINT fk_payments_order_id FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

CREATE INDEX idx_payments_order_id ON payments(order_id);
