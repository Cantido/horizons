(ns horizons.web
  (:require [horizons.core :as horizons]
            [clj-time.format :as f]
            [liberator.core :refer [resource defresource]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-params wrap-json-response]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.util.response :refer [resource-response]]
            [compojure.core :refer :all]
            [compojure.route :as route]))

(def iso-8601-date-time-formatter
  (f/formatters :date-time))

(def iso-8601-date-formatter
  (f/formatters :date))

(defn format-date
  [date]
  (f/unparse iso-8601-date-formatter date))

(defn format-date-time
  [date]
  (f/unparse iso-8601-date-time-formatter date))

(defn iso-format-duration
  [duration]
  (let [years (get duration :years 0)
        months (get duration :months 0)
        days (get duration :days 0)
        hours (get duration :hours 0)
        minutes (get duration :minutes 0)
        seconds (get duration :seconds 0)
        milliseconds (get duration :milliseconds 0)]
    (str "P" years "Y" months "M" days "DT" hours "H" minutes "M" (format "%d.%03dS" seconds milliseconds))))

(defn iso-format-dates
  [tree]
  (clojure.walk/postwalk
    (fn [form]
      (cond
        (not (map? form)) form
        (contains? form :date) (update-in form [:date] format-date)
        (contains? form :timestamp) (update-in form [:timestamp] format-date-time)
        (contains? form :duration) (update-in form [:duration] iso-format-duration)
        :else form))
    tree))


(defresource planetary-body-resource [id]
  :allowed-methods [:get]
  :available-media-types ["application/json"]
  :available-languages ["en-US"]
  :exists? (fn [_] (horizons/supported? id))
  :handle-ok (fn [ctx]
                 (iso-format-dates (horizons/get-planetary-body id)))
  :handle-exception (fn [e]
                        (println e)
                        (liberator.representation/ring-response (resource-response "500.json" {:root "public"}))))

(defroutes handler
  (GET "/" [] (resource-response "index.html" {:root "public"}))
  (ANY ["/bodies/:id", :id #"[0-9]+"] [id] (planetary-body-resource id))
  (route/resources "/"))

(def app
  (-> handler
      wrap-json-body
      wrap-json-params
      wrap-json-response
      wrap-params))
