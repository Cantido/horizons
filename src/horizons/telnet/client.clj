(ns horizons.telnet.client
  "Communicates to the HORIZONS Telnet server."
  (:require [clojure.core.async :as async]
            [clojure.string :as string]
            [horizons.telnet.pool :as pool]
            [clojure.tools.logging :as log]))

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

(def block-endings
  #{#"Horizons> "
    #"<cr>: "
    #"Select\.\.\. \[A\]gain, \[N\]ew-case, \[F\]tp, \[K\]ermit, \[M\]ail, \[R\]edisplay, \? : "
    #"Observe, Elements, Vectors  \[o,e,v,\?\] : "
    #"Coordinate center \[ <id>,coord,geo  \] : "
    #"Use previous center  \[ cr=\(y\), n, \? \] : "
    #"Reference plane \[eclip, frame, body \] : "
    #"Starting TDB \[>=\s+\d+(BC)?-[a-zA-Z]+-\d+ \d\d:\d\d\] : "
    #"Ending   TDB \[<=\s+\d+(BC)?-[a-zA-Z]+-\d+ \d\d:\d\d\] : "
    #"Output interval \[ex: 10m, 1h, 1d, \? \] : "
    #"Accept default output \[ cr=\(y\), n, \?\] : "})

(defn ^:private next-block
  "Returns a channel that will provide everything from
   the HORIZONS client until the next input prompt"
  [chan]
  (async/go-loop [block-so-far ""]
    (let [next-word (async/<! (next-token chan))
          new-next-block (str block-so-far next-word)]
      (log/trace "Block so far:\n" new-next-block)
      (cond
       (some #(re-find % new-next-block) block-endings) new-next-block
       :else (recur new-next-block)))))

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

(defn swallow-next-block [chan]
  (let [block (async/<!! (next-block chan))]
    (log/debug block)))

(defn get-ephemeris-data
  "Get a block of String data from the HORIZONS system
   with geophysical data about the given body-id"
  [body-id]
  (let [[in out] (pool/connect)]
    (async/<!! (wait-for-prompt out))
    (async/>!! in body-id)
    (log/debug "Sending E")
    (async/>!! in "E")
    (swallow-next-block out)
    (async/>!! in "v")
    (swallow-next-block out)
    (async/>!! in "")
    (swallow-next-block out)
    (async/>!! in "eclip")
    (swallow-next-block out)
    (async/>!! in "")
    (swallow-next-block out)
    (async/>!! in "")
    (swallow-next-block out)
    (async/>!! in "")
    (swallow-next-block out)
    (async/>!! in "")
    (let [result (async/<!! (next-block out))]
      (log/debug result)
      (async/>!! in "N")
      (swallow-next-block out)
      (pool/release [in out])
      result)))

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
