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
  {:date (fn [& more] [:date (t/datemap->date (into {} more))])
   :ephemeris (fn [& more] {:ephemeris (set more)})
   :ephemeris-line-item (fn [& more] (into {} more))
   :float bigdec
   :integer string->int
   :measurement-time (fn [& more] {:measurement-time (into {} more)})
   :month (fn [s] [:month (t/month->int s)])
   :time (fn [& more]  {:time (into {} more)})
   :timestamp t/timestamp-transformer})

(defn transform
  "Applies transformation functions to all nodes in the parse tree."
  [coll]
  (if-not (core/failure? coll)
          (transform/transform transform-rules coll)
          coll))

(defn ^:private coll-of-colls?
  "Returns true if every element of coll except the first is a collection."
  [coll]
  (and
   (vector? coll)
   (every? #(and (coll? %) (not (set? %))) (rest coll))))

(defn ^:private into-map-or-nil
  "Applies `(into {} form)` if the argument is not nil. Otherwise returns nil."
  [coll]
  (when (seq coll) (into {} coll)))

(defn put-keyword-in-ns
  "If the given element is a keyword, stick that keyword into the current namespace.
   Else, returns that element."
  [form]
  (if (keyword? form)
    (keyword "horizons.core" (name form))
    form))

(defn tree-vec->map
  "Transforms a list into a nested map, where the first list element is the key,
  and the rest of the list is put into a map under that key."
  [form]
  (if (coll-of-colls? form)
      {(first form) (into-map-or-nil (rest form))}
      form))

(def postwalk-step
  "Transforms form into a nested map with namespaced keywords."
  (comp tree-vec->map put-keyword-in-ns))

(defn tree->map
  "Transforms a parse tree into a nested map, where the first entry in a non-leaf node becomes the key,
  and the remaining children of the node are placed into a map. Leaf nodes are skipped, so leaf nodes
  become keys and values in their parents maps.

  Imagine that your tree is labeled, where the first member of every node vector is that node's name,
  and the remaining members are that node's children. This function will turn that tree into a
  map traversable by node names."
  [coll]
  (if-not (core/failure? coll)
          (clojure.walk/postwalk postwalk-step coll)
          coll))

(defn restructure
  "Applies tree transformations to a parse tree, then recursively converts all remaining key-value vectors into maps."
  [tree]
  (if (core/failure? tree)
      tree
      (->> tree
           transform
           tree->map)))
