(ns horizons.server
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.logging :as log]
            [ring.adapter.jetty :as jetty]
            [horizons.web :as web]))

(defrecord WebServer [web-app http-server host port path]
  component/Lifecycle
  (start [this]
    (log/info "Starting HORIZONS on port" port)
    (log/debug "DEBUG logging enabled")
    (log/trace "TRACE logging enabled")
    (assoc this :http-server
                (jetty/run-jetty (web/app-handler web-app)
                           {:host host
                            :port port
                            :path path})))
  (stop [this]
    (.stop http-server)
    this))

(defn web-server [host port path]
  (component/using
    (map->WebServer {:host host :port port :path path})
    [:web-app]))
