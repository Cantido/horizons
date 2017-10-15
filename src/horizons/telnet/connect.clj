(ns horizons.telnet.connect
  "Connects to the HORIZONS Telnet server."
  (:require [clojure.core.async :as async]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [clojure.core.async.impl.protocols :as protocols])
  (:import (org.apache.commons.net.telnet TelnetClient)
           (org.apache.commons.net SocketClient)
           (java.io Reader Writer Closeable InputStream)
           (java.util Collection)))

(defrecord ConnectionFactory [host port timeout])

(defn new-connection-factory
  "Creates a connection factory that will create Telnet connections to the
  given host & port."
  [host port timeout]
  (map->ConnectionFactory {:host host :port port :timeout timeout}))

(extend TelnetClient
  io/IOFactory
  (assoc io/default-streams-impl
    :make-input-stream (fn [^TelnetClient x opts] (io/make-input-stream (.getInputStream x) opts))
    :make-output-stream (fn [^TelnetClient x opts] (io/make-output-stream (.getOutputStream x) opts))))

(defmulti close!
          "Closes the given channel/connection/stream."
          class
          :default nil)

(defmethod close! nil               [component x] nil)
(defmethod close! SocketClient      [component x] (.disconnect x))
(defmethod close! protocols/Channel [component x] (async/close! x))
(defmethod close! Closeable         [component x] (.close x))
(defmethod close! Collection        [component x] (map (partial close! component) x))

(defn valid-connection?
  [connection-factory conn]
  {:post [(or (true? %) (false? %))]}
  (boolean
    (and
      (vector? conn)
      (= 2 (count conn))
      (satisfies? protocols/WritePort (first conn))
      (satisfies? protocols/ReadPort (second conn)))))

(defn- next-char!
  "Gets the next character from the given reader"
  [^Reader rdr]
  (let [read-int (io! (.read rdr))
        read-char (char read-int)
        read-str (str read-char)]
    (log/trace "Got integer" read-int "resulting in character" read-char "and string" read-str)
    read-str))

(defn- char-seq!
  "Returns a lazy sequence of single-character strings as read from the given reader."
  [^Reader rdr]
  (repeatedly #(next-char! rdr)))

(defn- write!
  [^Writer writer s]
  (io!
    (.write writer ^String (str s \newline))
    (.flush writer)))

(defn telnet!
  "Returns an IOFactory attached to a telnet client at the given address."
  [^String host ^long port ^long timeout]
  (log/info "Initiating a Telnet connection to" host ":" port)
  (doto (TelnetClient.)
    (.setConnectTimeout timeout)
    (.connect host port)))

(defn- reader-channel!
  "In another thread, constant reads x and puts the result onto chan. Closes
  both when either is closed. Opts are used when creating the reader from x.
  Returns chan."
  [x chan & opts]
  (async/thread
    (let [reader (apply io/reader x opts)]
      (async/<!! (async/onto-chan chan (char-seq! reader))))
    (map close! [x chan])
    (log/info "Channel connection from Telnet has been closed."))
  chan)

(defn- writer-channel!
  "In another thread, constantly reads from chan and puts the results onto x.
  Closes both when either is closed. Opts are used when creating the writer
  from x. Returns chan."
  [x chan & opts]
  (async/thread
    (let [writer (apply io/writer x opts)]
      (loop []
        (when-let [next-to-send (async/<!! chan)]
          (write! writer next-to-send)
          (recur))))
    (map close! [x chan])
    (log/info "Channel connection to Telnet has been closed."))
  chan)


(defn connect!
  "Connects to the HORIZONS telnet service, attaching its input and output to channels.
   Returns [to-telnet from-telnet] as a vector."
  [connection-factory]
  {:post [(valid-connection? connection-factory %)]}
  (let [{:keys [host port timeout]} connection-factory
        client (telnet! host port timeout)]
    (log/info "Connection to ssd.jpl.nasa.gov:6775 established.")
    [(writer-channel! client (async/chan) :encoding "US-ASCII")
     (reader-channel! client (async/chan) :encoding "US-ASCII")]))
