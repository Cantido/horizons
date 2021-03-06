(ns horizons.telnet.pool
  "Pools connections to the HORIZONS Telnet server."
  (:require [horizons.telnet.connect :as connect]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]))

(defn ^:private valid-pool?
  [pool-component pool]
  (and
    (set? pool)
    (every? (partial connect/valid-connection? (:connection-factory pool-component)) pool)))

(defn- grow-available-pool! [component conn]
  (let [{:keys [available-connections]} component]
    (dosync
      (alter available-connections conj conn))))

(defn ^:private ensure-available-pool! [pool-component]
    (let [{:keys [available-connections connection-factory]} pool-component]
      (when (empty? @available-connections)
        (alter available-connections conj (connect/connect! connection-factory)))))

(defn ^:private everybody-out-of-the-pool! [pool-component]
  (log/info "Getting everybody out of the pool (closing all connections and disposing of them)")
  (dosync
    (let [{:keys [available-connections connections-in-use]} pool-component]
      (when-not (empty? @available-connections)
        (log/info "Closing" (count @available-connections) "unused connections.")
        (map connect/close! @available-connections)
        (ref-set available-connections #{}))
      (when-not (empty? @connections-in-use)
        (log/warn "Closing" (count @connections-in-use) "connections that are currently in use!")
        (map connect/close! @connections-in-use)
        (ref-set connections-in-use #{})))))

(defn connection-available? [component]
  (pos? (count (deref (:available-connections component)))))

(defn- use-connection! [component conn]
  {:pre [(connect/valid-connection? (:connection-factory component) conn)]
   :post [(connect/valid-connection? (:connection-factory component) %)]}
  (let [{:keys [available-connections connections-in-use]} component]
    (log/debug "About to fetch a connection from the pool. There are currently" (count @connections-in-use) "connections in use, and" (count @available-connections) "connections available.")
    (dosync
      (assert (contains? @available-connections conn))
      (alter available-connections disj conn)
      (alter connections-in-use conj conn)
      conn)))

(defn connect
  "Returns [to-telnet from-telnet] channels connected to a Telnet client."
  [pool-component]
  {:pre [(some? pool-component)]
   :post [(connect/valid-connection? (:connection-factory pool-component) %)]}
  (let [{:keys [available-connections
                connections-in-use
                connection-factory]}
        pool-component]
    ;; We cannot create a new connection inside of a transaction, because creating
    ;; a new connection triggers IO. Thus the double-checked locking.
    (loop []
      (if (connection-available? pool-component)
        (dosync
          (if (connection-available? pool-component)
            (use-connection! pool-component (first @available-connections))
            (recur))) ; This exits the current transaction
        (do
          (grow-available-pool! pool-component (connect/connect! connection-factory))
          (recur))))))

(defn release
  "Puts an [to-telnet from-telnet] Telnet connection back in the pool."
  [pool-component conn]
  {:pre [(some? pool-component)
         (connect/valid-connection? (:connection-factory pool-component) conn)]}
  ;; We should do assertions inside the transaction,
  ;; otherwise we'd have a race condition.
  (dosync
    (let [{:keys [available-connections connections-in-use]} pool-component]
      (assert (contains? @connections-in-use conn))
      (alter connections-in-use disj conn)
      (alter available-connections conj conn))))

(defrecord ConnectionPool [connection-factory available-connections connections-in-use]
  component/Lifecycle

  (start [this]
    (set-validator! available-connections (partial valid-pool? this))
    (set-validator! connections-in-use (partial valid-pool? this))
    this)

  (stop [this]
    (everybody-out-of-the-pool! this)
    this))

(defn new-connection-pool
  ([]
   (component/using
     (map->ConnectionPool {:available-connections (ref #{})
                           :connections-in-use (ref #{})})
     [:connection-factory]))
  ([to-telnet from-telnet]
   {:pre [(some? to-telnet)
          (some? from-telnet)]}
   (component/using
     (map->ConnectionPool {:available-connections (ref #{[to-telnet from-telnet]})
                           :connections-in-use (ref #{})})
     [:connection-factory])))
