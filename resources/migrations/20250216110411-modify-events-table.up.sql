ALTER TABLE events RENAME COLUMN event_date TO date;
--;;
ALTER TABLE events ALTER COLUMN start_time DROP NOT NULL;
--;;
ALTER TABLE events ALTER COLUMN end_time DROP NOT NULL;
--;;
ALTER TABLE events DROP CONSTRAINT events_pkey;
--;;
ALTER TABLE events ADD COLUMN all_day BOOLEAN;
