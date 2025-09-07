-- Add read_at column to notification_events for unread/read support
ALTER TABLE notification_events
  ADD COLUMN IF NOT EXISTS read_at TIMESTAMP NULL;
