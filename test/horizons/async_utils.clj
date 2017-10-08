(ns horizons.async-utils
  (:require [clojure.test :refer :all]
            [clojure.core.async :as async]))

(def closed-chan
  (let [chan (async/chan)]
    (async/close! chan)
    chan))

(defn closed? [chan]
  (nil? (async/<!! chan)))

(defn dropping-channel
  "Returns a channel that never blocks, drops everything it is given,
   and always returns true. It doesn't store anything and will block
   when you try to read from it.

   This is probably your fastest option, if you need a channel
   but don't care about getting anything back out."
  [] (async/chan (async/dropping-buffer 0)))
