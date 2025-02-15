(ns hearth-chronicle.core
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [hugsql.core :as hugsql]
            [hearth-chronicle.db.queries :as db]
            [hearth-chronicle.handler :as handler]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.util.response :as response]
            [honey.sql :as sql]
            [honey.sql.helpers :as h]))

(defroutes app-routes
  (GET "/events" [] (handler/get-events))
  (POST "/events" {body :body} (handler/add-event body))
  (route/not-found {:error "Not found"}))

(def app
  (-> app-routes
      wrap-json-response
      (wrap-json-body {:keywords? true})))

(defn start-server [port]
  (jetty/run-jetty app {:port port :join? false}))

(defn -main []
  (println "Starting server on port 3000...")
  (start-server 3000))
