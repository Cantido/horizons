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

(let
  [stdin (new BufferedReader (new InputStreamReader System/in))]
  (go
    (while true
      (>!! to-telnet (.read stdin)))))

(while true
  (.write System/out ^int (<!! from-telnet))
  (.flush System/out))
