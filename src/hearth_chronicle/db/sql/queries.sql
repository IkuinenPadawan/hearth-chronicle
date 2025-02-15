-- :name get-events :? :*
SELECT * FROM events;

-- :name insert-event :! :n
INSERT INTO events (title, description, event_date, start_time, end_time)
VALUES (:title, :description, CAST(:event_date AS DATE), CAST(:start_time AS TIME), CAST(:end_time AS TIME));
