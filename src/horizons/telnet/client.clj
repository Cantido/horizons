(ns horizons.telnet.client
  "Communicates to the HORIZONS Telnet server."
  (:require [clojure.core.async :as async]
            [clojure.string :as string]
            [horizons.telnet.pool :as pool]
            [clojure.tools.logging :as log]))


(def ^:private block-endings
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

(def ^:private penultimate
  (comp second reverse))

(def body-prompt-commands
  {:ephemeris "E"
   :clear ""})

(def ephemeris-prompt-commands
  {:new-case "N"})

(def ephemeris-options
  {:table-type {:vectors "v"}
   :coordinate-center {:earth ""}
   :reference-plane {:ecliptic "eclip"}
   :start-datetime {:now ""}
   :end-datetime {:plus2weeks ""}
   :output-interval {:60m ""}
   :accept-defaut-output {true ""}})

(def default-opts
  {:table-type :vectors
   :coordinate-center :earth
   :reference-plane :ecliptic
   :start-datetime :now
   :end-datetime :plus2weeks
   :output-interval :60m
   :accept-defaut-output true})

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
  (async/>!! conn (:clear body-prompt-commands)))

(defn ^:private swallow-echo
  "Swallows the echo of arg s from channel chan"
  [s chan]
  ; The echoed text is length (count s) and it is followed by a carriage return and a line feed.
  (dotimes [n (+ 2 (count (str s)))]
    (async/<!! chan)))

(defn ^:private connect []
  (let [[in out] (pool/connect)]
    (async/<!! (wait-for-prompt out))
    [in out]))

(defn ^:private release [[in out]]
  (reset-client in)
  (pool/release [in out]))

(defn ^:private transmit
  "Send a string to the given channels, and returns the next block."
  [in out s]
  (async/>!! in s)
  (swallow-echo s out)
  (async/<!! (next-block out)))

(defn options-map
  [opts]
  (zipmap
    (keys ephemeris-options)
    (map
      #(get-in ephemeris-options [% (% (merge default-opts opts))])
      (keys ephemeris-options))))

(defn get-ephemeris-data
  "Get a block of String data from the HORIZONS system
   with geophysical data about the given body-id"
  [body-id]
  (let [[in out] (connect)
        tx (partial transmit in out)
        opts (options-map {})
        result (penultimate
                (map tx
                     [body-id
                      (:ephemeris body-prompt-commands)
                      (:table-type opts)
                      (:coordinate-center opts)
                      (:reference-plane opts)
                      (:start-datetime opts)
                      (:end-datetime opts)
                      (:output-interval opts)
                      (:accept-defaut-output opts)
                      (:new-case ephemeris-prompt-commands)]))]
   (release [in out])
   (log/spy result)))

(defn get-body
  "Get a block of String data from the HORIZONS system about the given body-id"
  [body-id]
  (let [[in out] (connect)
        result (transmit in out body-id)]
    (release [in out])
    result))
