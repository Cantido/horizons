(ns horizons.core
  "Provides access to NASA's HORIZONS solar system body database."
  (:require [horizons.parsing.parser :as parser]
            [horizons.telnet.client :as telnet]
            [clojure.tools.logging :as log]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [horizons.telnet.connect :as connect]
            [com.stuartsierra.component :as component]))

(defrecord HorizonsClient [supported-bodies telnet-client])

(defn horizons-client []
  (component/using
    (map->HorizonsClient
      {:supported-bodies #{199 299 399 499 599 699 799 899}})
    [:telnet-client]))

(defn supported?
  "Check if the given body ID is definitely supported by this system."
  [client id]
  (->> id
    bigdec
    int
    (:supported-bodies client)))

(defn- parsed-result
  [fn & more]
  (when-let [result (apply fn more)]
    (::S (parser/parse-horizons-response result))))

(defn- with-new-connection
  [fn client & more]
  (let [conn (telnet/connect)
        result (apply fn (cons client (cons conn more)))]
    (telnet/release conn)
    result))

(defn get-planetary-body
  "Get geophysical data about a solar system body with the given ID."
  ([client id] (with-new-connection get-planetary-body client id))
  ([client connection id]
   {:pre  [(connect/valid-connection? connection)]}
   (log/info "Getting body" id)
   (parsed-result telnet/get-body connection id)))

(defn get-ephemeris
  ([client id] (with-new-connection get-ephemeris client id))
  ([client connection id] (get-ephemeris client connection id {}))
  ([client connection id opts]
   {:pre  [(connect/valid-connection? connection)]}
   (log/debug "Getting ephemeris for body" id "with options" opts)
   (parsed-result telnet/get-ephemeris-data connection id)))

