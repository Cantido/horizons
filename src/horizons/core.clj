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

(defn get-planetary-body
  "Get geophysical data about a solar system body with the given ID."
  [id]
  (->> id
    (log/spyf "Getting body %s")
    telnet/get-body
    (log/spyf "Got response from HORIZONS:\n%s")
    parser/horizons-response->data-structure
    ::S))

(defn get-ephemeris [id & {:keys [table-type
                                  coordinate-center
                                  reference-plane
                                  start-datetime
                                  end-datetime
                                  output-interval
                                  accept-default-output]
                           :or {table-type :vectors
                                coordinate-center :earth
                                reference-plane :ecliptic
                                start-datetime :now
                                end-datetime :plus2weeks
                                output-interval :60m
                                accept-default-output true}
                           :as opts}]
  (as-> [id] e
    (log/spyf "Getting ephemeris for %s" e)
    (apply (partial telnet/get-ephemeris-data e) (tokens->options opts))
    (log/spyf "Got response from HORIZONS:\n%s" e)
    (parser/horizons-response->data-structure e)
    (::S e)))
