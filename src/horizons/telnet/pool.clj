(ns horizons.telnet.pool
  "Pools connections to the HORIZONS Telnet server."
  (:require [clojure.core.async :as async]
            [clojure.core.async.impl.protocols :as pro]
            [horizons.telnet.connect :as conn]
            [clojure.tools.logging :as log]))

(defn valid-connection?
  [conn]
  {:post [(or (true? %) (false? %))]}
  (boolean
    (and
      (vector? conn)
      (= 2 (count conn))
      (satisfies? pro/WritePort (first conn))
      (satisfies? pro/ReadPort (second conn)))))

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
  {:post [(valid-connection? %)]}
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
  {:pre [(valid-connection? conn)]}
  (dosync
    ((juxt
      (partial alter connections-in-use disj)
      (partial alter connection-pool conj))
     conn)))
