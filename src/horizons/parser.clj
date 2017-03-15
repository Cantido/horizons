(ns horizons.parser
  (:require
    [clj-time.core :as t]
    [clj-time.format :as f]
    [instaparse.core :as core]
    [instaparse.transform :as transform]))

(def parse
  (core/parser (clojure.java.io/resource "horizons.bnf")))

(defn string->int
  [s]
  (Integer/parseInt s))

(def month-formatter (f/formatter "MMM"))

(defn month->int
  [month]
  (t/month (f/parse month-formatter month)))

(defn datemap->date
  [datemap]
  (t/date-time (:year datemap) (:month datemap) (:day datemap)))

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
   :date (fn [& args] [:date (datemap->date (into {} args))])
   :ephemeris (fn [& args] {:ephemeris (set args)})
   :ephemeris-line-item (fn [& args] (into {} args))
   :integer string->int
   :measurement-time (fn [& args] {:measurement-time (into {} args)})
   :month (fn [mo] [:month (month->int mo)])
   :time (fn [& args]  {:time (into {} args)})
   :timestamp timestamp-transformer})

;; If we could give insta/transform a default rule, it should
;; be (fn [& rest] {:label (into {} rest)}). But alas...

(defn transform
  [tree]
  (if (core/failure? tree)
    tree
    (transform/transform transform-rules tree)))

(defn coll-of-colls?
  [form]
  (and
    (vector? form)
    (every? #(and (coll? %) (not (set? %))) (rest form))))

(defn into-map-or-nil
  [form]
  (if (empty? form)
    nil
    (into {} form)))

(defn tree->map
  "Transforms a parse tree into a nested map, where the first entry in a non-leaf node becomes the key,
  and the remaining children of the node are placed into a map. Leaf nodes are skipped, so leaf nodes
  become keys and values in their parents maps.

  Imagine that your tree is labeled, where the first member of every node vector is that node's name,
  and the remaining members are that node's children. This function will turn that tree into a
  map traversable by node names."
  [tree]
  (if (core/failure? tree)
    tree
    (clojure.walk/postwalk
      (fn [form]
        (cond
          (coll-of-colls? form) {(first form) (into-map-or-nil (rest form))}
          :else form))
     tree)))

(defn check-for-nil
  [msg x]
  (if (nil? x)
      (throw (NullPointerException. msg))
      x))


(defn restructure
  [tree]
  (if (core/failure? tree)
    tree
    (->> tree
         (check-for-nil "tree was null")
         transform
         (check-for-nil "Result of transform was null")
         tree->map
         (check-for-nil "Result of tree->map was null"))))
