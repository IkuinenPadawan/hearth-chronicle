(ns hearth-chronicle.db.queries
  (:require [hugsql.core :as hugsql]))

;; Load SQL queries
(hugsql/def-db-fns "hearth_chronicle/db/sql/queries.sql")

