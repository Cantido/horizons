(ns horizons.core
  "Provides access to NASA's HORIZONS solar system body database."
  (:require [horizons.parsing.parser :as parser]
            [horizons.telnet.client :as telnet]
            [clojure.tools.logging :as log]
            [clj-time.core :as t]
            [clj-time.format :as f]))

(defn ^:private end-datetime-parser [x]
  (cond
    (= x :plus2weeks) ""
    (satisfies? t/DateTimeProtocol x) (f/unparse (f/formatters :basic-date-time) x)))

(def ^:private ephemeris-options
  {:table-type {:vectors "v"}
   :coordinate-center {:earth ""}
   :reference-plane {:ecliptic "eclip"}
   :start-datetime {:now ""}
   :end-datetime end-datetime-parser
   :output-interval {:60m ""}
   :accept-default-output {true ""}})

(defn ^:private tokenreduce [m k v]
  (assoc m k (get-in ephemeris-options [k v])))

(defn ^:private tokens->options [tokens]
  (reduce-kv tokenreduce {} tokens))

(def supported-bodies
  #{199 299 399 499 599 699 799 899})

(defn supported?
  "Check if the given body ID is definitely supported by this system."
  [id]
  (->> id
    bigdec
    int
    supported-bodies))

(defn parsed-result
  [fn & more]
  (when-let [result (apply fn more)]
    (::S (parser/parse-horizons-response result))))

(defn get-planetary-body
  "Get geophysical data about a solar system body with the given ID."
  ([id]
   (log/info "Getting body" id)
   (parsed-result telnet/get-body id))
  ([id connection]
   (parsed-result telnet/get-body id connection)))

(defn get-ephemeris
  ([id]
   (log/spyf "Getting ephemeris for body" id)
   (parsed-result telnet/get-ephemeris-data id))
  ([id connection]
   (log/spyf "Getting ephemeris for body" id)
   (parsed-result telnet/get-ephemeris-data id connection)))

