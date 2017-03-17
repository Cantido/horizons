(ns horizons.telnet.cli
  (:require [clojure.core.async :refer [alts!! chan go go-loop timeout sliding-buffer <! <!! >! >!!]]
            [horizons.telnet.client :as client])
  (:import [java.io BufferedReader
                    InputStreamReader
                    BufferedWriter
                    OutputStreamWriter]))

(defn pipe-stdin-to-telnet []
  (let
    [stdin (new BufferedReader (new InputStreamReader System/in))]
    (go-loop []
             (let [got-from-stdin (.read stdin)]
               (println "Got this from STDIN. Char: " (char got-from-stdin) ", int: " (int got-from-stdin) ", type: " (type got-from-stdin))
               (>! client/to-telnet got-from-stdin)
               (recur)))))

(defn show-telnet-out []
  (let
    [stdout (new BufferedWriter (new OutputStreamWriter System/out))]
    (go-loop []
             (.write stdout ^String (<! client/from-telnet))
             (.flush stdout)
             (recur))))
