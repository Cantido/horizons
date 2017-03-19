(ns horizons.telnet.pool
  "Pools connections to the HORIZONS Telnet server."
  (:require [clojure.core.async :as async]
    [horizons.telnet.connect :as conn]))

(defn ^:private valid-connection?
  [conn]
  (and
    (vector? conn)
    (= 2 (count conn))))

(defn ^:private valid-pool?
  [pool]
  (and
    (set? pool)
    (every? valid-connection? pool)))


(def ^:private connection-pool
  (ref #{} :validator valid-pool?))

(def ^:private connections-in-use
  (ref #{} :validator valid-pool?))


(defn ^:private ensure-available-pool []
  (dosync
    (when (empty? @connection-pool)
      (alter connection-pool conj (conn/connect)))))

(defn connect
  "Returns [in out] channels connected to a Telnet client."
  []
  {:post [(partial contains? connections-in-use)]}
  (dosync
    (ensure-available-pool)
    (let [connection (first @connection-pool)]
      (alter connection-pool disj connection)
      (alter connections-in-use conj connection)
      connection)))

(defn release
  "Put an [in out] Telnet connection back in the pool."
  [conn]
  {:pre [(partial contains? connections-in-use)]}
  (dosync
    (alter connections-in-use disj conn)
    (alter connection-pool conj conn)))
