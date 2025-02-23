(ns hearth-chronicle.core
  (:require [compojure.core :refer [defroutes GET POST DELETE PUT]]
            [compojure.route :as route]
            [hugsql.core :as hugsql]
            [hearth-chronicle.db.queries :as db]
            [hearth-chronicle.handler :as handler]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.util.response :as response]
            [ring.middleware.cors :refer [wrap-cors]]
            [honey.sql :as sql]
            [honey.sql.helpers :as h]))

(defroutes app-routes
  (GET "/api/events" [] (handler/get-events))
  (POST "/api/events" {body :body} (handler/add-event body))
  (PUT "/api/events/:id" {body :body {id :id} :route-params} 
       (handler/update-event (assoc body :id (Integer/parseInt id))))
  (DELETE "/api/events/:id" request (handler/delete-event request))
  (route/not-found {:error "Not found"}))

(def app
  (-> app-routes
      wrap-json-response
      (wrap-json-body {:keywords? true})
      (wrap-cors :access-control-allow-origin [#"http://localhost:3000"]
                 :access-control-allow-headers ["Content-Type"]
                 :access-control-allow-methods [:get :post :put :delete])))

(defn start-server [port]
  (jetty/run-jetty app {:port port :join? false}))

(defn -main []
  (println "Starting server on port 8081")
  (start-server 8081))
