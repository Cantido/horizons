(ns horizons.main
  "Defines, starts, and stops the Horizons system."
  (:require [com.stuartsierra.component :as component]
            [horizons.server :as server]
            [horizons.web :as web]
            [horizons.core :as core]
            [horizons.telnet.client :as telnet]
            [horizons.telnet.pool :as pool]
            [horizons.telnet.connect :as connect]
            [aero.core :as aero]
            [horizons.parsing.parser :as parser]
            [clojure.java.io :as io]))

(defn- bodies []
  (-> "bodies.edn" io/resource io/file slurp read-string))

(defn horizons-system
  "Build a new Horizons system."
  [config-options]
  (let [{:keys [http telnet parser]} config-options]
    (component/system-map
      :web-app (web/web-app)
      :horizons-client (core/horizons-client (bodies))
      :telnet-client (telnet/new-telnet-client)
      :connection-factory (connect/new-connection-factory (:host telnet) (:port telnet) (:timeout telnet))
      :connection-pool (pool/new-connection-pool)
      :parser (parser/new-parser (:grammar-specification parser) (:supported-bodies parser)))))

(defn horizons-system-server
  "Build a new Horizons system that runs inside of a built-in webserver."
  [config-options]
  (let [{:keys [http]} config-options]
    (merge
      (horizons-system config-options)
      {:web-server (server/web-server (:host http) (:port http) (:path http))})))

(defn- attach-shutdown-hook
  "Attach a shutdown hook to the given runtime to stop the given system. Returns the system."
  [system ^Runtime runtime]
  (.addShutdownHook runtime (Thread. #(component/stop system) "horizons-shutdown-hook"))
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

(def lein-ring-handler
  "A handler function wired up with an initialized system,
  so the server can be started with the lein-ring plugin."
  (let [web-app-component (-> "resources/config.edn"
                            aero/read-config
                            horizons-system
                            component/start
                            (attach-shutdown-hook (Runtime/getRuntime))
                            (:web-app))]
    (web/app-handler web-app-component)))


(defn -main [& more]
  (-> "resources/config.edn"
      aero/read-config
      (apply-cli-args more)
      horizons-system-server
      component/start
      (attach-shutdown-hook (Runtime/getRuntime))))
