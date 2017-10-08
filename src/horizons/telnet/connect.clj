(ns horizons.telnet.connect
  "Connects to the HORIZONS Telnet server."
  (:require [clojure.core.async :as async]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [clojure.core.async.impl.protocols :as protocols])
  (:import (org.apache.commons.net.telnet TelnetClient)
           (java.io Reader Writer)))

(defrecord ConnectionFactory [host port])

(defn new-connection-factory [host port]
  (map->ConnectionFactory {:host host :port port}))

(defn ^:private next-char
  "Gets the next character from the given reader"
  [^Reader rdr]
  (let [read-int (io! (.read rdr))
        read-char (char read-int)
        read-str (str read-char)]
    (log/trace "Got integer" read-int "resulting in character" read-char "and string" read-str)
    read-str))

(defn ^:private char-seq
  "Returns a lazy sequence of single-character strings as read from the given reader."
  [^Reader rdr]
  (repeatedly #(next-char rdr)))

(defn ^:private write
  [^Writer writer s]
  (io!
    (.write writer ^String (str s \newline))
    (.flush writer)))

(defn valid-connection?
  [connection-factory conn]
  {:post [(or (true? %) (false? %))]}
  (boolean
    (and
      (vector? conn)
      (= 2 (count conn))
      (satisfies? protocols/WritePort (first conn))
      (satisfies? protocols/ReadPort (second conn)))))

(defn- close-connection! [[x y]]
  (async/close! x)
  (async/close! y)
  (log/info "Channel connection to Telnet has been closed."))

(defn connect
  "Connects to the HORIZONS telnet service, attaching its input and output to channels.
   Returns [to-telnet from-telnet] as a vector."
  [connection-factory]
  {:post [(valid-connection? connection-factory %)]}
  (log/info "Initiating a Telnet connection to ssd.jpl.nasa.gov:6775.")
  (let [^TelnetClient client (TelnetClient.)
        to-telnet (async/chan)
        from-telnet (async/chan)]
    (.setConnectTimeout client 5000)
    (.connect client ^String (:host connection-factory) ^int (:port connection-factory))
    (let [writer (-> client .getOutputStream (io/writer :encoding "US-ASCII"))
          reader (-> client .getInputStream (io/reader :encoding "US-ASCII"))]
      (async/thread
        (try
          (async/<!! (async/onto-chan from-telnet (char-seq reader)))
          (finally
            (close-connection! [to-telnet from-telnet]))))
      (async/thread
        (try
          (loop []
            (when-let [next-to-send (async/<!! to-telnet)]
              (write writer next-to-send)
              (recur)))
          (finally
            (close-connection! [to-telnet from-telnet])))))
    (log/info "Connection to ssd.jpl.nasa.gov:6775 established.")
    [to-telnet from-telnet]))
