(ns horizons.test-utils
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [horizons.async-utils :as asu]
            [clojure.core.async :as async]
            [horizons.main :as main]
            [com.stuartsierra.component :as component]
            [horizons.web :as web]
            [horizons.core :as core]
            [horizons.telnet.client :as telnet]
            [horizons.telnet.connect :as connect]
            [horizons.telnet.pool :as pool]
            [horizons.parsing.parser :as parser]
            [clojure.string :as string]))

(defn slurp-resource
  [s]
  {:pre [(complement string/blank?)]
   :post [(comp not string/blank?)]}
  (slurp (io/file (io/resource s))))

(defn build-test-system
  "Builds a (started) Horizons system that will read data from stubbed-out channels instead of from Telnet.

  - When given no arguments, the given system will read from and write to closed channels.

  - When given a single string argument, takes that string as a resource name, and
  the given system will be given the file through the from-telnet channel. The
  to-telnet channel will drop all input but always return true.

  - When given two channels, the system will use them as to-telnet and from-telnet connections."
  ([] (build-test-system asu/closed-chan asu/closed-chan))
  ([s] (build-test-system (asu/dropping-channel) (async/to-chan (slurp-resource s))))
  ([to-telnet from-telnet]
   (->
     (component/system-map
       :web-server {}
       :web-app (web/web-app)
       :horizons-client (core/horizons-client)
       :telnet-client (telnet/new-telnet-client)
       :connection-factory (connect/new-connection-factory "bad-host" 0 0)
       :connection-pool (pool/new-connection-pool to-telnet from-telnet)
       :parser (parser/new-parser (io/resource "horizons.bnf")))
     (component/start-system))))
