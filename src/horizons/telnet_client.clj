(ns horizons.telnet-client
  (:require [clojure.core.async :refer [alts!! chan go timeout sliding-buffer <!! <! >!! >!]])
  (:import (org.apache.commons.net.telnet TelnetClient)
           (java.io BufferedReader BufferedWriter InputStreamReader OutputStreamWriter PrintStream)
           (java.nio.charset StandardCharsets Charset)))

(def from-telnet (chan (sliding-buffer 1000)))
(def to-telnet (chan))

(defn reader [^TelnetClient client]
  (new BufferedReader (new InputStreamReader (.getInputStream client) StandardCharsets/US_ASCII)))

(defn writer [^TelnetClient client]
  (new BufferedWriter (new OutputStreamWriter (.getOutputStream client) StandardCharsets/US_ASCII)))


(let [client (new TelnetClient)]
  (try
    (.connect client "ssd.jpl.nasa.gov" 6775)
    (let
      [ reader (reader client)
        writer (writer client)]

      (try
        (go
          (while true
            (>! from-telnet (.read reader))))
        (go
          (while true
            (.write writer ^int (<! to-telnet))
            (.flush writer)))))))

(defn pipe-stdin-to-telnet []
  (let
    [stdin (new BufferedReader (new InputStreamReader System/in))]
    (go
      (while true
        (let [got-from-stdin (.read stdin)]
          (println "Got this from STDIN. Char: " (char got-from-stdin) ", int: " (int got-from-stdin) ", type: " (type got-from-stdin))
          (>!! to-telnet got-from-stdin))))))

(defn show-telnet-out []
  (while true
    (.write System/out ^int (<!! from-telnet))
    (.flush System/out)))

(defn next-token
  ([] (next-token ""))
  ([word-so-far]
   (let [next-char (str (char (<!! from-telnet)))
          next-word (str word-so-far next-char)]
        (if (clojure.string/blank? next-char)
            next-word
            (recur next-word)))))

(defn wait-for-prompt []
  (let [token (next-token)]
       (if (clojure.string/starts-with? token "Horizons>")
           (do (print token) (flush))
           (recur))))

(wait-for-prompt)
(>!! to-telnet 63) ; question mark
(>!! to-telnet 10) ; line feed
(show-telnet-out)
