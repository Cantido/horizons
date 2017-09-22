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

(defn close! [[in out]]
  (async/close! in)
  (async/close! out))

(defn ^:private everybody-out-of-the-pool! []
  (log/info "Getting everybody out of the pool (closing all connections and disposing of them)")
  (dosync
    (log/info "Closing" (count @connection-pool) "unused connections.")
    (map close! @connection-pool)
    (ref-set connection-pool #{})
    (log/warn "Closing" (count @connections-in-use) "connections that are currently in use!")
    (map close! @connections-in-use)
    (ref-set connections-in-use #{})))

(defn connect
  "Returns [in out] channels connected to a Telnet client."
  []
  {:post [(conn/valid-connection? %)]}
  (log/debug "About to fetch a connection from the pool. There are currently" (count @connections-in-use) "connections in use, and" (count @connection-pool) "connections available.")
  (dosync
    (ensure-available-pool)
    (let [conn (first @connection-pool)]
      (alter connection-pool disj conn)
      (alter connections-in-use conj conn)
      (assert (contains? @connections-in-use conn))
      conn)))

(defn release
  "Put an [in out] Telnet connection back in the pool."
  [conn]
  {:pre [(conn/valid-connection? conn)]}
  ;; We should do assertions inside the transaction,
  ;; otherwise we'd have a race condition.
  (dosync
    (assert (contains? @connections-in-use conn))
    (alter connections-in-use disj conn)
    (alter connection-pool conj conn)
    (assert (contains? @connection-pool conn))))
