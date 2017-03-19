(ns horizons.telnet.client
  "Communicates to the HORIZONS Telnet server."
  (:require [clojure.core.async :as async]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [horizons.telnet.connect :as conn]))

(defn ^:private next-token
  "Returns a channel that will provide the next
   whitespace-delimited word from the given channel"
  [chan]
  (async/go-loop [word-so-far ""]
    (let [next-char (async/<!! chan)
          next-word (str word-so-far next-char)]
      (if (string/blank? next-char)
          next-word
          (recur next-word)))))

(defn ^:private next-block
  "Get everything from the HORIZONS client until the next input prompt"
  ([chan] (next-block chan ""))
  ([chan block-so-far]
   (let [next-word (async/<!! (next-token chan))]
     (cond
      (string/starts-with? next-word "Horizons>") block-so-far
      (string/starts-with? next-word "<cr>:") (str block-so-far next-word)
      :else (recur chan (str block-so-far next-word))))))

(defn ^:private wait-for-prompt
  "Blocks until the next input prompt is received from HORIZONS."
  [chan]
  (let [token (async/<!! (next-token chan))]
    (when-not (clojure.string/starts-with? token "Horizons>") (recur chan))))

(defn get-body
  "Get a block of String data from the HORIZONS system about the given body-id"
  [body-id]
  (do
   (let [[in out] (conn/connect)]
     (wait-for-prompt out)
     (async/put! in body-id)
     (next-block out))))
