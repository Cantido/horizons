(ns horizons.core
  "Provides access to NASA's HORIZONS solar system body database."
  (:require [horizons.parsing.parser :refer :all]
            [horizons.telnet.client :refer :all]))

(def supported-bodies
  #{199 299 399 499})

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
  (->>
    id
    get-body
    horizons-response->data-structure
    ::S))
