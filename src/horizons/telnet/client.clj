(ns horizons.telnet.client
  "Communicates to the HORIZONS Telnet server."
  (:require [clojure.core.async :as async]
            [clojure.string :as string]
            [horizons.telnet.pool :as pool]))

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
  "Returns a channel that will provide everything from
   the HORIZONS client until the next input prompt"
  [chan]
  (async/go-loop [block-so-far ""]
    (let [next-word (async/<! (next-token chan))]
      (cond
       (string/starts-with? next-word "Horizons>") block-so-far
       (string/starts-with? next-word "<cr>:") (str block-so-far next-word)
       :else (recur (str block-so-far next-word))))))

(defn ^:private wait-for-prompt
  "Returns a channel that will close once the next
   input prompt is received from HORIZONS."
  [chan]
  (async/go-loop []
    (let [token (async/<! (next-token chan))]
      (when-not (clojure.string/starts-with? token "Horizons>") (recur)))))

(defn ^:private reset-client
  "Brings the given client back to a command prompt"
  [conn]
  (async/>!! conn ""))

(defn get-body
  "Get a block of String data from the HORIZONS system about the given body-id"
  [body-id]
  (let [[in out] (pool/connect)]
    (async/<!! (wait-for-prompt out))
    (async/>!! in body-id)
    (let [result (async/<!! (next-block out))]
      (reset-client in)
      (pool/release [in out])
      result)))
