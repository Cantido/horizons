(ns horizons.async-utils
  (:require [clojure.test :refer :all]
            [clojure.core.async :as async]))

(def closed-chan
  (let [chan (async/chan)]
    (async/close! chan)
    chan))

(defn closed? [chan]
  (nil? (async/<!! chan)))
