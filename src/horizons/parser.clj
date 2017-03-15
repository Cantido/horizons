(ns horizons.parser
  "Parses and transforms output from the HORIZONS telnet client."
  (:require
    [clj-time.core :as t]
    [clj-time.format :as f]
    [instaparse.core :as core]
    [instaparse.transform :as transform]))

(def month-formatter (f/formatter "MMM"))

(def parse
  "Parse the a string into a parse tree."
  (core/parser (clojure.java.io/resource "horizons.bnf")))


(defn- month->int
  [s]
  (t/month (f/parse month-formatter s)))

(defn- string->int [s]
  (Integer/parseInt s))

(defn- datemap->date
  [m]
  (t/date-time (:year m) (:month m) (:day m)))

(defn date-and-time->datetime
  [date time]
  (cond
    (nil? (:hour-of-day time))  (t/date-time
                                  (t/year date)
                                  (t/month date)
                                  (t/day date))
    (nil? (:minute-of-hour time)) (t/date-time
                                    (t/year date)
                                    (t/month date)
                                    (t/day date)
                                    (:hour-of-day time))
    (nil? (:second-of-minute time)) (t/date-time
                                      (t/year date)
                                      (t/month date)
                                      (t/day date)
                                      (:hour-of-day time)
                                      (:minute-of-hour time))
    (nil? (:millisecond-of-second  time)) (t/date-time
                                            (t/year date)
                                            (t/month date)
                                            (t/day date)
                                            (:hour-of-day time)
                                            (:minute-of-hour time)
                                            (:second-of-minute time))
    :else (t/date-time
            (t/year date)
            (t/month date)
            (t/day date)
            (:hour-of-day time)
            (:minute-of-hour time)
            (:second-of-minute time)
            (:millisecond-of-second time))))

(defn timestamp-transformer
  ([date time] {:timestamp (date-and-time->datetime (last date) (:time time))})
  ([era date time time-zone] {:timestamp (date-and-time->datetime (last date) (:time time))}))

(def transform-rules
  {
   :date (fn [& more] [:date (datemap->date (into {} more))])
   :ephemeris (fn [& more] {:ephemeris (set more)})
   :ephemeris-line-item (fn [& more] (into {} more))
   :float bigdec
   :integer string->int
   :measurement-time (fn [& more] {:measurement-time (into {} more)})
   :month (fn [s] [:month (month->int s)])
   :time (fn [& more]  {:time (into {} more)})
   :timestamp timestamp-transformer})

;; If we could give insta/transform a default rule, it should
;; be (fn [& rest] {:label (into {} rest)}). But alas...

(defn transform
  [coll]
  (if (core/failure? coll)
    coll
    (transform/transform transform-rules coll)))

(defn- coll-of-colls?
  [coll]
  (and
    (vector? coll)
    (every? #(and (coll? %) (not (set? %))) (rest coll))))

(defn- into-map-or-nil
  "Applies `(into {} form)` if the argument is not nil. Otherwise returns nil."
  [coll]
  (when (not (empty? coll))
    (into {} coll)))

(defn tree->map
  "Transforms a parse tree into a nested map, where the first entry in a non-leaf node becomes the key,
  and the remaining children of the node are placed into a map. Leaf nodes are skipped, so leaf nodes
  become keys and values in their parents maps.

  Imagine that your tree is labeled, where the first member of every node vector is that node's name,
  and the remaining members are that node's children. This function will turn that tree into a
  map traversable by node names."
  [coll]
  (if (core/failure? coll)
    coll
    (clojure.walk/postwalk
      (fn [form]
        (cond
          (coll-of-colls? form) {(first form) (into-map-or-nil (rest form))}
          :else form))
      coll)))

(defn restructure
  "Applies tree transformations to a parse tree, then recursively converts all remaining key-value vectors into maps."
  [tree]
  (if (core/failure? tree)
    tree
    (->> tree
         transform
         tree->map)))
