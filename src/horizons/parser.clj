(ns horizons.parser
  (:require [instaparse.core :as core]
            [instaparse.transform :as transform]))

(def parse
  (core/parser (clojure.java.io/resource "horizons.bnf")))

(def transform-rules
  {
   :date (fn [& args] [:date (into {} args)])
   :ephemeris (fn [& args] {:ephemeris (set args)})
   :ephemeris-line-item (fn [& args] (into {} args))
   :measurement-time (fn [& args] {:measurement-time (into {} args)})
   :timestamp (fn [& args] [:timestamp (into {} args)])})

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

(defn restructure
  [tree]
  (if (core/failure? tree)
    tree
    (->> tree
         transform
         tree->map)))