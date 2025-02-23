(ns hearth-chronicle.handler
  (:require [hearth-chronicle.db.queries :as db]
            [hearth-chronicle.config :as config]
            [ring.util.response :as response]))

(defn get-events []
  (-> (db/get-events config/db-config)
      response/response
      (response/content-type "application/json")))

(defn add-event [event]
  (-> (do (db/insert-event config/db-config event)
          {:status "Event added successfully"})
      response/response
      (response/status 201)
      (response/content-type "application/json")))

(defn delete-event [{:keys [route-params]}]
  (let [id (Integer/parseInt (:id route-params))]
    (-> (do (db/delete-event config/db-config {:id id})
            {:status "Event deleted successfully"})
        response/response
        (response/status 200)
        (response/content-type "application/json"))))

(defn update-event [event]
  (-> (do (db/update-event config/db-config event)
          {:status "Event updated successfully"})
      response/response
      (response/status 200)
      (response/content-type "application/json")))
