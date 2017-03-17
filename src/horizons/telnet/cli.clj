(ns horizons.telnet.cli
  (:require [clojure.core.async :as async]
            [horizons.telnet.client :as client])
  (:import [java.io BufferedReader
                    InputStreamReader
                    BufferedWriter
                    OutputStreamWriter]))

(defn pipe-stdin-to-telnet []
  (let
    [stdin (new BufferedReader (new InputStreamReader System/in))]
    (async/go-loop []
             (let [got-from-stdin (.read stdin)]
               (println "Got this from STDIN. Char: " (char got-from-stdin) ", int: " (int got-from-stdin) ", type: " (type got-from-stdin))
               (async/>! client/to-telnet got-from-stdin)
               (recur)))))

(defn show-telnet-out []
  (let
    [stdout (new BufferedWriter (new OutputStreamWriter System/out))]
    (async/go-loop []
             (.write stdout ^String (async/<! client/from-telnet))
             (.flush stdout)
             (recur))))
