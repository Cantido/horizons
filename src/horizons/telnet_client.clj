(ns horizons.telnet-client
  (:import (org.apache.commons.net.telnet TelnetClient)
           (java.io BufferedReader BufferedWriter InputStreamReader OutputStreamWriter PrintStream)
           (java.nio.charset StandardCharsets Charset)))

(defn reader [^TelnetClient client]
  (new BufferedReader (new InputStreamReader (.getInputStream client) StandardCharsets/US_ASCII)))

(defn writer [^TelnetClient client]
  (new BufferedWriter (new OutputStreamWriter (.getOutputStream client) StandardCharsets/US_ASCII)))


(let [client (new TelnetClient)]
  (try
    (.connect client "ssd.jpl.nasa.gov" 6775)
    (let
      [ reader (reader client)
        writer (writer client)
        stdin (new BufferedReader (new InputStreamReader System/in))]
      (try

        (while [(.isAvailable client)
                (while (.ready reader) (.write System/out ^int (.read reader)))
                (while (.ready stdin) (.write writer ^int (.read stdin)))
                (.flush writer)
                (.flush System/out)])
        (finally
          (.disconnect client))))))

