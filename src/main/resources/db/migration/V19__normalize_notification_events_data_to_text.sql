-- Normalize notification_events.data column to TEXT to avoid Hibernate type mismatches
-- Safe to run multiple times; Postgres will keep TEXT as TEXT.
DO $$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_name = 'notification_events'
      AND column_name = 'data'
  ) THEN
    -- Force column to TEXT explicitly
    EXECUTE 'ALTER TABLE notification_events ALTER COLUMN data TYPE TEXT USING data::text';
  END IF;
END $$;