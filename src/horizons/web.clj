(ns horizons.web
  (:require [compojure.core :as routes]
            [compojure.route :as route]
            [clojure.tools.logging :as log]
            [environ.core :as environ]
            [horizons.core :as horizons]
            [immutant.web :as web]
            [liberator.core :as liberator]
            [ring.middleware.defaults :as defaults]
            [ring.util.response :as response]
            [horizons.parsing.time :as t])
  (:import (org.joda.time DateTime)))

; Since liberator/defresource is a macro, some editors
; (like IntelliJ) will think that the symbols you give
; them are undefined. This declare statement just lets
; my IDE shut up; the symbols ARE declared.
(declare planetary-body-resource ephemeris-resource id)

(defn handle-exception [e]
  (log/error e)
  (-> "500.json"
      (response/resource-response {:root "public"})
      (response/status 500)
      (liberator.representation/ring-response)))

(defn process-params [params]
  (cond-> {}
    (:start params) (assoc :start-datetime (t/normalize-date-string (:start params)))
    (:end params) (assoc :end-datetime (t/normalize-date-string (:end params)))))

(liberator/defresource planetary-body-resource [id]
  :allowed-methods [:get]
  :available-media-types ["application/json"]
  :available-languages ["en-US"]
  :exists? (fn [_] (horizons/supported? id))
  :handle-ok (fn [_] (horizons/get-planetary-body id))
  :handle-exception handle-exception)

(liberator/defresource ephemeris-resource [id]
  :allowed-methods [:get]
  :available-media-types ["application/json"]
  :available-languages ["en-US"]
  :exists? (fn [_] (horizons/supported? id))
  :handle-ok (fn [ctx] (horizons/get-ephemeris id (process-params (get-in ctx [:request :params]))))
  :handle-exception handle-exception)

(routes/defroutes handler
  (routes/GET "/" [] (response/redirect "https://cantido.github.io/horizons/"))
  (routes/context ["/bodies/:id", :id #"[0-9]+"] [id]
    (routes/ANY "/" [] (planetary-body-resource id))
    (routes/ANY "/ephemeris" [] (ephemeris-resource id)))
  (route/not-found (response/not-found "Resource not found.")))

(def app
  (-> handler
      (defaults/wrap-defaults
        (assoc defaults/api-defaults :static {:resources "public"}))))

(defn -main [& [port]]
  (let [port (or port (environ/env :port) 3000)]
    (log/info "Starting HORIZONS on port" port)
    (log/debug "DEBUG logging enabled")
    (log/trace "TRACE logging enabled")
    (web/run app
             :host "0.0.0.0"
             :port port
             :path "/")))
