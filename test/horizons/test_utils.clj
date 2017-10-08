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
            [horizons.parsing.parser :as parser]))

(defn slurp-resource [s]
  (slurp (io/file (io/resource s))))

(defn build-test-system
  "Builds a Horizons system that will read data from
  the given resource instead of from Telnet."
  ([] (build-test-system asu/closed-chan asu/closed-chan))
  ([s] (build-test-system (asu/dropping-channel) (async/to-chan (slurp-resource s))))
  ([to-telnet from-telnet]
   (->
     (component/system-map
       :web-server {}
       :horizons-client (core/horizons-client)
       :telnet-client (telnet/new-telnet-client)
       :connection-factory (connect/new-connection-factory "bad-host" 0)
       :connection-pool (pool/new-connection-pool to-telnet from-telnet)
       :parser (parser/new-parser (io/resource "horizons.bnf")))
     (component/start-system))))
