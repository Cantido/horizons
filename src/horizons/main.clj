(ns horizons.main
  "Defines, starts, and stops the Horizons system."
  (:require [com.stuartsierra.component :as component]
            [horizons.web :as web]
            [horizons.core :as core]
            [horizons.telnet.client :as telnet]
            [horizons.telnet.pool :as pool]
            [horizons.telnet.connect :as connect]
            [aero.core :refer (read-config)]
            [horizons.parsing.parser :as parser]
            [clojure.java.io :as io]))

(defn horizons-system [config-options]
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

(defn- assoc-if
  "Assocs key & value to the given map if the value is true."
  [coll key value]
  (conj coll (when value [key value])))

(defn- apply-cli-args [m & [port]]
  (-> m
    (assoc-if :http-listen-port port)))

(defn -main [& more]
  (let [config (-> "resources/config.edn" read-config (apply-cli-args more))
        system
        (component/start-system
          (horizons-system config))]
    (.addShutdownHook (Runtime/getRuntime) (Thread. #(component/stop-system system) "horizons-shutdown-hook"))))
