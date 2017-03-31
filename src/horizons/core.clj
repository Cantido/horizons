(ns horizons.core
  "Provides access to NASA's HORIZONS solar system body database."
  (:require [horizons.parsing.parser :as parser]
            [horizons.telnet.client :as telnet]
            [clojure.tools.logging :as log]))

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

(defn get-ephemeris [id]
  (->> id
    (log/spyf "Getting ephemeris for %s")
    telnet/get-ephemeris-data
    (log/spyf "Got response from HORIZONS:\n%s")
    parser/horizons-response->data-structure
    ::S))
