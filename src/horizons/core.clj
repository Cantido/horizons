(ns horizons.core
  "Provides access to NASA's HORIZONS solar system body database."
  (:require [horizons.parsing.parser :as parser]
            [horizons.telnet.client :as telnet]
            [clojure.tools.logging :as log]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [horizons.telnet.connect :as connect]
            [com.stuartsierra.component :as component]))

(defrecord HorizonsClient [bodies
                           parser
                           telnet-client
                           connection-factory])

(defn horizons-client [bodies]
  (component/using
    (map->HorizonsClient {:bodies bodies})
    [:telnet-client :connection-factory :parser]))

(defn supported?
  [component id]
  (parser/supported? (:parser component) id))

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

(defn bodies
  [component]
  (:bodies component))

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
