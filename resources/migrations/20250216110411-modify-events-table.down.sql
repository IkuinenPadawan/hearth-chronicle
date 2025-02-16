ALTER TABLE events RENAME COLUMN date TO event_date;
--;;
ALTER TABLE events ALTER COLUMN start_time SET NOT NULL;
--;;
ALTER TABLE events ALTER COLUMN end_time SET NOT NULL;
--;;
ALTER TABLE events ADD CONSTRAINT events_pkey FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE;
--;;
ALTER TABLE events DROP COLUMN all_day;
