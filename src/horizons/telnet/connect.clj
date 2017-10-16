(ns horizons.telnet.connect
  "Connects to the HORIZONS Telnet server."
  (:require [clojure.core.async :as async]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [clojure.core.async.impl.protocols :as protocols])
  (:import (org.apache.commons.net.telnet TelnetClient)
           (org.apache.commons.net SocketClient)
           (java.io Reader Writer Closeable InputStream IOException)
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

(defmethod close! nil               [x] nil)
(defmethod close! SocketClient      [x] (.disconnect x))
(defmethod close! protocols/Channel [x] (async/close! x))
(defmethod close! Closeable         [x] (.close x))
(defmethod close! Collection        [x] (map (partial close!) x))

(defn valid-connection?
  [connection-factory conn]
  {:post [(or (true? %) (false? %))]}
  (boolean
    (and
      (vector? conn)
      (= 2 (count conn))
      (satisfies? protocols/WritePort (first conn))
      (satisfies? protocols/ReadPort (second conn)))))

(defn- read!
  "Takes one int off of the given reader, swallowing any exceptions. Returns
  nil if there is an error or if the end of the stream is reached."
  [^Reader rdr]
  (try
    (let [result (io! (.read rdr))]
      (if-not (neg? result)
        result
        nil))
    (catch IOException e
      (log/error e "Reader closed unexpectedly.")
      nil)))

;; Consider using this like a reducing function. Use a writer as an init value.
(defn- write!
  "Writes s to writer, then flushes it. Returns the writer."
  [^Writer writer s]
  (io!
    (.write writer ^String (str s \newline))
    (.flush writer))
  writer)

(defn- reader-seq
  "Takes a reader and returns a lazy seq of all integers read from it. The
  seq is terminated when an error occurs or the end of the stream is reached."
  [^Reader rdr]
  (take-while some? (repeatedly #(read! rdr))))

(defn- next-char!
  "Gets the next character from the given reader"
  [^Reader rdr]
  (let [read-int (first (reader-seq rdr))
        read-char (when (some? read-int) (char read-int))
        read-str (when (some? read-char) (str read-char))]
    read-str))

(defn- char-seq!
  "Returns a lazy sequence of single-character strings as read from the given reader."
  [^Reader rdr]
  (take-while some? (repeatedly #(next-char! rdr))))

(defn telnet!
  "Returns an IOFactory attached to a telnet client at the given address."
  [^String host ^long port ^long timeout]
  (log/info "Initiating a Telnet connection to" host ":" port)
  (io!
    (doto (TelnetClient.)
      (.setConnectTimeout timeout)
      (.connect host port))))

(defn- reader-channel!
  "In another thread, constant reads x and puts the result onto chan. Closes
  both when either is closed. Opts are used when creating the reader from x.
  Returns chan."
  [x chan & opts]
  (async/thread
    (async/<!! (async/onto-chan chan (char-seq! (apply io/reader x opts))))
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
