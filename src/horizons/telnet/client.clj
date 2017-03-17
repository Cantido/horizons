(ns horizons.telnet.client
  "Communicates to the HORIZONS Telnet server."
  (:require [clojure.core.async :as async]
            [clojure.java.io :as io]
            [clojure.string :as string])
  (:import (org.apache.commons.net.telnet TelnetClient)
           (java.io BufferedReader BufferedWriter InputStreamReader OutputStreamWriter PrintStream)
           (java.nio.charset StandardCharsets Charset)))

(def ^:private from-telnet (async/chan (async/sliding-buffer 1000)))
(def ^:private to-telnet (async/chan))

(defn next-char
  "Gets the next character from the given reader"
  [rdr]
  (-> rdr .read char str))

(defn char-seq
  "Returns a lazy sequence of single-character strings as read from the given reader."
  [rdr]
  (repeatedly #(next-char rdr)))

(defn ^:private connect
  "Connects to the HORIZONS telnet service, attaching its input and output to channels."
  []
  (let [client (TelnetClient.)]
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
          (recur)))))))

(defn ^:private next-token
  "Get the next whitespace-delimited word from the Telnet connection"
  ([] (next-token ""))
  ([word-so-far]
   (let [next-char (async/<!! from-telnet)
         next-word (str word-so-far next-char)]
     (if (string/blank? next-char)
         next-word
         (recur next-word)))))

(defn ^:private next-block
  "Get everything from the HORIZONS client until the next input prompt"
  ([] (next-block ""))
  ([block-so-far]
   (let [next-word (next-token)]
     (cond
      (string/starts-with? next-word "Horizons>") block-so-far
      (string/starts-with? next-word "<cr>:") (str block-so-far next-word)
      :else (recur (str block-so-far next-word))))))

(defn ^:private wait-for-prompt
  "Blocks until the next input prompt is received from HORIZONS."
  []
  (let [token (next-token)]
    (when-not (clojure.string/starts-with? token "Horizons>") (recur))))

(defn get-body
  "Get a block of String data from the HORIZONS system about the given body-id"
  [body-id]
  (do
   (connect)
   (wait-for-prompt)
   (async/>!! to-telnet body-id)
   (next-block)))
