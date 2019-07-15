(ns horizons.parsing.tree
  "Modifies of a parsed tree into a map of maps")

(defn- do-if
  "Calls (fn x) if pred is true, returning the result. Otherwise, returns x untouched."
  [pred fn x]
  (if (pred x) (fn x) x))

(defn- put-keyword-in-ns
  "Stick a keyword into the horizons.core namespace."
  [x]
  (keyword "horizons.core" (name x)))

(defn- coll-of-colls?
  "Returns true if every element of coll except the first is a collection."
  [xs]
  (and
   (vector? xs)
   (every? (every-pred coll? (complement set?)) (rest xs))))

(defn- tree-vec->map
  "Transforms a list into a nested map, where the first list element is the key,
  and the rest of the list is put into a map under that key."
  [xs]
  {(first xs) (into {} (rest xs))})

(defn tree->map [tree]
  (->> tree
    (clojure.walk/postwalk #(do-if coll-of-colls? tree-vec->map %))
    (clojure.walk/postwalk #(do-if keyword? put-keyword-in-ns %))))
