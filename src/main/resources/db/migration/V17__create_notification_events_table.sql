-- Flyway Migration: create notification_events table for persisted feeds
CREATE TABLE IF NOT EXISTS notification_events (
  id UUID PRIMARY KEY,
  user_id UUID NULL,
  audience VARCHAR(50) NULL,
  app VARCHAR(50) NULL,
  type VARCHAR(100) NOT NULL,
  title VARCHAR(200) NULL,
  body VARCHAR(500) NULL,
  data TEXT NULL,
  created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_ne_created_at ON notification_events(created_at);
CREATE INDEX IF NOT EXISTS idx_ne_audience_created_at ON notification_events(audience, created_at);
CREATE INDEX IF NOT EXISTS idx_ne_user_created_at ON notification_events(user_id, created_at);
