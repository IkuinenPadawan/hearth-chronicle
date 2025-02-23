-- :name get-events :? :*
SELECT * FROM events;

-- :name insert-event :! :n
INSERT INTO events (title, date)
VALUES (:title, CAST(:date AS DATE));

-- :name delete-event :! :n
DELETE FROM events WHERE id = :id;

-- :name update-event :! :n
UPDATE events 
SET title = :title, 
    date = CAST(:date AS DATE)
WHERE id = :id;
