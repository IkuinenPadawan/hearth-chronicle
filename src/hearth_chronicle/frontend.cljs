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

(defn log-data [prefix data]
  (.log js/console (str prefix ": ") (clj->js data))
  data)

(defn fetch-events []
  (go
    (let [response (<! (http/get "/api/events" {:with-credentials? false}))]
      (.log js/console "API Response:" (clj->js response))
      (when (= 200 (:status response))
        (let [transformed-events (mapv (fn [event]
                                        (.log js/console "Processing event: " (clj->js event))
                                        {:id (str (:id event))  ; Ensure ID is a string
                                         :title (:title event)
                                         :start (:date event)
                                         :allDay true}) ; Use start instead of date for FullCalendar
                                      (:body response))]
          (.log js/console "Transformed events:" (clj->js transformed-events))
          (reset! events transformed-events)
          (when @calendar-instance
            (.log js/console "Calendar instance exists, updating events")
            (try
              ; Completely clear and then add events
              (.removeAllEventSources @calendar-instance)
              (.log js/console "Adding event source:", (clj->js transformed-events))
              (.addEventSource @calendar-instance (clj->js transformed-events))
              (catch js/Error e
                (.error js/console "Error updating calendar:", e)))))))))

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
                                      (str "/api/events/" (:id @new-event))
                                      "/api/events")
                                 method (if @editing? http/put http/post)
                                 response (<! (method url
                                                    {:with-credentials? false
                                                     :json-params @new-event}))]
                             (.log js/console (str (if @editing? "Update" "Add") " response:"), (clj->js response))
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
                                                  (str "/api/events/" 
                                                       (:id @new-event))
                                                  {:with-credentials? false}))]
                                 (.log js/console "Delete response:", (clj->js response))
                                 (when (= 200 (:status response))
                                   (reset! new-event {:title "" :date "" :id nil})
                                   (reset! editing? false)
                                   (fetch-events))))))}
       "Delete Event"])]])
(defn calendar-component []
  (r/create-class
    {:component-did-mount
     (fn [this]
       (.log js/console "Calendar component mounting")
       (let [calendar-el (.getElementById js/document "calendar")]
         (if calendar-el
           (let [calendar-options {:plugins [dayGridPlugin interactionPlugin]
                                  :initialView "dayGridMonth"
                                  :dateClick handle-date-click
                                  :eventClick handle-event-click}
                 calendar (Calendar. calendar-el (clj->js calendar-options))]
             (.log js/console "Calendar created with options:", (clj->js calendar-options))
             (reset! calendar-instance calendar)
             (.render calendar)
             
             ;; Fetch and add events after calendar is initialized
             (js/setTimeout
              (fn []
                (go
                  (let [response (<! (http/get "/api/events" {:with-credentials? false}))]
                    (.log js/console "API Response for calendar:", (clj->js response))
                    (when (= 200 (:status response))
                      (let [transformed-events (mapv (fn [event]
                                                      (let [event-data {:id (str (:id event))
                                                                        :title (:title event)
                                                                        :start (:date event)
                                                                        :allDay true}]
                                                        (.log js/console "Adding event:", (clj->js event-data))
                                                        event-data))
                                                    (:body response))]
                        ;; First clear any existing events
                        (.removeAllEvents calendar)
                        
                        ;; Add each event individually with explicit logging
                        (doseq [event transformed-events]
                          (let [result (.addEvent calendar (clj->js event))]
                            (.log js/console "Event add result:", result)))
                        
                        ;; Verify events were added
                        (let [all-events (.getEvents calendar)]
                          (.log js/console "All events in calendar:", all-events)))))))
              500))
           (.error js/console "Could not find calendar element!"))))
     
     :component-will-unmount
     (fn [this]
       (.log js/console "Calendar component unmounting")
       (when @calendar-instance
         (.destroy @calendar-instance)
         (reset! calendar-instance nil)))
     
     :reagent-render
     (fn []
       [:div {:id "calendar" :style {:height "600px" :margin-top "20px"}}])}))
#_(defn calendar-component []
  (r/create-class
    {:component-did-mount
     (fn [this]
       (.log js/console "Calendar component mounting")
       (let [calendar-el (.getElementById js/document "calendar")]
         (if calendar-el
           (let [calendar (Calendar. 
                           calendar-el 
                           (clj->js 
                            {:plugins [dayGridPlugin interactionPlugin]
                             :initialView "dayGridMonth"
                             :initialEvents [{:title "Test Event"
                                             :start "2025-02-15"
                                             :allDay true}]  ; Test with a hardcoded event
                             :dateClick handle-date-click
                             :eventClick handle-event-click}))]
             (.log js/console "Calendar created with test event")
             (reset! calendar-instance calendar)
             (.render calendar)
             ; After calendar is rendered and initialized, fetch real events
             (js/setTimeout
              (fn []
                (go
                  (let [response (<! (http/get "/api/events" {:with-credentials? false}))]
                    (when (= 200 (:status response))
                      (let [transformed-events (mapv (fn [event]
                                                      {:id (str (:id event))
                                                       :title (:title event)
                                                       :start (:date event)
                                                       :allDay true})
                                                    (:body response))]
                        (.log js/console "Adding transformed events:" (clj->js transformed-events))
                        ; Direct approach - add each event individually
                        (doseq [event transformed-events]
                          (.addEvent @calendar-instance (clj->js event))))))))
              500))
           (.error js/console "Could not find calendar element!"))))

     :component-will-unmount
     (fn [this]
       (when @calendar-instance
         (.destroy @calendar-instance)
         (reset! calendar-instance nil)))
     
     :reagent-render
     (fn []
       [:div {:id "calendar" :style {:height "600px" :margin-top "20px"}}])}))


(defn home-page []
  [:div {:style {:padding "20px" :max-width "1000px" :margin "0 auto"}}
   [:h1 "Welcome to Hearth Chronicle"]
   [event-form]
   [calendar-component]])

(defn init []
  (.log js/console "Initializing application")
  (rdom/render [home-page] (.getElementById js/document "app")))
