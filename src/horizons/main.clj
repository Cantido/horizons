(ns horizons.main
  "Defines, starts, and stops the Horizons system."
  (:require [com.stuartsierra.component :as component]
            [horizons.web :as web]
            [horizons.core :as core]
            [horizons.telnet.client :as telnet]
            [horizons.telnet.pool :as pool]
            [horizons.telnet.connect :as connect]))

(defn horizons-system [config-options]
  (let [{:keys [port]} config-options]
    (component/system-map
      :web-server (web/web-server port)
      :horizons-client (core/horizons-client)
      :telnet-client (telnet/new-telnet-client)
      :connection-factory (connect/new-connection-factory)
      :connection-pool (pool/new-connection-pool))))

(defn -main [& [port]]
  (component/start-system (horizons-system {:port port})))
