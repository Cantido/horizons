(ns horizons.core
  "Provides access to NASA's HORIZONS solar system body database."
  (:require [horizons.parsing.parser :as parser]
            [horizons.telnet.client :as telnet]
            [clojure.tools.logging :as log]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [horizons.telnet.connect :as connect]
            [com.stuartsierra.component :as component]))

(defrecord HorizonsClient [telnet-client])

(defn horizons-client []
  (component/using (map->HorizonsClient {}) [:telnet-client]))

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
  ([id] (telnet/with-new-connection get-planetary-body id))
  ([connection id]
   {:pre  [(connect/valid-connection? connection)]}
   (log/info "Getting body" id)
   (parsed-result telnet/get-body connection id)))

(defn get-ephemeris
  ([id] (telnet/with-new-connection get-ephemeris id))
  ([connection id] (get-ephemeris connection id {}))
  ([connection id opts]
   {:pre  [(connect/valid-connection? connection)]}
   (log/debug "Getting ephemeris for body" id "with options" opts)
   (parsed-result telnet/get-ephemeris-data connection id)))

