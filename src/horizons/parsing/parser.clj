(ns horizons.parsing.parser
  "Parses and transforms output from the HORIZONS telnet client."
  (:require [horizons.parsing.time :as t]
            [instaparse.core :as core]
            [instaparse.transform :as transform]
            [clojure.tools.logging :as log]))

(defn ^:private throw-parse-exception
  "Throw an exception documenting a parse exception"
  [failure]
  (throw (Exception. (str "Unable to parse HORIZONS response."
                          "\n\nGot the following failure: \n" (with-out-str (print failure))))))

(defn ^:private assert-success
  [x]
  (log/debug "Checking parse tree for failure")
  (if (instaparse.core/failure? x)
    (throw-parse-exception x)
    x))

(def parse
  "Parse a string into a parse tree."
  (comp
    (-> "horizons.bnf"
      clojure.java.io/resource
      core/parser)))


(defn ^:private string->int [s]
  (Integer/parseInt s))

(defn sci-not-coll->bigdec [significand-coll mantissa-coll]
  (->> [significand-coll mantissa-coll]
    (map last)
    (interpose "E")
    (apply str)
    bigdec))

(defn sci-not->bigdec [significand mantissa]
  (.scaleByPowerOfTen (bigdec significand) mantissa))

(defn value-with-exponent->bigdec
  ([label value]
   [label value])
  ([label exponent-coll value]
   (if (number? value)
     [label (sci-not->bigdec value (last exponent-coll))]
     [label [:value value] exponent-coll])))

(defn value-with-exponent-map->bigdec
  ([label & more]
   (let [fields (into {} more)]
     (if (number? (:value fields))
       [label
        (-> fields
         (assoc :value (sci-not->bigdec (:value fields) (:exponent fields)))
         (dissoc :exponent)
         (merge (dissoc fields :exponent :value)))]
       [label fields]))))

(def ^:private transform-rules
  {:comma-separated-integer #(clojure.string/replace % "," "")
   :date (fn [& more] [:date (t/datemap->date (into {} more))])
   :ephemeredes (fn [& more] [:ephemeredes (into #{} more)])
   :ephemeris (fn [& more] (into {} more))
   :float bigdec
   :heat-flow-mass (partial value-with-exponent-map->bigdec :heat-flow-mass)
   :integer string->int
   :mass (partial value-with-exponent-map->bigdec :mass)
   :month (fn [s] [:month (t/month->int s)])
   :rotation-rate (partial value-with-exponent-map->bigdec :rotation-rate)
   :sci-not sci-not-coll->bigdec
   :time (fn [& more]  {:time (into {} more)})
   :timestamp t/timestamp-transformer
   :unit-23 (constantly [:unit-code "23"])
   :unit-2A (constantly [:unit-code "2A"])
   :unit-A62 (constantly [:unit-code "A62"])
   :unit-BAR (constantly [:unit-code "BAR"])
   :unit-D54 (constantly [:unit-code "D54"])
   :unit-D61 (constantly [:unit-code "D61"])
   :unit-D62 (constantly [:unit-code "D62"])
   :unit-DD (constantly [:unit-code "DD"])
   :unit-H20 (constantly [:unit-code "H20"])
   :unit-KEL (constantly [:unit-code "KEL"])
   :unit-KMT (constantly [:unit-code "KMT"])
   :unit-KGM (constantly [:unit-code "KGM"])
   :unit-MSK (constantly [:unit-code "MSK"])
   :unit-M62 (constantly [:unit-code "M62"])
   :unit-SEC (constantly [:unit-code "SEC"])
   :volume (partial value-with-exponent-map->bigdec :volume)})

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
   (every? (every-pred coll? (complement set?)) (rest coll))))

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

(defn log-parse-result [s]
  (log/debug "Resulting parse tree:\n" s)
  s)

(defn horizons-response->data-structure
  "Parses and transforms a response from HORIZONS
   into a useful data structure."
  [s]
  {:post [(complement empty?)]}
  (->> s
       parse
       assert-success
       log-parse-result
       restructure))
