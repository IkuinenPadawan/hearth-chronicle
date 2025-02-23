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
(def new-event (r/atom {:title "" :date "" :id nil}))
(def editing? (r/atom false))

(defn fetch-events []
  (go
    (let [response (<! (http/get "http://localhost:8081/api/events" {:with-credentials? false}))]
      (when (= 200 (:status response))
        (let [transformed-events (map (fn [event]
                                      {:id (:id event)
                                       :title (:title event)
                                       :start (:date event)})
                                    (:body response))]
          (reset! events transformed-events)
          (when @calendar-instance
            (.removeAllEvents @calendar-instance)
            (.addEventSource @calendar-instance (clj->js @events))))))))

(defn handle-date-click [info]
  (reset! editing? false)
  (swap! new-event assoc :date (.-dateStr info) :title "" :id nil))

(defn handle-event-click [info]
  (let [event (.-event info)
        id (.-id event)
        start (-> event .-start .toISOString (subs 0 10))
        title (.-title event)]
    (reset! editing? true)
    (swap! new-event assoc
           :date start
           :title title
           :id id)))

(defn event-form []
  [:div {:style {:margin-bottom "20px"}}
   [:div {:style {:margin-bottom "10px"}}
    [:label {:style {:display "block" :margin-bottom "5px"}} "Title: "]
    [:input {:style {:padding "5px" :width "200px"}
             :type "text"
             :value (:title @new-event)
             :on-change #(swap! new-event assoc :title (.. % -target -value))
             :required true}]]

   [:div {:style {:margin-bottom "10px"}}
    [:label {:style {:display "block" :margin-bottom "5px"}} "Date: "]
    [:input {:style {:padding "5px" :width "200px"}
             :type "date"
             :value (:date @new-event)
             :on-change #(swap! new-event assoc :date (.. % -target -value))
             :required true}]]

   [:div
    [:button {:style {:padding "5px 10px"
                      :background-color (if @editing? "#ffc107" "#007bff")
                      :color "white"
                      :border "none"
                      :cursor "pointer"
                      :margin-right "10px"}
              :on-click (fn []
                         (go
                           (let [url (if @editing?
                                      (str "http://localhost:8081/api/events/" (:id @new-event))
                                      "http://localhost:8081/api/events")
                                 method (if @editing? http/put http/post)
                                 response (<! (method url
                                                    {:with-credentials? false
                                                     :json-params @new-event}))]
                             (when (or (= 201 (:status response)) 
                                     (= 200 (:status response)))
                               (reset! new-event {:title "" :date "" :id nil})
                               (reset! editing? false)
                               (fetch-events)))))}
     (if @editing? "Update Event" "Add Event")]

    (when @editing?
      [:button {:style {:padding "5px 10px"
                        :background-color "#dc3545"
                        :color "white"
                        :border "none"
                        :cursor "pointer"}
                :on-click (fn []
                           (when (js/confirm "Are you sure you want to delete this event?")
                             (go
                               (let [response (<! (http/delete 
                                                  (str "http://localhost:8081/api/events/" 
                                                       (:id @new-event))
                                                  {:with-credentials? false}))]
                                 (when (= 200 (:status response))
                                   (reset! new-event {:title "" :date "" :id nil})
                                   (reset! editing? false)
                                   (fetch-events))))))}
       "Delete Event"])]])

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
                                           :eventClick handle-event-click
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
  [:div {:style {:backgroundColor "#333" :color "white"}}
   [:h1 "Welcome to Hearth Chronicle"]
   [event-form]
   [calendar-component]])

(defn init []
  (rdom/render [home-page] (.getElementById js/document "app")))
