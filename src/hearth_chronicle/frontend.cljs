(ns hearth-chronicle.frontend
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            ["@fullcalendar/core" :refer [Calendar]]
            ["@fullcalendar/daygrid" :default dayGridPlugin]
            ["@fullcalendar/interaction" :default interactionPlugin]
            [cljs-http.client :as http]
            [cljs.core.async :refer [chan go <!]]))


(def events (r/atom []))
(def calendar-instance (r/atom nil))

(defn fetch-events []
  (go
    (let [response (<! (http/get "http://localhost:8081/api/events" {:with-credentials? false}))]
      (when (= 200 (:status response))
        (let [transformed-events (map (fn [event]
                                      {:title (:title event)
                                       :start (:date event)})
                                    (:body response))]
          (reset! events transformed-events)
          (when @calendar-instance
            (.removeAllEvents @calendar-instance)
            (.addEventSource @calendar-instance (clj->js @events))))))))

(defn handle-date-click [info]
  (js/alert (str "date clicked" (.-dateStr info))))

(defn calendar-component []
  (r/create-class
    {:component-did-mount
     (fn [this]
       (let [calendar-el (.querySelector js/document "#calendar")]
         (when calendar-el
           (let [calendar (Calendar. calendar-el
                                   (clj->js {:plugins [dayGridPlugin interactionPlugin]
                                           :initialView "dayGridMonth"
                                           :dateClick handle-date-click
                                           :events @events}))]
             (reset! calendar-instance calendar)
             (.render calendar)
             (fetch-events)))))

     :component-will-unmount
     (fn [this]
       (when @calendar-instance
         (.destroy @calendar-instance)
         (reset! calendar-instance nil)))
     :reagent-render
     (fn []
       [:div#calendar])}))

(defn home-page []
  [:div
   [:h1 "Welcome to Hearth Chronicle"]
   [calendar-component]])

(defn init []
  (rdom/render [home-page] (.getElementById js/document "app")))
