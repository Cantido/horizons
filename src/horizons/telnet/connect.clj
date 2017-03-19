(ns horizons.telnet.connect
  "Connects to the HORIZONS Telnet server."
  (:require [clojure.core.async :as async]
            [clojure.java.io :as io]
            [clojure.string :as string])
  (:import (org.apache.commons.net.telnet TelnetClient)
           (java.io BufferedReader BufferedWriter InputStreamReader OutputStreamWriter PrintStream)
           (java.nio.charset StandardCharsets Charset)))

(defn ^:private next-char
  "Gets the next character from the given reader"
  [rdr]
  (-> rdr .read char str))

(defn ^:private char-seq
  "Returns a lazy sequence of single-character strings as read from the given reader."
  [rdr]
  (repeatedly #(next-char rdr)))

(defn connect
  "Connects to the HORIZONS telnet service, attaching its input and output to channels."
  []
  (let [client (TelnetClient.)
        to-telnet (async/chan)
        from-telnet (async/chan (async/sliding-buffer 1000))]
    (try
      (.connect client "ssd.jpl.nasa.gov" 6775)
      (let [writer (-> client .getOutputStream (io/writer :encoding "US-ASCII"))
            reader-seq (-> client .getInputStream (io/reader :encoding "US-ASCII") char-seq)]
        (try
          (async/go-loop [remaining-reader-seq reader-seq]
            (async/>! from-telnet (first remaining-reader-seq))
            (recur (rest remaining-reader-seq)))
          (async/go-loop []
            (.write writer ^String (str (async/<! to-telnet) \newline))
            (.flush writer)
            (recur)))))
    [to-telnet from-telnet]))