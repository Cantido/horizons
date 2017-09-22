(ns horizons.telnet.pool
  "Pools connections to the HORIZONS Telnet server."
  (:require [clojure.core.async :as async]
            [clojure.core.async.impl.protocols :as pro]
            [horizons.telnet.connect :as conn]
            [clojure.tools.logging :as log]))

(defn ^:private valid-pool?
  [pool]
  (and
    (set? pool)
    (every? conn/valid-connection? pool)))


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
  {:post [(conn/valid-connection? %)]}
  (log/debug "About to fetch a connection from the pool. There are currently" (count @connections-in-use) "connections in use, and" (count @connection-pool) "connections available.")
  (dosync
    (ensure-available-pool)
    (last
      ((juxt
         (partial alter connection-pool disj)
         (partial alter connections-in-use conj)
         identity)
       (first @connection-pool)))))

(defn release
  "Put an [in out] Telnet connection back in the pool."
  [conn]
  {:pre [(conn/valid-connection? conn)]}
  (dosync
    ((juxt
      (partial alter connections-in-use disj)
      (partial alter connection-pool conj))
     conn)))
