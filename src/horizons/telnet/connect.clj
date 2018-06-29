(ns horizons.telnet.connect
  "Connects to the HORIZONS Telnet server."
  (:require [clojure.core.async :as async]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [clojure.core.async.impl.protocols :as protocols])
  (:import (org.apache.commons.net.telnet TelnetClient)
           (org.apache.commons.net SocketClient)
           (java.io Reader Writer Closeable IOException)
           (java.util Collection)))

(defrecord ConnectionFactory [host port timeout])

(defn new-connection-factory
  "Creates a connection factory that will create Telnet connections to the
  given host & port."
  [host port timeout]
  (map->ConnectionFactory {:host host :port port :timeout timeout}))

(defn telnet!
  "Returns an IOFactory attached to a telnet client at the given address."
  [^String host ^long port ^long timeout]
  (log/info "Initiating a Telnet connection to" host ":" port)
  (io!
    (doto (TelnetClient.)
      (.setConnectTimeout timeout)
      (.connect host port))))

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


(defn- swallow-ioe
  "Calls fn, logging & swallowing an IOException. Returns the result of fn, or nil."
  [fn]
  (try
    (fn)
    (catch IOException e
      (log/error e "Reader closed unexpectedly.")
      nil)))

(defn- read!
  "Takes one int off of the given reader. Returns nil if the end of the stream has been reached."
  [^Reader rdr]
  (let [result (io! (.read rdr))]
    (when-not (neg? result) result)))

(defn- safe-read!
  "Takes one int off of the given reader, swallowing any exceptions. Returns
   nil if there is an error or if the end of the stream is reached."
  [^Reader rdr]
  (swallow-ioe #(read! rdr)))

(defn- char-seq!
  "Returns a lazy sequence of single-character strings as read from the given reader."
  [^Reader rdr]
  (->> (repeatedly #(safe-read! rdr))
    (take-while some?)
    (map char)
    (map str)))

(defn- put-reduce!
  "Puts x onto chan, and returns chan. The 1-arity version returns its argument,
   which makes this fn suitable as a reduction function.

   This function is just a wrapper for async/>!!, since that function returns
   a boolean instead of the channel, which does not allow the function to be
   used for reduction."
  ([chan] chan)
  ([chan x]
   (async/>!! chan x)
   chan))

(defn- reader-channel!
  "In another thread, constant reads x and puts the result onto chan. Closes
   both when either is closed. Opts are used when creating the reader from x.
   Returns chan."
  [x chan & opts]
  (async/thread
    (reduce put-reduce! chan (char-seq! (apply io/reader x opts)))
    (map close! [x chan])
    (log/info "Channel connection from Telnet has been closed."))
  chan)


(defn- write-reduce!
  "Writes s to writer, then flushes it. Returns the writer. The 1-arity version
   just returns its argument, which makes this fn suitable as a reduction function."
  ([x] x)
  ([^Writer writer s]
   (io!
     (.write writer ^String (str s \newline))
     (.flush writer))
   writer))

(defn- from-chan
  "Creates a lazy sequence of elements pulled from chan. Realizing an element
   will block. The seq terminates when the channel closes."
  [chan]
  (take-while some? (repeatedly #(async/<!! chan))))

(defn- writer-channel!
  "In another thread, constantly reads from chan and puts the results onto x.
   Closes both when either is closed. Opts are used when creating the writer
   from x. Returns chan."
  [x chan & opts]
  (async/thread
    (reduce write-reduce! (apply io/writer x opts) (from-chan chan))
    (map close! [x chan])
    (log/info "Channel connection to Telnet has been closed."))
  chan)

(defn- kib
  "Given an n in KiB, returns the number of bytes."
  [n] (* n 1024))

; We only ever send a couple characters at a time,
; so this is more than enough.
(def writer-buffer-size 32)

; The default ephemeris is about 110 KiB.
; This lets us avoid ever blocking in the middle of a request.
(def reader-buffer-size (kib 128))

(defn connect!
  "Connects to the HORIZONS telnet service, attaching its input and output to channels.
   Returns [to-telnet from-telnet] as a vector."
  [connection-factory]
  {:post [(valid-connection? connection-factory %)]}
  (let [{:keys [host port timeout]} connection-factory
        client (telnet! host port timeout)]
    (log/info "Connection to ssd.jpl.nasa.gov:6775 established.")
    [(writer-channel! client (async/chan writer-buffer-size) :encoding "US-ASCII")
     (reader-channel! client (async/chan reader-buffer-size) :encoding "US-ASCII")]))
