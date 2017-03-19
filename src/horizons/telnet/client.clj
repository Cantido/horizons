(ns horizons.telnet.client
  "Communicates to the HORIZONS Telnet server."
  (:require [clojure.core.async :as async]
            [clojure.java.io :as io]
            [clojure.string :as string])
  (:import (org.apache.commons.net.telnet TelnetClient)
           (java.io BufferedReader BufferedWriter InputStreamReader OutputStreamWriter PrintStream)
           (java.nio.charset StandardCharsets Charset)))

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

(defn ^:private next-token
  "Get the next whitespace-delimited word from the Telnet connection"
  ([chan] (next-token chan ""))
  ([chan word-so-far]
   (let [next-char (async/<!! chan)
         next-word (str word-so-far next-char)]
     (if (string/blank? next-char)
         next-word
         (recur chan next-word)))))

(defn ^:private next-block
  "Get everything from the HORIZONS client until the next input prompt"
  ([chan] (next-block chan ""))
  ([chan block-so-far]
   (let [next-word (next-token chan)]
     (cond
      (string/starts-with? next-word "Horizons>") block-so-far
      (string/starts-with? next-word "<cr>:") (str block-so-far next-word)
      :else (recur chan (str block-so-far next-word))))))

(defn ^:private wait-for-prompt
  "Blocks until the next input prompt is received from HORIZONS."
  [chan]
  (let [token (next-token chan)]
    (when-not (clojure.string/starts-with? token "Horizons>") (recur chan))))

(defn get-body
  "Get a block of String data from the HORIZONS system about the given body-id"
  [body-id]
  (do
   (let [[in out] (connect)]
     (wait-for-prompt out)
     (async/put! in body-id)
     (next-block out))))
