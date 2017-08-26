(ns horizons.web
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clojure.tools.logging :as log]
            [environ.core :as environ]
            [horizons.core :as h]
            [horizons.parsing.time :as t]
            [immutant.web :as web]
            [liberator.core :refer [defresource]]
            [ring.middleware.defaults :as defaults]
            [ring.middleware.json :refer [wrap-json-body
                                          wrap-json-params
                                          wrap-json-response]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.util.response :as response]))

(defn iso-format-dates
  [tree]
  (clojure.walk/postwalk
    (fn [form]
      (cond
        (not (map? form)) form
        (contains? form ::h/date) (update-in form [::h/date] t/format-date)
        (contains? form ::h/timestamp) (update-in form [::h/timestamp] t/format-date-time)
        (contains? form ::h/duration) (update-in form [::h/duration] t/iso-format-duration)
        :else form))
    tree))

(defn handle-exception [e]
  (log/error e)
  (-> "500.json"
      (response/resource-response {:root "public"})
      (response/status 500)
      (liberator.representation/ring-response)))

(defresource planetary-body-resource [id]
  :allowed-methods [:get]
  :available-media-types ["application/json"]
  :available-languages ["en-US"]
  :exists? (fn [_] (h/supported? id))
  :handle-ok (fn [ctx] (-> id h/get-planetary-body iso-format-dates))
  :handle-exception handle-exception)

(defn handle-ephemeris-ok [id ctx]
  (-> id
      h/get-ephemeris
      iso-format-dates))

(defresource ephemeris-resource [id]
  :allowed-methods [:get]
  :available-media-types ["application/json"]
  :available-languages ["en-US"]
  :exists? (fn [_] (h/supported? id))
  :handle-ok (partial handle-ephemeris-ok id)
  :handle-exception handle-exception)

(defroutes handler
  (GET "/" [] (response/redirect "https://cantido.github.io/horizons/"))
  (context ["/bodies/:id", :id #"[0-9]+"] [id]
    (ANY "/" [] (planetary-body-resource id))
    (ANY "/ephemeris" [] (ephemeris-resource id)))
  (route/not-found (response/not-found "Resource not found.")))

(def handler-options
  (merge
    defaults/api-defaults
    {:static {:resources "public"}}))

(def app
  (-> handler
      (defaults/wrap-defaults handler-options)))

(defn -main [& [port]]
  (let [port (or port (environ/env :port) 3000)]
    (log/info "Starting HORIZONS on port" port)
    (log/debug "DEBUG logging enabled")
    (log/trace "TRACE logging enabled")
    (web/run app
             :host "0.0.0.0"
             :port port
             :path "/")))
