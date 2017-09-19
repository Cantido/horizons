(ns horizons.telnet.connect
  "Connects to the HORIZONS Telnet server."
  (:require [clojure.core.async :as async]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.tools.logging :as log])
  (:import (org.apache.commons.net.telnet TelnetClient)
           (java.io BufferedReader BufferedWriter InputStreamReader OutputStreamWriter PrintStream)
           (java.nio.charset StandardCharsets Charset)))

(defn ^:private next-char
  "Gets the next character from the given reader"
  [rdr]
  (let [read-int (io! (.read rdr))
        read-char (char read-int)
        read-str (str read-char)]
    (log/trace "Got integer" read-int "resulting in character" read-char "and string" read-str)
    read-str))

(defn ^:private char-seq
  "Returns a lazy sequence of single-character strings as read from the given reader."
  [rdr]
  (repeatedly #(next-char rdr)))

(defn ^:private write
  [writer s]
  (io!
    (.write writer ^String (str s \newline))
    (.flush writer)))

(defn connect
  "Connects to the HORIZONS telnet service, attaching its input and output to channels."
  []
  (log/info "Initiating a Telnet connection to ssd.jpl.nasa.gov:6775.")
  (let [client (TelnetClient.)
        to-telnet (async/chan)
        from-telnet (async/chan)]
    (.connect client "ssd.jpl.nasa.gov" 6775)
    (let [writer (-> client .getOutputStream (io/writer :encoding "US-ASCII"))
          reader (-> client .getInputStream (io/reader :encoding "US-ASCII"))]
      (async/thread
        (async/<!! (async/onto-chan from-telnet (char-seq reader)))
        (log/info "Channel from telnet has been closed."))
      (async/thread
        (loop []
          (when-let [next-to-send (async/<!! to-telnet)]
            (write writer next-to-send)
            (recur)))
        (log/info "Channel to telnet has been closed.")))
    (log/info "Connection to ssd.jpl.nasa.gov:6775 established.")
    [to-telnet from-telnet]))
