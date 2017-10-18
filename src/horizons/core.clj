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
  [horizons-client-component telnet-fn conn & more]
  {:pre [(some? horizons-client-component)
         (some? (:parser horizons-client-component))
         (some? (:telnet-client horizons-client-component))]}
  (when-let [result (apply telnet-fn (:telnet-client horizons-client-component) conn more)]
    (log/debug "Parsing result...")
    (->>
      result
      (parser/parse-horizons-response (:parser horizons-client-component))
      (log/spyf "Done parsing, got result:%n----- BEGIN PARSE TREE -----%n%s%n------ END PARSE TREE  ------")
      (::S))))

(defn- with-new-connection
  [fn component & more]
  (let [conn (telnet/connect (:telnet-client component))
        result (apply fn component conn more)]
    (telnet/release (:telnet-client component) conn)
    result))

(defn geophysical
  "Get geophysical data about a solar system body with the given ID."
  ([component id]
   (with-new-connection geophysical component id))
  ([component connection id]
   {:pre  [(connect/valid-connection? (:connection-factory component) connection)]}
   (log/info "Getting body" id)
   (parsed-result component telnet/get-body connection id)))

(defn ephemeris
  ([component id] (ephemeris component id {}))
  ([component id opts] (with-new-connection ephemeris component id opts))
  ([component connection id opts]
   {:pre  [(some? component)
           (connect/valid-connection? (:connection-factory component) connection)]}
   (log/debug "Getting ephemeris for body" id "with options" opts)
   (parsed-result component telnet/get-ephemeris-data connection id opts)))
