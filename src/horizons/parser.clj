(ns horizons.parser
  (:require [instaparse.core :as core]
            [instaparse.transform :as transform]))

(def parse
  (core/parser (clojure.java.io/resource "horizons.bnf")))

(defn all-members-have-same-keys?
  [form]
  (->> form
      rest
      (map #(apply hash-map %))
      (apply =)))

(defn coll-of-colls?
  [form]
  (and
    (coll? form)
    (every? coll? (rest form))))

(defn set-of-maps
  [& args]
  (set (map #(apply hash-map %) args)))

(defn tree->map
  "Transforms a parse tree into a nested map, where the first entry in a non-leaf node becomes the key,
  and the remaining children of the node are placed into a map. Leaf nodes are skipped, so leaf nodes
  become keys and values in their parents maps.

  Imagine that your tree is labeled, where the first member of every node vector is that node's name,
  and the remaining members are that node's children. This function will turn that tree into a
  map traversable by node names."
  [tree]
  (clojure.walk/postwalk
    (fn [form]
      (cond
        (coll-of-colls? form) {(first form) (into {} (rest form))}
        :else form))
   tree))