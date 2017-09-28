(ns horizons.main
  "Defines, starts, and stops the Horizons system."
  (:require [com.stuartsierra.component :as component]
            [horizons.web :as web]
            [horizons.core :as core]
            [horizons.telnet.client :as telnet]
            [horizons.telnet.pool :as pool]
            [horizons.telnet.connect :as connect]
            [aero.core :as aero]
            [horizons.parsing.parser :as parser]
            [clojure.java.io :as io]))

(defn horizons-system
  "Build a new Horizons system."
  [config-options]
  (let [{:keys [http-listen-port
                telnet-host
                telnet-port
                grammar-specification]}
        config-options]
    (component/system-map
      :web-server (web/web-server http-listen-port)
      :horizons-client (core/horizons-client)
      :telnet-client (telnet/new-telnet-client)
      :connection-factory (connect/new-connection-factory telnet-host telnet-port)
      :connection-pool (pool/new-connection-pool)
      :parser (parser/new-parser grammar-specification))))

(defn- attach-shutdown-hook
  "Attach a shutdown hook to the given runtime to stop the given system. Returns the system."
  [system ^Runtime runtime]
  (.addShutdownHook runtime (Thread. #(component/stop-system system) "horizons-shutdown-hook"))
  system)

(defn- assoc-if
  "Assocs [key value] to the given map if the value is true."
  [m k v]
  (conj m (when v [k v])))

(defn- apply-cli-args
  "Assocs the give vector of program arguments to the given map"
  [m & [port]]
  (-> m
    (assoc-if :http-listen-port port)))

(defn -main [& more]
  (-> "resources/config.edn"
      aero/read-config
      (apply-cli-args more)
      horizons-system
      component/start-system
      (attach-shutdown-hook (Runtime/getRuntime))))
