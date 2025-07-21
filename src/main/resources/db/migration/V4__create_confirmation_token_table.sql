CREATE TABLE confirmation_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_confirmation_token_user 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_confirmation_tokens_token ON confirmation_tokens(token);
CREATE INDEX idx_confirmation_tokens_user_id ON confirmation_tokens(user_id);
CREATE INDEX idx_confirmation_tokens_expires_at ON confirmation_tokens(expires_at);
