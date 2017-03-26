(ns horizons.core
  "Provides access to NASA's HORIZONS solar system body database."
  (:require [horizons.parsing.parser :refer :all]
            [horizons.telnet.client :refer :all]
            [clojure.tools.logging :as log]))

(def supported-bodies
  #{199 299 399 499 599 699})

(defn supported?
  "Check if the given body ID is definitely supported by this system."
  [id]
  (->> id
       bigdec
       int
       supported-bodies))

(defn log-response [s]
  (log/debug "Got response from HORIZONS:\n" s)
  s)

(defn get-planetary-body
  "Get geophysical data about a solar system body with the given ID."
  [id]
  (log/debug "Getting body" id)
  (->>
    id
    get-body
    log-response
    horizons-response->data-structure
    ::S))
