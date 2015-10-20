(ns kicker-clj.core
  (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [cljsjs.react :as react]
              [cljs-http.client :as http]
              [cljs.core.async :refer [<!]])
    (:import goog.History))

(defonce state (atom {:name "Jim"}))

;; -------------------------
;; Views

(enable-console-print!)

(defn get-data []
  (go (let [response (<! (http/get "/persons"))]
        (prn (:status response))
        (prn (:body response))
        (swap! state assoc :persons (:body response))
        (prn @state))))

(get-data)

(defn home-page []
  [:div [:h2 "Welcome to kicker-aaclj"]
   [:span (:name @state)]
   [:h3 "Persons"]
   [:ul
    (map (fn [name] [:li {:key name} name]) (:persons @state))]
   [:div [:a {:href "#/about"} "go to about page"]]])

(defn about-page []
  [:div [:h2 "About kicker-clj"]
   [:div [:a {:href "#/"} "go to the home page"]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (mount-root))
