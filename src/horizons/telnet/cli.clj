(ns horizons.telnet.cli
  "Some functions that are useful when debugging the Telnet connection in a REPL."
  (:require [clojure.core.async :as async]
            [clojure.java.io :as io]
            [horizons.telnet.client :as client])
  (:import [java.io BufferedReader
                    InputStreamReader
                    BufferedWriter
                    OutputStreamWriter]))

(defn pipe-stdin-to-telnet
  "Starts a loop in another thread that reads from stdin and writes it to the telnet service."
  []
  (let [stdin (io/reader *in*)]
    (async/go-loop []
             (let [got-from-stdin (.read stdin)]
               (async/>! client/to-telnet got-from-stdin)
               (recur)))))

(defn show-telnet-out
  "Starts a loop in another thread that reads from the Telnet service and writes it to stdout."
  []
  (let
    [stdout (io/writer *out*)]
    (async/go-loop []
             (.write stdout ^String (async/<! client/from-telnet))
             (.flush stdout)
             (recur))))
