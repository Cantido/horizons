(ns horizons.telnet.new-client
  "Communicates to the HORIZONS Telnet server better. Using transducers."
  (:require [clojure.core.async :as async]
    [clojure.core.async.impl.protocols :as pro]
    [clojure.string :as string]
    [horizons.telnet.pool :as pool]
    [horizons.telnet.client :as old]
    [clojure.tools.logging :as log]
    [horizons.telnet.connect :as connect]
    [com.stuartsierra.component :as component])
  (:import (java.io IOException)))

(def ^:private prompt-tokens
  #{"Horizons"
    "<cr>"
    "Select... [A]gain, [N]ew-case, [F]tp, [K]ermit, [M]ail, [R]edisplay, ? "
    "Observe, Elements, Vectors  [o,e,v,?] "
    "Coordinate center [ <id>,coord,geo  ] "
    "Use previous center  [ cr=(y), n, ? ] "
    "Reference plane [eclip, frame, body ] "
    "Starting TDB [>= 9999BC-Mar-20 00:00] "
    "Ending   TDB [<=   9999-Dec-31 12:00] "
    "Output interval [ex: 10m, 1h, 1d, ? ] "
    "Accept default output [ cr=(y), n, ?] "})

(def first-line-re #"^[:>][ \d\w]*\s*")

(def str-xform
  "A transducer that expects elements to be lists of chars or strings.
  Reduces those lists into strings."
  (map (partial apply str)))

(defn prompt-xform []
  "Transforms a stream of characters into blocks and prompts."
  (comp
    (partition-by #{\: \> \newline})
    str-xform
    (partition-by prompt-tokens)
    str-xform
    (map #(string/replace % first-line-re ""))
    (map string/trim)))

(defn next-block-channel
  "Creates a channel that will return the next block,
  then the next prompt, then the next block, etc, stripping echoes."
  [chan]
  (let [block-channel (async/chan 2048 (prompt-xform))]
    (async/pipe chan block-channel)
    block-channel))

(defn transmit
  [component to-telnet from-telnet s]
  (let [block-channel (next-block-channel from-telnet)
        accepted-transmit (async/>!! to-telnet s)
        return-block (async/<!! block-channel)
        discarded-prompt (async/<!! block-channel)]
    (and
      (log/spyf :trace "Channel accepted transmit: %s" accepted-transmit)
      (log/spyf :trace "Next block contents:%n----%n%s%n----" return-block))))

(defn get-body
  "Get a block of String data from the HORIZONS system about the given body-id"
  ([component body-id] (old/with-new-connection get-body component body-id))
  ([component [to-telnet from-telnet] body-id]
   {:pre [(connect/valid-connection? (:connection-factory component) [to-telnet from-telnet])]
    :post [(connect/valid-connection? (:connection-factory component) [to-telnet from-telnet])
           some?]}
   (log/debug "Transmitting request to Telnet...")
   (if-let [result (transmit component to-telnet from-telnet body-id)]
     (log/spyf old/response-format result)
     (throw (IOException. "Unable to get a result from Telnet.")))))
