(ns horizons.telnet.pool
  "Pools connections to the HORIZONS Telnet server."
  (:require [clojure.core.async :as async]
    [horizons.telnet.connect :as conn]))

(def ^:private connection-pool
  (ref #{}))

(def ^:private connections-in-use
  (ref #{}))

(defn ^:private ensure-available-pool []
  (dosync
    (when (empty? @connection-pool)
      (alter connection-pool conj (conn/connect)))))

(defn connect
  "Returns [in out] channels connected to a Telnet client."
  []
  (dosync
    (ensure-available-pool)
    (let [connection (first @connection-pool)]
      (alter connection-pool disj connection)
      (alter connections-in-use conj connection)
      connection)))

(defn release
  "Put an [in out] Telnet connection back in the pool."
  [conn]
  (dosync
    (alter connections-in-use disj conn)
    (alter connection-pool conj conn)))
