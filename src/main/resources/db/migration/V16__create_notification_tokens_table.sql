-- Flyway Migration: create notification_tokens table for FCM device tokens
CREATE TABLE IF NOT EXISTS notification_tokens (
  id UUID PRIMARY KEY,
  user_id UUID NULL,
  token VARCHAR(512) NOT NULL UNIQUE,
  platform VARCHAR(50) NULL,
  app VARCHAR(50) NULL,
  user_agent VARCHAR(500) NULL,
  locale VARCHAR(20) NULL,
  revoked BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  last_seen_at TIMESTAMP NULL
);

CREATE INDEX IF NOT EXISTS idx_nt_user ON notification_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_nt_app_revoked ON notification_tokens(app, revoked);
