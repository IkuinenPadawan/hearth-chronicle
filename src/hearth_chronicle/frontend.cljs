(ns hearth-chronicle.frontend
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            ["@fullcalendar/core" :refer [Calendar]]
            ["@fullcalendar/daygrid" :default dayGridPlugin]
            ["@fullcalendar/interaction" :default interactionPlugin]))

(defn handle-date-click [info]
  (js/alert (str "date clicked" (.-dateStr info))))

(defn calendar-component []
  (r/create-class
    {:component-did-mount
     (fn [this]
       (let [calendar-el (.querySelector js/document "#calendar")
             calendar    (Calendar. calendar-el
                                    (clj->js {:plugins [dayGridPlugin interactionPlugin]
                                              :initialView "dayGridMonth"
                                              :dateClick handle-date-click
                                              :events [{:title "Event 1" :start "2025-02-20"}
                                                       {:title "Event 2" :start "2025-02-25"}]}))]
         (.render calendar)))
     :reagent-render
     (fn []
       [:div#calendar])}))

(defn home-page []
  [:div
   [:h1 "Welcome to Hearth Chronicle"]
   [calendar-component]])

(defn init []
  (rdom/render [home-page] (.getElementById js/document "app")))
