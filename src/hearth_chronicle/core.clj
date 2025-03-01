(ns hearth-chronicle.core
  (:require [compojure.core :refer [defroutes GET POST DELETE PUT]]
            [compojure.route :as route]
            [hugsql.core :as hugsql]
            [hearth-chronicle.db.queries :as db]
            [hearth-chronicle.handler :as handler]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.not-modified :refer [wrap-not-modified]]
            [ring.util.response :as response]
            [honey.sql :as sql]
            [honey.sql.helpers :as h])
  (:gen-class))

(defroutes app-routes
  (GET "/api/events" [] (handler/get-events))
  (POST "/api/events" {body :body} (handler/add-event body))
  (PUT "/api/events/:id" {body :body {id :id} :route-params} 
       (handler/update-event (assoc body :id (Integer/parseInt id))))
  (DELETE "/api/events/:id" request (handler/delete-event request))
  (GET "/*" [] (response/resource-response "public/index.html"))
  (route/not-found {:error "Not found"}))

(def app
  (-> app-routes
      (wrap-resource "public")
      wrap-content-type
      wrap-not-modified
      wrap-json-response
      (wrap-json-body {:keywords? true})
      (wrap-cors :access-control-allow-origin [#".*"]
                 :access-control-allow-headers ["Content-Type"]
                 :access-control-allow-methods [:get :post :put :delete])))

(defn start-server [port]
  (jetty/run-jetty app {:port port :join? false}))

(defn -main []
  (println "Starting server on port 8081")
  (start-server 8081))
