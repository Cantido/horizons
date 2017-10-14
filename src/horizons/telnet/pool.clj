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

(defn ^:private ensure-available-pool [pool-component]
  (dosync
    (let [{:keys [available-connections connection-factory]} pool-component]
      (when (empty? @available-connections)
        (alter available-connections conj (connect/connect connection-factory))))))

(defn ^:private everybody-out-of-the-pool! [pool-component]
  (log/info "Getting everybody out of the pool (closing all connections and disposing of them)")
  (dosync
    (let [{:keys [available-connections connections-in-use]} pool-component]
      (when-not (empty? @available-connections)
        (log/info "Closing" (count @available-connections) "unused connections.")
        (map (partial connect/close! pool-component) @available-connections)
        (ref-set available-connections #{}))
      (when-not (empty? @connections-in-use)
        (log/warn "Closing" (count @connections-in-use) "connections that are currently in use!")
        (map (partial connect/close! pool-component) @connections-in-use)
        (ref-set connections-in-use #{})))))

(defn connect
  "Returns [to-telnet from-telnet] channels connected to a Telnet client."
  [pool-component]
  {:pre [(some? pool-component)]
   :post [(connect/valid-connection? (:connection-factory pool-component) %)]}
  (dosync
    (let [{:keys [available-connections connections-in-use]} pool-component]
      (assert (some? connections-in-use))
      (log/debug "About to fetch a connection from the pool. There are currently" (count @connections-in-use) "connections in use, and" (count @available-connections) "connections available.")
      (ensure-available-pool pool-component)
      (let [conn (first @available-connections)]
        (alter available-connections disj conn)
        (alter connections-in-use conj conn)
        (assert (contains? @connections-in-use conn))
        conn))))

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
      (alter available-connections conj conn)
      (assert (contains? @available-connections conn)))))


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
