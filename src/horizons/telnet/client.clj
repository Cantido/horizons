(ns horizons.telnet.client
  (:require [clojure.core.async :as async]
            [clojure.java.io :as io])
  (:import (org.apache.commons.net.telnet TelnetClient)
           (java.io BufferedReader BufferedWriter InputStreamReader OutputStreamWriter PrintStream)
           (java.nio.charset StandardCharsets Charset)))

(def from-telnet (async/chan (async/sliding-buffer 1000)))
(def to-telnet (async/chan))

(defn connect
  "Connects to the HORIZONS telnet service, attaching its input and output to channels."
  []
  (let [client (new TelnetClient)]
    (try
      (.connect client "ssd.jpl.nasa.gov" 6775)
      (let
        [reader (io/reader (.getInputStream client) :encoding "US-ASCII")
         writer (io/writer (.getOutputStream client) :encoding "US-ASCII")]

        (try
          (async/go-loop []
              (async/>! from-telnet (str (char (.read reader))))
              (recur))
          (async/go-loop []
              (.write writer ^String (str (async/<! to-telnet) \newline))
              (.flush writer)
              (recur)))))))

(defn next-token
  ([] (next-token ""))
  ([word-so-far]
   (let [next-char (async/<!! from-telnet)
          next-word (str word-so-far next-char)]
        (if (clojure.string/blank? next-char)
            next-word
            (recur next-word)))))

(defn next-block
  "Get everything from the HORIZONS client until the next input prompt"
  ([] (next-block ""))
  ([block-so-far]
   (let [next-word (next-token)]
     (cond
       (clojure.string/starts-with? next-word "Horizons>") block-so-far
       (clojure.string/starts-with? next-word "<cr>:") (str block-so-far next-word)
       :else (recur (str block-so-far next-word))))))

(defn wait-for-prompt []
  (let [token (next-token)]
       (if (not (clojure.string/starts-with? token "Horizons>"))
           (recur))))

(defn get-body
  [body-id]
  (do
    (connect)
    (wait-for-prompt)
    (async/>!! to-telnet body-id)
    (next-block)))
