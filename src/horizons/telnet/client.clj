(ns horizons.telnet.client
  "Communicates to the HORIZONS Telnet server."
  (:require [clojure.core.async :as async]
            [clojure.core.async.impl.protocols :as pro]
            [clojure.string :as string]
            [horizons.telnet.pool :as pool]
            [clojure.tools.logging :as log]
            [horizons.telnet.connect :as connect]
            [com.stuartsierra.component :as component]))


(defrecord TelnetClient [connection-pool connection-factory])

(defn new-telnet-client []
  (component/using (map->TelnetClient {}) [:connection-pool :connection-factory]))


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

(defn next-token
  "Returns a channel that will provide the next
   whitespace-delimited word from the given channel.
   The channel will be closed once the next word is
   returned, or of the given channel is closed."
  [chan]
  {:pre  [(satisfies? pro/ReadPort chan)]
   :post [(satisfies? pro/ReadPort %)]}
  (async/go-loop [word-so-far nil]
    (when-let[next-char (async/<!! chan)]
      (let [next-word (str word-so-far next-char)]
        (if (string/blank? (str next-char))
          next-word
          (recur next-word))))))

(defn next-block
  "Returns a channel that will provide everything from
   the HORIZONS client until the next input prompt.
   The channel will be closed once the next block
   is returned, or if the given channel is closed."
  [chan]
  {:pre [(satisfies? pro/ReadPort chan)]
   :post [(satisfies? pro/ReadPort %)]}
  (async/go-loop [block-so-far nil]
    (when-let [next-word (async/<! (next-token chan))]
      (let [new-next-block (str block-so-far next-word)]
        (log/trace "Block so far:\n" new-next-block)
        (cond
         (some #(re-find % new-next-block) block-endings) new-next-block
         :else (recur new-next-block))))))

(defn wait-for-prompt
  "Returns a channel that will close once the next
   input prompt is received from HORIZONS, or if
   the given channel is closed."
  [chan]
  {:pre [(satisfies? pro/ReadPort chan)]
   :post [(satisfies? pro/ReadPort %)]}
  (async/go-loop []
    (when-let [token (async/<! (next-token chan))]
      (when-not (clojure.string/starts-with? token "Horizons>") (recur)))))

(defn reset-client
  "Brings the given client back to a command prompt"
  [chan]
  {:pre [(satisfies? pro/WritePort chan)]}
  (async/>!! chan (:clear body-prompt-commands)))

(def crlf-size 2)

(defn swallow-echo
  "Swallows the echo of arg s from channel chan.
  Returns true if it could swallow the entire length of s."
  [s chan]
  {:pre [(satisfies? pro/ReadPort chan)]
   :post [(or (true? %) (false? %))]}
  ; The echoed text is length (count s) and it is followed by a carriage return and a line feed.
  (some?
    (loop [n (+ crlf-size (count (str s)))]
      (or (>= 0 n) (when (async/<!! chan) (recur (dec n)))))))

(defn connect
  ([client]
   {:post [(connect/valid-connection? (:connection-factory client) %)]}
   (connect client (pool/connect (:connection-pool client))))
  ([client [in out]]
   {:pre [(connect/valid-connection? (:connection-factory client) [in out])]
    :post [(connect/valid-connection? (:connection-factory client) %)]}
   (async/<!! (wait-for-prompt out))
   [in out]))


(defn release [client [in out]]
  {:pre [(connect/valid-connection? (:connection-factory client) [in out])]
   :post [(connect/valid-connection? (:connection-factory client) [in out])]}
  (reset-client in)
  (pool/release (:connection-pool client) [in out]))

(defn transmit
  "Send a string to the given channels, and returns the next block."
  [client in out s]
  {:pre [(connect/valid-connection? (:connection-factory client) [in out])]
   :post [(connect/valid-connection? (:connection-factory client) [in out])]}
  (and
    (async/put! in s)
    (swallow-echo s out)
    (async/<!! (next-block out))))

(defn with-new-connection
  [fn client & more]
  (let [conn (connect client)
        result (apply fn (cons client (cons conn more)))]
    (release client conn)
    result))

(def default-opts {:table-type "v"
                   :coordinate-center ""
                   :reference-plane "eclip"
                   :start-datetime ""
                   :end-datetime ""
                   :output-interval ""
                   :accept-default-output ""})

(defn- merge-defaults [map]
  (merge default-opts map))


(defn get-body
  "Get a block of String data from the HORIZONS system about the given body-id"
  ([client body-id] (with-new-connection get-body {} body-id))
  ([client [in out] body-id]
   {:pre [(connect/valid-connection? (:connection-factory client) [in out])]
    :post [(connect/valid-connection? (:connection-factory client) [in out])]}
   (transmit client in out body-id)))

(defn get-ephemeris-data
  "Get a block of String data from the HORIZONS system
   with geophysical data about the given body-id"
  ([client body-id] (with-new-connection get-ephemeris-data {} body-id))
  ([client conn body-id] (get-ephemeris-data {} conn body-id default-opts))
  ([client [in out] body-id opts]
   {:pre [(connect/valid-connection? (:connection-factory client) [in out])]
    :post [(connect/valid-connection? (:connection-factory client) [in out])]}
   (let [tx (partial transmit client in out)
         opts (merge-defaults opts)]
     (penultimate
       (map tx
            [body-id
             (:ephemeris body-prompt-commands)
             (:table-type opts)
             (:coordinate-center opts)
             (:reference-plane opts)
             (:start-datetime opts)
             (:end-datetime opts)
             (:output-interval opts)
             (:accept-default-output opts)
             (:new-case ephemeris-prompt-commands)])))))

