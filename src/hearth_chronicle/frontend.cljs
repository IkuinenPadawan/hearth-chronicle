(ns hearth-chronicle.frontend
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]))

(defn home-page []
  [:div
   [:h1 "Welcome to Hearth Chronicle"]
   [:p "Here will be the chronicle."]])

(defn init []
  (rdom/render [home-page] (.getElementById js/document "app")))
