(ns horizons.web
  (:require [compojure.core :as routes]
            [compojure.route :as route]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [horizons.core :as horizons]
            [immutant.web :as web]
            [liberator.core :as liberator]
            [ring.middleware.defaults :as defaults]
            [ring.util.response :as response]
            [horizons.parsing.time :as t]))

(defn- handle-exception [web-server-component e]
  (log/error e)
  (-> "500.json"
      (response/resource-response {:root "public"})
      (response/status 500)
      (liberator.representation/ring-response)))

(defn- planetary-body-resource [web-server-component id]
  (liberator/resource
    :allowed-methods [:get]
    :available-media-types ["application/json"]
    :available-languages ["en-US"]
    :exists? (fn [_] (horizons/supported? (:horizons-client web-server-component) id))
    :handle-ok (fn [_] (horizons/get-planetary-body (:horizons-client web-server-component) id))
    :handle-exception (partial handle-exception web-server-component)))

(defn- ephemeris-options [horizons-client m]
  (-> m
    (get-in [:request :params])
    (select-keys #{:start :end :step-size})))

(defn- ephemeris-resource [web-server-component id]
  (liberator/resource
    :allowed-methods [:get]
    :available-media-types ["application/json"]
    :available-languages ["en-US"]
    :exists? (fn [_] (horizons/supported? (:horizons-client web-server-component) id))
    :handle-ok (fn [ctx] (horizons/get-ephemeris (:horizons-client web-server-component) id (ephemeris-options ephemeris-resource ctx)))
    :handle-exception (partial handle-exception web-server-component)))

(defn- app-routes [web-server-component]
  (routes/routes
    (routes/GET "/" [] (response/redirect "https://cantido.github.io/horizons/"))
    (routes/context ["/bodies/:id", :id #"[0-9]+"] [id]
      (routes/ANY "/" [] (planetary-body-resource web-server-component id))
      (routes/ANY "/ephemeris" [] (ephemeris-resource web-server-component id)))
    (route/not-found (response/not-found "Resource not found."))))

(defn app-handler [web-server-component]
  (-> (app-routes web-server-component)
      (defaults/wrap-defaults
        (assoc defaults/api-defaults :static {:resources "public"}))))

(def app (app-handler {}))


(defrecord WebServer [http-server horizons-client]
  component/Lifecycle
  (start [this]
    (assoc this :http-server
      (let [port (:port this)]
        (log/info "Starting HORIZONS on port" port)
        (log/debug "DEBUG logging enabled")
        (log/trace "TRACE logging enabled")
        (web/run (app-handler this)
                 :host "0.0.0.0"
                 :port port
                 :path "/"))))
  (stop [this]
    (web/stop http-server)
    this))

(defn web-server [port]
  (component/using (map->WebServer {:port port}) [:horizons-client]))
