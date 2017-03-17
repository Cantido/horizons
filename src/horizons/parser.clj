(ns horizons.parser
  "Parses and transforms output from the HORIZONS telnet client."
  (:require [horizons.time :as t]
            [instaparse.core :as core]
            [instaparse.transform :as transform]))

(def parse
  "Parse a string into a parse tree."
  (core/parser (clojure.java.io/resource "horizons.bnf")))

(defn ^:private string->int [s]
  (Integer/parseInt s))

(def ^:private transform-rules
  {
   :date (fn [& more] [:date (t/datemap->date (into {} more))])
   :ephemeris (fn [& more] {:ephemeris (set more)})
   :ephemeris-line-item (fn [& more] (into {} more))
   :float bigdec
   :integer string->int
   :measurement-time (fn [& more] {:measurement-time (into {} more)})
   :month (fn [s] [:month (t/month->int s)])
   :time (fn [& more]  {:time (into {} more)})
   :timestamp t/timestamp-transformer})

;; If we could give insta/transform a default rule, it should
;; be (fn [& rest] {:label (into {} rest)}). But alas...

(defn transform
  [coll]
  (if (core/failure? coll)
    coll
    (transform/transform transform-rules coll)))

(defn ^:private coll-of-colls?
  [coll]
  (and
    (vector? coll)
    (every? #(and (coll? %) (not (set? %))) (rest coll))))

(defn ^:private into-map-or-nil
  "Applies `(into {} form)` if the argument is not nil. Otherwise returns nil."
  [coll]
  (when (seq coll) (into {} coll)))

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
      (comp
        (fn [form]
          (cond
            (coll-of-colls? form) {(first form) (into-map-or-nil (rest form))}
            :else form))
        (fn [form]
          (if (keyword? form)
              (keyword "horizons.core" (name form))
              form)))
      coll)))

(defn restructure
  "Applies tree transformations to a parse tree, then recursively converts all remaining key-value vectors into maps."
  [tree]
  (if (core/failure? tree)
    tree
    (->> tree
         transform
         tree->map)))
