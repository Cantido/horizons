(ns horizons.core
  "Provides access to NASA's HORIZONS solar system body database."
  (:require [horizons.parsing.parser :as parser]
            [horizons.telnet.client :as telnet]
            [clojure.tools.logging :as log]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [horizons.telnet.connect :as connect]
            [com.stuartsierra.component :as component]))

(defrecord HorizonsClient [supported-bodies
                           parser
                           telnet-client
                           connection-factory])

(defn horizons-client []
  (component/using
    (map->HorizonsClient
      {:supported-bodies #{199 299 399 499 599 699 799 899}})
    [:telnet-client :connection-factory :parser]))

(defn supported?
  "Check if the given body ID is definitely supported by this system."
  [client id]
  (->> id
    bigdec
    int
    (:supported-bodies client)))

(defn- parsed-result
  [horizons-client-component telnet-fn & more]
  (when-let [result (apply telnet-fn (:telnet-client horizons-client-component) more)]
    (::S (parser/parse-horizons-response (:parser horizons-client-component) result))))

(defn- with-new-connection
  [fn client & more]
  (let [conn (telnet/connect (:telnet-client client))
        result (apply fn (cons client (cons conn more)))]
    (telnet/release (:telnet-client client) conn)
    result))

(defn get-planetary-body
  "Get geophysical data about a solar system body with the given ID."
  ([client id] (with-new-connection get-planetary-body client id))
  ([client connection id]
   {:pre  [(connect/valid-connection? (:connection-factory client) connection)]}
   (log/info "Getting body" id)
   (parsed-result client telnet/get-body connection id)))

(defn get-ephemeris
  ([client id] (with-new-connection get-ephemeris client id))
  ([client connection id] (get-ephemeris client connection id {}))
  ([client connection id opts]
   {:pre  [(some? client)
           (connect/valid-connection? (:connection-factory client) connection)]}
   (log/debug "Getting ephemeris for body" id "with options" opts)
   (parsed-result client telnet/get-ephemeris-data connection id)))
