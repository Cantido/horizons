(ns horizons.main
  "Defines, starts, and stops the Horizons system."
  (:require [com.stuartsierra.component :as component]
            [horizons.web :as web]
            [horizons.core :as core]
            [horizons.telnet.client :as telnet]
            [horizons.telnet.pool :as pool]
            [horizons.telnet.connect :as connect]
            [environ.core :as environ]
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

(defn -main [& [port]]
  (component/start-system (horizons-system {:http-listen-port      (or port (environ/env :port) 3000)
                                            :telnet-host           "ssd.jpl.nasa.gov"
                                            :telnet-port           6775
                                            :grammar-specification (io/resource "horizons.bnf")})))
